package com.example.svmarket.service;

import com.example.svmarket.dto.OrderDetailResponse;
import com.example.svmarket.dto.OrderItemResponse;
import com.example.svmarket.entity.*;
import com.example.svmarket.repository.*;
import com.example.svmarket.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.svmarket.dto.OrderRequest;
import com.example.svmarket.dto.OrderResponse;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JwtUtil jwtUtil;


    public void createOrder(String buyerEmail, OrderRequest request) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người mua"));

        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));

        if (buyer.getId().equals(listing.getSeller().getId())) {
            throw new RuntimeException("Bạn không thể tự đặt mua sản phẩm của chính mình");
        }

        // Tạo đơn hàng lưu vào bảng orders
        Order order = Order.builder()
                .buyer(buyer)
                .seller(listing.getSeller())
                .totalAmount(listing.getPrice())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Tạo chi tiết đơn hàng lưu vào bảng order_details
        OrderDetail orderDetail = OrderDetail.builder()
                .order(savedOrder)
                .listing(listing)
                .quantity(1)
                .price(listing.getPrice())
                .note(request.getNote())
                .build();

        orderDetailRepository.save(orderDetail);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .amount(savedOrder.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .paymentMethod(null)
                .build();

        paymentRepository.save(payment);

        // Tạo thông báo gửi đến người bán
        String content = buyer.getFullName() + " muốn mua " + listing.getTitle();
        Notification notification = Notification.builder()
                .user(listing.getSeller())
                .content(content)
                .type(NotificationType.ORDER)
                .referenceId(savedOrder.getId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public List<OrderResponse> getSalesHistory(String email) {
        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return orderRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId())
                .stream()
                .map(order -> {
                    OrderDetail detail = order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()
                            ? order.getOrderDetails().get(0) : null;

                    String productTitle = detail != null && detail.getListing() != null ? detail.getListing().getTitle() : "Sản phẩm không xác định";
                    String imageUrl = detail != null && detail.getListing() != null && detail.getListing().getImages() != null && !detail.getListing().getImages().isEmpty()
                            ? detail.getListing().getImages().get(0).getUrl() : "/images/detail.png";

                    String buyerName = order.getBuyer() != null ? order.getBuyer().getFullName() : "Khách";
                    String buyerInitials = buyerName.length() >= 2 ? buyerName.substring(0, 2).toUpperCase() : "KH";
                    String buyerEmail = order.getBuyer() != null ? order.getBuyer().getEmail() : "";

                    return OrderResponse.builder()
                            .id(order.getId())
                            .buyerName(buyerName)
                            .buyerInitials(buyerInitials)
                            .product(productTitle)
                            .price(order.getTotalAmount())
                            .status(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN")
                            .email(buyerEmail)
                            .requestDate(order.getCreatedAt())
                            .note(detail != null && detail.getNote() != null ? detail.getNote() : "") 
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList();
    }

    public List<OrderResponse> getPurchaseHistory(String email) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(order -> {
                    OrderDetail detail = order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()
                            ? order.getOrderDetails().get(0) : null;

                    String productTitle = detail != null && detail.getListing() != null ? detail.getListing().getTitle() : "Sản phẩm không xác định";
                    String imageUrl = detail != null && detail.getListing() != null && detail.getListing().getImages() != null && !detail.getListing().getImages().isEmpty()
                            ? detail.getListing().getImages().get(0).getUrl() : "/images/detail.png";

                    String sellerName = order.getSeller() != null ? order.getSeller().getFullName() : "Khuyết danh";
                    String sellerInitials = sellerName.length() >= 2 ? sellerName.substring(0, 2).toUpperCase() : "KD";
                    String sellerEmail = order.getSeller() != null ? order.getSeller().getEmail() : "";

                    return OrderResponse.builder()
                            .id(order.getId())
                            .buyerName(sellerName)
                            .buyerInitials(sellerInitials)
                            .product(productTitle)
                            .price(order.getTotalAmount())
                            .status(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN")
                            .email(sellerEmail)
                            .requestDate(order.getCreatedAt())
                            .note(detail != null && detail.getNote() != null ? detail.getNote() : "") 
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList();
    }

    public void acceptOrder(Integer orderId, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("Bạn không có quyền thao tác trên đơn hàng này");
        }

        order.setStatus(OrderStatus.ACCEPTED);
        orderRepository.save(order);

        // Tạm ẩn bài đăng thành HIDDEN (hoặc INACTIVE) để người khác không thể đặt mua trong lúc chờ thanh toán
        Listing listing = order.getOrderDetails().get(0).getListing();
        listing.setStatus(ListingStatus.HIDDEN); 
        listingRepository.save(listing);

        // Tạo thông báo gửi đến người mua
        String productTitle = order.getOrderDetails().get(0).getListing().getTitle();
        String content = "Người bán đã chấp nhận yêu cầu mua " + productTitle + " của bạn. Vui lòng thanh toán.";
        Notification notification = Notification.builder().user(order.getBuyer()).content(content)
                .type(NotificationType.PAYMENT).referenceId(order.getId()).isRead(false).build();
        notificationRepository.save(notification);
    }

    // Lấy thông tin chi tiết của đơn hàng
    public OrderDetailResponse getOrderDetail(Integer orderId) {

        User currentUser = jwtUtil.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Không có quyền xem đơn hàng này");
        }

        Payment latestPayment = order.getPayments().isEmpty()
                ? null
                : order.getPayments().get(order.getPayments().size() - 1);

        List<OrderItemResponse> items = order.getOrderDetails().stream()
                .map(d -> new OrderItemResponse(
                        d.getListing().getTitle(),
                        d.getQuantity(),
                        d.getPrice(),
                        d.getNote()
                ))
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getBuyer().getFullName(),
                order.getSeller().getFullName(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                latestPayment != null ? latestPayment.getStatus().name() : "PENDING",
                latestPayment != null ? latestPayment.getPaymentMethod() : null,
                latestPayment != null ? latestPayment.getCreatedAt() : null,
                items
        );
    }
}