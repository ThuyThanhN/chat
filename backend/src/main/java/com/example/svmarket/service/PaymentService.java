package com.example.svmarket.service;

import com.example.svmarket.config.VnpayConfig;
import com.example.svmarket.entity.*;
import com.example.svmarket.repository.*;
import com.example.svmarket.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private VnpayConfig config;
    @Autowired private SellerPackageRepository sellerPackageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ListingRepository listingRepository;

    public String createVnpayUrl(String orderInfo, long amount) throws Exception {
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", config.vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", vnp_TxnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", config.vnp_ReturnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        Map<String, String> build = buildQueryAndHash(params);
        String hash = hmacSHA512(config.vnp_HashSecret, build.get("hashData"));
        return config.vnp_PayUrl + "?" + build.get("query") + "&vnp_SecureHash=" + hash;
    }

    public boolean isValidSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        Map<String, String> signParams = new HashMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        try {
            Map<String, String> built = buildQueryAndHash(signParams);
            String expectedHash = hmacSHA512(config.vnp_HashSecret, built.get("hashData"));
            return expectedHash.equalsIgnoreCase(receivedHash);
        } catch (Exception e) {
            return false;
        }
    }


    // Thanh toán gói tin
    public String createPaymentUrl(Integer packageId, String returnUrl) throws Exception {
        User user = jwtUtil.getCurrentUser();
        PackagePlan plan = packagePlanRepository.findById(packageId).orElseThrow();

        // Kiểm tra gói hợp lệ
        if (plan.getPrice() == null || plan.getPrice().longValue() <= 0) {
            throw new RuntimeException("Gói tin không hợp lệ");
        }

        String encodedReturnUrl = Base64.getEncoder().encodeToString(returnUrl.getBytes());
        String orderInfo = "type-package-packageId-" + packageId +
                "-userId-" + user.getId() +
                "-returnUrl-" + encodedReturnUrl;

        return createVnpayUrl(orderInfo, plan.getPrice().longValue() * 100);
    }

    public void handlePaymentSuccess(Map<String, String> params) {
        String orderInfo = params.get("vnp_OrderInfo");
        String[] parts = orderInfo.split("-");
        Integer packageId = Integer.parseInt(parts[3]);
        Integer userId = Integer.parseInt(parts[5]);

        // Kiểm tra userId tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        PackagePlan plan = packagePlanRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Gói tin không tồn tại"));

        // Kiểm tra số tiền khớp
        long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
        long planAmount = plan.getPrice().longValue();
        if (vnpAmount != planAmount) {
            throw new RuntimeException("Số tiền không khớp: " + vnpAmount + " != " + planAmount);
        }

        SellerPackage sp = SellerPackage.builder()
                .seller(user)
                .packagePlan(plan)
                .remainingPosts(plan.getPostLimit())
                .remainingPushes(plan.getPushLimit())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(plan.getDurationDays()))
                .build();

        sellerPackageRepository.save(sp);
    }

    // Thanh toán đơn hàng
    public String createOrderPaymentUrl(Integer orderId, String returnUrl) throws Exception {
        User user = jwtUtil.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Kiểm tra order thuộc về user
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền thanh toán đơn hàng này");
        }

        // Kiểm tra trạng thái hợp lệ
        if (!List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED).contains(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái có thể thanh toán");
        }

        // Kiểm tra số tiền hợp lệ
        if (order.getTotalAmount() == null || order.getTotalAmount().longValue() <= 0) {
            throw new RuntimeException("Số tiền đơn hàng không hợp lệ");
        }

        // Khởi tạo bản ghi Payment ở trạng thái PENDING trước khi sang VNPay
        Payment pendingPayment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .paymentMethod("VNPay")
                .build();
        paymentRepository.save(pendingPayment);

        String encodedReturnUrl = Base64.getEncoder().encodeToString(returnUrl.getBytes());
        String orderInfo = "type-order-orderId-" + orderId +
                "-userId-" + user.getId() +
                "-returnUrl-" + encodedReturnUrl;

        return createVnpayUrl(orderInfo, order.getTotalAmount().longValue() * 100);
    }

    public void handleOrderPaymentSuccess(Map<String, String> params) {
        String orderInfo = params.get("vnp_OrderInfo");
        String[] parts = orderInfo.split("-");
        Integer orderId = Integer.parseInt(parts[3]);
        Integer userId = Integer.parseInt(parts[5]);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Kiểm tra userId khớp với buyer
        if (!order.getBuyer().getId().equals(userId)) {
            throw new RuntimeException("userId không khớp với đơn hàng");
        }

        // Kiểm tra số tiền khớp
        long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
        long orderAmount = order.getTotalAmount().longValue();
        if (vnpAmount != orderAmount) {
            throw new RuntimeException("Số tiền không khớp: " + vnpAmount + " != " + orderAmount);
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        Payment payment = paymentRepository
                .findTopByOrderIdOrderByIdDesc(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentMethod("VNPay");
        payment.setTransactionId(params.get("vnp_TransactionNo"));
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Cập nhật trạng thái bài đăng thành SOLD (Đã bán)
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            Listing listing = order.getOrderDetails().get(0).getListing();
            if (listing != null) {
                listing.setStatus(ListingStatus.SOLD);
                listingRepository.save(listing);
            }
        }
    }

    public static Map<String, String> buildQueryAndHash(Map<String, String> params) throws Exception {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8);
                hashData.append(fieldName).append("=").append(encodedValue).append("&");
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append("=").append(encodedValue).append("&");
            }
        }

        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);

        Map<String, String> result = new HashMap<>();
        result.put("hashData", hashData.toString());
        result.put("query", query.toString());
        return result;
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            final SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}