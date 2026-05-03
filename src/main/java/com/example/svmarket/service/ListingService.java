package com.example.svmarket.service;

import java.time.LocalDateTime;
import java.util.*;

import com.example.svmarket.dto.*;
import com.example.svmarket.entity.*;
import com.example.svmarket.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.svmarket.exception.BadRequestException;
import com.example.svmarket.service.CloudinaryService.UploadedImage;

@Service
public class ListingService {

    private static final int MAX_IMAGES = 5;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ListingFavoriteRepository listingFavoriteRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private SellerPackageRepository sellerPackageRepository;

    // Lay danh sach danh muc de hien thi dropdown o form.
    public List<CategoryOptionResponse> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryOptionResponse(category.getId(), category.getName(), category.getImage()))
                .toList();
    }

    public Map<String, Object> getPostLimit(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Reset free nếu hết hạn miễn phí lượt đăng
        if (user.getFreeResetDate() == null ||
                user.getFreeResetDate().isBefore(LocalDateTime.now())) {

            user.setFreePostsRemaining(3);
            user.setFreeResetDate(LocalDateTime.now().plusMonths(1));
            userRepository.save(user);
        }

        SellerPackage pkg = sellerPackageRepository
                .findAvailablePackage(user.getId(), LocalDateTime.now())
                .stream()
                .findFirst()
                .orElse(null);

        int freeRemaining = user.getFreePostsRemaining();
        int packageRemaining = pkg != null ? pkg.getRemainingPosts() : 0;

        boolean canPost = (freeRemaining > 0) || (packageRemaining > 0);

        int remaining = freeRemaining > 0 ? freeRemaining : packageRemaining;

        String message;
        if (freeRemaining > 0) {
            message = "Bạn còn " + freeRemaining + " lượt đăng miễn phí";
        } else if (packageRemaining > 0) {
            message = "Bạn còn " + packageRemaining + " lượt đăng từ gói";
        } else {
            message = "Hết lượt đăng. Vui lòng mua gói tin.";
        }

        return Map.of(
                "canPost", canPost,
                "freeRemaining", freeRemaining,
                "packageRemaining", packageRemaining,
                "remaining", remaining,
                "message", message);
    }

    // Tao bai dang moi cua user dang dang nhap.
    @Transactional
    public ListingDetailResponse createMyListing(String email,
            ListingUpsertRequest request,
            List<MultipartFile> images) {

        User seller = getUserByEmail(email);

        resetFree(seller);

        Category category = getCategoryById(request.getCategoryId());
        PostSource postSource = request.getPostSource() != null
                ? PostSource.valueOf(request.getPostSource())
                : PostSource.FREE;

        Listing listing = Listing.builder()
                .seller(seller)
                .category(category)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .deliveryAddress(request.getDeliveryAddress())
                .conditionLevel(request.getConditionLevel())
                .status(ListingStatus.PENDING)
                .stock(1)
                .postSource(postSource)
                .build();

        SellerPackage pkg = sellerPackageRepository
                .findAvailablePackage(seller.getId(), LocalDateTime.now())
                .stream()
                .findFirst()
                .orElse(null);

        // Free
        if (postSource == PostSource.FREE) {

            if (seller.getFreePostsRemaining() <= 0) {
                throw new BadRequestException("Hết lượt đăng miễn phí");
            }

            seller.setFreePostsRemaining(seller.getFreePostsRemaining() - 1);
            userRepository.save(seller);

        } else { // PACKAGE

            if (pkg == null) {
                throw new BadRequestException("Bạn chưa đăng ký gói");
            }

            if (pkg.getRemainingPosts() <= 0) {
                throw new BadRequestException("Gói đã hết lượt đăng");
            }

            if (pkg.getRemainingPushes() <= 0) {
                throw new BadRequestException("Gói đã hết lượt đẩy");
            }

            if (pkg.getRemainingPosts() <= 0 && pkg.getRemainingPushes() <= 0) {
                pkg.setStatus(PackageStatus.EXPIRED);
            }
            listing.setSellerPackage(pkg);
            sellerPackageRepository.save(pkg);
        }

        Listing savedListing = listingRepository.save(listing);
        List<String> imageUrls = saveImages(savedListing, images);

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            Notification notification = Notification.builder()
                    .user(admin)
                    .content("Có bài đăng mới cần kiểm duyệt: " + savedListing.getTitle())
                    .type(NotificationType.SYSTEM)
                    .referenceId(savedListing.getId())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }

        return toDetailResponse(savedListing, imageUrls);
    }

    // Lấy danh sách nổi bật
    public List<ListingSummaryResponse> getFeaturedListings() {
        return listingRepository.findByStatus(ListingStatus.ACTIVE)
                .stream()
                .filter(l ->
                        l.getPostSource() == PostSource.PACKAGE
                )
                .filter(l -> {
                    SellerPackage pkg = l.getSellerPackage(); // ✅
                    return pkg != null
                            && Boolean.TRUE.equals(pkg.getPackagePlan().getIsFeatured());
                })
                .sorted((a, b) -> Integer.compare(getPriority(b), getPriority(a)))
                .limit(4)
                .map(this::toSummaryResponse)
                .toList();
    }

    // Reset lượt đăng free mỗi tháng
    public void resetFree(User user) {
        if (user.getFreeResetDate() == null ||
                user.getFreeResetDate().isBefore(LocalDateTime.now())) {

            user.setFreePostsRemaining(3);
            user.setFreeResetDate(LocalDateTime.now().plusMonths(1));
            userRepository.save(user);
        }
    }

    public SellerPackage getPkg(Listing listing) {
        return sellerPackageRepository
                .findActivePackage(listing.getSeller().getId(), LocalDateTime.now())
                .orElse(null);
    }

    // Lay danh sach bai dang cua user hien tai.
    public List<ListingSummaryResponse> getMyListings(String email) {
        User seller = getUserByEmail(email);

        return listingRepository.findBySellerIdAndStatusNotOrderByCreatedAtDesc(seller.getId(), ListingStatus.DELETED)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // Lay danh sach bai dang dang hoat dong de hien thi o trang chu.
    public List<ListingSummaryResponse> getActiveListings() {
        List<Listing> listings = listingRepository.findByStatus(ListingStatus.ACTIVE);

        return listings.stream()
                .sorted(listingComparator())
                .map(this::toSummaryResponse)
                .toList();
    }

    // Lấy mức độ ưu tiên theo gói đã đăng ký
    public int getPriority(Listing listing) {
        if (listing.getPostSource() == PostSource.FREE) return 0;

        SellerPackage pkg = listing.getSellerPackage();
        if (pkg == null) return 0;

        // Chỉ trả về priorityLevel, không cộng thêm gì
        return pkg.getPackagePlan().getPriorityLevel();
    }

    // Kiểm tra xem còn trong thời gian được đẩy hay không?
    public boolean isPushActive(Listing listing) {

        if (listing.getLastPushAt() == null) return false;

        SellerPackage pkg = listing.getSellerPackage();

        if (pkg == null) return false;

        return listing.getLastPushAt()
                .plusHours(pkg.getPackagePlan().getPushHours())
                .isAfter(LocalDateTime.now());
    }

    // Lọc tổng hợp kết hợp từ khóa, trường đại học, danh mục và sắp xếp
    public List<ListingSummaryResponse> filterListingsCustom(
            String keyword, String university, Integer categoryId, String sortBy) {

        List<Listing> listings = listingRepository
                .filterListingsCustom(keyword, university, categoryId, sortBy);

        // 🔥 sort chuẩn
        listings.sort(listingComparator());

        // sort giá nếu user chọn
        if ("price_asc".equals(sortBy)) {
            listings.sort(Comparator.comparing(Listing::getPrice));
        } else if ("price_desc".equals(sortBy)) {
            listings.sort(Comparator.comparing(Listing::getPrice).reversed());
        }

        return listings.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // Lọc bài đăng theo trường đại học và sắp xếp theo độ ưu tiên gói tin
    public List<ListingSummaryResponse> filterByUniversity(String university) {

        if (university == null || university.isBlank()) {
            return getActiveListings();
        }

        List<Listing> listings = listingRepository
                .findByUniversityCustom(ListingStatus.ACTIVE, university.trim());

        listings.sort(listingComparator());

        return listings.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // Them/bo luu bai dang theo user dang nhap.
    @Transactional
    public FavoriteToggleResponse toggleFavoriteListing(String email, Integer listingId) {
        User user = getUserByEmail(email);
        Listing listing = listingRepository.findByIdAndStatus(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Bai dang khong ton tai hoac da bi an"));

        return listingFavoriteRepository.findByUserIdAndListingId(user.getId(), listing.getId())
                .map(existingFavorite -> {
                    listingFavoriteRepository.delete(existingFavorite);
                    return new FavoriteToggleResponse(listingId, false);
                })
                .orElseGet(() -> {
                    ListingFavorite favorite = ListingFavorite.builder()
                            .user(user)
                            .listing(listing)
                            .build();
                    listingFavoriteRepository.save(favorite);
                    return new FavoriteToggleResponse(listingId, true);
                });
    }

    // Lay danh sach bai dang da luu cua user dang nhap.
    public List<ListingSummaryResponse> getMyFavoriteListings(String email) {
        User user = getUserByEmail(email);

        return listingFavoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ListingFavorite::getListing)
                .filter(listing -> listing != null && listing.getStatus() == ListingStatus.ACTIVE)
                .map(this::toSummaryResponse)
                .toList();
    }

    // Lay danh sach id bai dang da luu de to mau icon tim tren UI.
    public List<Integer> getMyFavoriteListingIds(String email) {
        User user = getUserByEmail(email);

        return listingFavoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ListingFavorite::getListing)
                .filter(listing -> listing != null && listing.getStatus() == ListingStatus.ACTIVE)
                .map(Listing::getId)
                .distinct()
                .toList();
    }

    // Lay chi tiet mot bai dang hoat dong de hien thi trang san pham.
    public ListingDetailResponse getActiveListingById(Integer listingId) {
        Listing listing = listingRepository.findByIdAndStatus(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Bai dang khong ton tai hoac da bi an"));

        List<String> imageUrls = listing.getImages() == null
                ? List.of()
                : listing.getImages().stream().map(Image::getUrl).toList();

        return toPublicDetailResponse(listing, imageUrls);
    }

    // Lay chi tiet mot bai dang cua user hien tai de hien thi va sua.
    public ListingDetailResponse getMyListingById(String email, Integer listingId) {
        User seller = getUserByEmail(email);
        Listing listing = getMyListingByIdAndSellerId(listingId, seller.getId());

        List<String> imageUrls = listing.getImages() == null
                ? List.of()
                : listing.getImages().stream().map(Image::getUrl).toList();

        return toDetailResponse(listing, imageUrls);
    }

    // Cap nhat bai dang cua user hien tai, co the thay anh moi.
    @Transactional
    public ListingDetailResponse updateMyListing(String email,
            Integer listingId,
            ListingUpsertRequest request,
            List<MultipartFile> images) {
        User seller = getUserByEmail(email);
        Listing listing = getMyListingByIdAndSellerId(listingId, seller.getId());

        Category category = getCategoryById(request.getCategoryId());

        listing.setTitle(request.getTitle().trim());
        listing.setCategory(category);
        listing.setPrice(request.getPrice());
        listing.setDeliveryAddress(request.getDeliveryAddress());
        listing.setConditionLevel(request.getConditionLevel());
        listing.setDescription(request.getDescription());
        listing.setStatus(parseStatus(request.getStatus()));

        List<String> imageUrls;

        if (images != null && !images.isEmpty() && images.stream().anyMatch(file -> !file.isEmpty())) {
            // Xoa anh cu tren Cloudinary truoc khi thay bo anh moi.
            List<Image> oldImages = imageRepository.findByListingId(listing.getId());
            oldImages.forEach(oldImage -> cloudinaryService.deleteImage(oldImage.getPublicId()));
            imageRepository.deleteByListingId(listing.getId());
            imageUrls = saveImages(listing, images);
        } else {
            imageUrls = listing.getImages() == null
                    ? List.of()
                    : listing.getImages().stream().map(Image::getUrl).toList();
        }

        Listing updatedListing = listingRepository.save(listing);
        return toDetailResponse(updatedListing, imageUrls);
    }

    // Xoa mem bai dang de van giu lai du lieu giao dich.
    @Transactional
    public void deleteMyListing(String email, Integer listingId) {
        User seller = getUserByEmail(email);
        Listing listing = getMyListingByIdAndSellerId(listingId, seller.getId());

        listing.setStatus(ListingStatus.DELETED);
        listingRepository.save(listing);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Khong tim thay user"));
    }

    private Category getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("Danh muc khong ton tai"));
    }

    private Listing getMyListingByIdAndSellerId(Integer listingId, Integer sellerId) {
        return listingRepository.findByIdAndSellerId(listingId, sellerId)
                .orElseThrow(() -> new BadRequestException("Bai dang khong ton tai hoac ban khong co quyen"));
    }

    private ListingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ListingStatus.ACTIVE;
        }

        try {
            return ListingStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Trang thai bai dang khong hop le");
        }
    }

    private ListingDetailResponse toDetailResponse(Listing listing, List<String> imageUrls) {
        Boolean isVerified = listing.getSeller() != null && Boolean.TRUE.equals(listing.getSeller().getIsVerified());
        return ListingDetailResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .categoryId(listing.getCategory() != null ? listing.getCategory().getId() : null)
                .categoryName(listing.getCategory() != null ? listing.getCategory().getName() : null)
                .price(listing.getPrice())
                .deliveryAddress(listing.getDeliveryAddress())
                .conditionLevel(listing.getConditionLevel())
                .description(listing.getDescription())
                .status(listing.getStatus() != null ? listing.getStatus().name() : ListingStatus.ACTIVE.name())
                .imageUrls(imageUrls)
                .sellerId(listing.getSeller() != null ? listing.getSeller().getId() : null)
                .sellerAvatar(listing.getSeller() != null ? listing.getSeller().getAvatar() : null)
                .postSource(listing.getPostSource().name())
                .isVerified(isVerified)
                .build();
    }

    private ListingDetailResponse toPublicDetailResponse(Listing listing, List<String> imageUrls) {
        Boolean isVerified = listing.getSeller() != null && Boolean.TRUE.equals(listing.getSeller().getIsVerified());
        return ListingDetailResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .categoryId(listing.getCategory() != null ? listing.getCategory().getId() : null)
                .categoryName(listing.getCategory() != null ? listing.getCategory().getName() : null)
                .price(listing.getPrice())
                .deliveryAddress(listing.getDeliveryAddress())
                .conditionLevel(listing.getConditionLevel())
                .description(listing.getDescription())
                .status(listing.getStatus() != null ? listing.getStatus().name() : ListingStatus.ACTIVE.name())
                .imageUrls(imageUrls)
                .sellerName(listing.getSeller() != null ? listing.getSeller().getFullName() : null)
                .sellerUniversity(listing.getSeller() != null ? listing.getSeller().getUniversity() : null)
                .isVerified(isVerified)
                .thumbnailUrl(!imageUrls.isEmpty() ? imageUrls.get(0) : null)
                .createdAt(listing.getCreatedAt())
                .sellerId(listing.getSeller() != null ? listing.getSeller().getId() : null)
                .sellerAvatar(listing.getSeller() != null ? listing.getSeller().getAvatar() : null)
                .build();
    }

    public ListingSummaryResponse toSummaryResponse(Listing listing) {
        Boolean isVerified = listing.getSeller() != null && Boolean.TRUE.equals(listing.getSeller().getIsVerified());

        String thumbnail = listing.getImages() != null && !listing.getImages().isEmpty()
                ? listing.getImages().get(0).getUrl()
                : null;

        ListingSummaryResponse response = new ListingSummaryResponse();
        response.setId(listing.getId());
        response.setTitle(listing.getTitle());
        response.setPrice(listing.getPrice());
        response.setStatus(listing.getStatus() != null ? listing.getStatus().name() : ListingStatus.ACTIVE.name());
        response.setThumbnailUrl(thumbnail);
        response.setSellerUniversity(listing.getSeller() != null ? listing.getSeller().getUniversity() : null);
        response.setSellerName(listing.getSeller() != null ? listing.getSeller().getFullName() : null);
        response.setIsVerified(isVerified);
        response.setCreatedAt(listing.getCreatedAt());

        if (listing.getPostSource() == PostSource.PACKAGE) {

            SellerPackage pkg = listing.getSellerPackage();

            if (pkg != null) {
                int priority = pkg.getPackagePlan().getPriorityLevel();
                boolean isFeatured = Boolean.TRUE.equals(pkg.getPackagePlan().getIsFeatured());

                boolean stillPushing = isPushActive(listing);

                response.setPriorityLevel(priority);
                response.setIsFeatured(isFeatured);
                response.setPushing(stillPushing);

            } else {
                response.setPriorityLevel(0);
                response.setIsFeatured(false);
                response.setPushing(false);
            }
        }

        return response;
    }

    // Luu danh sach anh bai dang len Cloudinary va DB.
    private List<String> saveImages(Listing listing, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<MultipartFile> validImages = images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (validImages.size() > MAX_IMAGES) {
            throw new BadRequestException("Chi duoc tai len toi da 5 anh");
        }

        List<String> imageUrls = new ArrayList<>();
        List<Image> imageEntities = new ArrayList<>();

        for (MultipartFile file : validImages) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Tat ca file phai la hinh anh");
            }

            UploadedImage uploadedImage = cloudinaryService.uploadListingImage(file);
            String url = uploadedImage.secureUrl();
            imageUrls.add(url);

            imageEntities.add(Image.builder()
                    .url(url)
                    .publicId(uploadedImage.publicId())
                    .listing(listing)
                    .build());
        }

        imageRepository.saveAll(imageEntities);
        return imageUrls;
    }

    // loc bai bai dang theo truong
    public List<ListingSummaryResponse> getListingsByUniversity(String university) {
        return listingRepository.findByUniversity(university)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // loc bai dang theo danh muc
    public List<ListingSummaryResponse> getListingsByCategory(Integer categoryId) {
        return listingRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // Lấy danh sách bài đăng (có mua gói) đã được duyệt
    public List<PushHistoryResponse> getPushHistory(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        SellerPackage activePkg = sellerPackageRepository
                .findAvailablePackage(user.getId(), LocalDateTime.now())
                .stream()
                .findFirst()
                .orElse(null);

        String packageName = activePkg != null
                ? activePkg.getPackagePlan().getName()
                : "—";

        int remainingPushes = activePkg != null
                ? activePkg.getRemainingPushes()
                : 0;

        int pushHours = activePkg != null
                ? activePkg.getPackagePlan().getPushHours()
                : 0;

        return listingRepository
                .findBySellerIdAndPostSource(user.getId(), PostSource.PACKAGE)
                .stream()
                .filter(listing -> listing.getStatus() == ListingStatus.ACTIVE)
                .map(listing -> {

                    LocalDateTime lastPush = listing.getLastPushAt();

                    LocalDateTime expiresAt = lastPush != null
                            ? lastPush.plusHours(pushHours)
                            : null;

                    boolean pushExpired = expiresAt != null
                            && LocalDateTime.now().isAfter(expiresAt);

                    boolean canPush = pushExpired
                            && activePkg != null
                            && remainingPushes > 0;

                    return new PushHistoryResponse(
                            listing.getId(),
                            listing.getTitle(),
                            packageName,
                            lastPush,
                            expiresAt,
                            remainingPushes,
                            canPush
                    );
                })
                .toList();
    }

    //  Xử lý đẩy lại bài đăng khi bấm nút "Đẩy lại"
    @Transactional
    public void pushListing(String email, Integer listingId) {

        User user = getUserByEmail(email);

        Listing listing = listingRepository
                .findByIdAndSellerId(listingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));

        SellerPackage pkg = sellerPackageRepository
                .findAvailablePackage(user.getId(), LocalDateTime.now())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không có gói khả dụng"));

        if (pkg.getRemainingPushes() <= 0) {
            throw new RuntimeException("Hết lượt đẩy");
        }

        // Kiểm tra chưa hết thời gian push
        if (listing.getLastPushAt() != null) {
            LocalDateTime expire = listing.getLastPushAt()
                    .plusHours(pkg.getPackagePlan().getPushHours());

            if (LocalDateTime.now().isBefore(expire)) {
                throw new RuntimeException("Chưa hết thời gian đẩy");
            }
        }

        // Cập nhật
        listing.setLastPushAt(LocalDateTime.now());
        pkg.setRemainingPushes(pkg.getRemainingPushes() - 1);

        listingRepository.save(listing);
        sellerPackageRepository.save(pkg);
    }

    /**
     * Comparator dùng để sắp xếp danh sách bài đăng theo độ ưu tiên hiển thị.
     *
     * Thứ tự ưu tiên:
     * 1. Gói đăng (priorityLevel):
     *    - Gói cao hơn (VIP > Sinh viên > Free) luôn đứng trước
     *
     * 2. Trạng thái đẩy (push):
     *    - Nếu cùng gói, bài nào còn trong thời gian được đẩy sẽ đứng trước
     *
     * 3. Thời điểm đẩy gần nhất (lastPushAt):
     *    - Nếu cả hai đều đang được đẩy, bài được đẩy gần đây hơn sẽ đứng trước
     *
     * 4. Thời gian tạo (createdAt):
     *    - Nếu không còn push, bài đăng mới hơn sẽ đứng trước
     *
     */
    public Comparator<Listing> listingComparator() {
        return (a, b) -> {

            // Package
            int priorityA = getPriority(a);
            int priorityB = getPriority(b);

            if (priorityA != priorityB) {
                return Integer.compare(priorityB, priorityA);
            }

            // Push
            boolean pushA = isPushActive(a);
            boolean pushB = isPushActive(b);

            if (pushA != pushB) {
                return pushA ? -1 : 1;
            }

            // Push time
            if (pushA && pushB) {
                return b.getLastPushAt().compareTo(a.getLastPushAt());
            }

            // Created
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        };
    }
}