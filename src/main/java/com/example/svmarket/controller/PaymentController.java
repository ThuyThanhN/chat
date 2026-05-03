package com.example.svmarket.controller;

// import com.example.svmarket.entity.SellerPackage;
// import com.example.svmarket.entity.User;
// import com.example.svmarket.repository.SellerPackageRepository;
import com.example.svmarket.service.PaymentService;
// import com.example.svmarket.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
// import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    // @Autowired
    // private SellerPackageRepository sellerPackageRepository;

    // @Autowired
    // private JwtUtil jwtUtil;

    @GetMapping("/create")
    public String createPayment(@RequestParam Integer packageId,  @RequestParam String returnUrl) throws Exception {
        return paymentService.createPaymentUrl(packageId, returnUrl);
    }

    // Tạo URL thanh toán cho đơn hàng
    @GetMapping("/create-order")
    public ResponseEntity<String> createOrderPayment(@RequestParam Integer orderId, @RequestParam String returnUrl) throws Exception {
        String url = paymentService.createOrderPaymentUrl(orderId, returnUrl);
        return ResponseEntity.ok(url);
    }

    // Cập nhật callback để xử lý cả 2 loại
    @GetMapping("/callback")
    public void callback(@RequestParam Map<String, String> params,
                         HttpServletResponse response) throws IOException {

        String orderInfo = params.get("vnp_OrderInfo");
        String[] parts = orderInfo.split("-");
        String type = parts[1];
        String encodedReturnUrl = parts[parts.length - 1];
        String returnUrl = new String(Base64.getDecoder().decode(encodedReturnUrl));

        // Kiểm tra chữ ký VNPay trước khi xử lý
        if (!paymentService.isValidSignature(params)) {
            System.out.println("Chữ ký không hợp lệ!");
            if ("package".equals(type)) {
                response.sendRedirect(returnUrl + "/my-packages?status=failed");
            } else {
                response.sendRedirect(returnUrl + "/purchase-history?status=failed");
            }
            return;
        }

        String responseCode = params.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            try {
                if ("package".equals(type)) {
                    paymentService.handlePaymentSuccess(params);
                    response.sendRedirect(returnUrl + "/my-packages?status=success");
                } else if ("order".equals(type)) {
                    paymentService.handleOrderPaymentSuccess(params);
                    response.sendRedirect(returnUrl + "/purchase-history?status=success");
                }
            } catch (Exception e) {
                System.out.println("Lỗi xử lý callback: " + e.getMessage());
                if ("package".equals(type)) {
                    response.sendRedirect(returnUrl + "/my-packages?status=failed");
                } else {
                    response.sendRedirect(returnUrl + "/purchase-history?status=failed");
                }
            }
        } else {
            if ("package".equals(type)) {
                response.sendRedirect(returnUrl + "/my-packages?status=failed");
            } else {
                response.sendRedirect(returnUrl + "/purchase-history?status=failed");
            }
        }
    }

}
