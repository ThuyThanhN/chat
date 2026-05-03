package com.example.svmarket.config;

import org.springframework.stereotype.Component;

@Component
public class VnpayConfig {
    public String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public String vnp_ReturnUrl = "http://localhost:8080/api/payment/callback";
    public String vnp_TmnCode = "4YUP19I4";
    public String vnp_HashSecret = "MDUIFDCRAKLNBPOFIAFNEKFRNMFBYEPX";
}
