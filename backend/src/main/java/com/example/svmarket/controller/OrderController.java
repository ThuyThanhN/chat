package com.example.svmarket.controller;

import java.util.List;
// import java.util.stream.Collectors;

// import com.example.svmarket.dto.OrderDetailResponse;
// import com.example.svmarket.dto.OrderItemResponse;
// import com.example.svmarket.entity.Order;
// import com.example.svmarket.entity.Payment;
// import com.example.svmarket.entity.User;
// import com.example.svmarket.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.svmarket.dto.OrderRequest;
import com.example.svmarket.dto.OrderResponse;
import com.example.svmarket.service.OrderService;
import com.example.svmarket.util.JwtUtil;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class OrderController {

    @Autowired
    private OrderService orderService;

    // @Autowired
    // private OrderRepository orderRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/request")
    public String createOrder(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody OrderRequest request) {
        if (token == null || !token.startsWith("Bearer ") || token.equals("Bearer null")) {
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện giao dịch");
        }

        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        orderService.createOrder(email, request);
        return "Đặt hàng thành công";
    }

    @GetMapping("/sales")
    public List<OrderResponse> getSalesHistory(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ") || token.equals("Bearer null")) {
            throw new RuntimeException("Vui lòng đăng nhập để xem lịch sử bán hàng");
        }

        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        return orderService.getSalesHistory(email);
    }

    @GetMapping("/purchases")
    public List<OrderResponse> getPurchaseHistory(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ") || token.equals("Bearer null")) {
            throw new RuntimeException("Vui lòng đăng nhập để xem lịch sử mua hàng");
        }

        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        return orderService.getPurchaseHistory(email);
    }

    @PutMapping("/{id}/accept")
    public String acceptOrder(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ") || token.equals("Bearer null")) {
            throw new RuntimeException("Vui lòng đăng nhập để thao tác");
        }

        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        orderService.acceptOrder(id, email);
        return "Đã chấp nhận đơn hàng";
    }

    // Chi tiết đơn hàng
    @GetMapping("/{orderId}/detail")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer orderId) {
        try {
            return ResponseEntity.ok(orderService.getOrderDetail(orderId));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("quyền")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}