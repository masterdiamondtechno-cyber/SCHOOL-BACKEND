package com.schoolmanagement.subscription.service;

import com.schoolmanagement.subscription.dto.PaymentOrderRequestDTO;
import com.schoolmanagement.subscription.dto.PaymentOrderResponseDTO;
import com.schoolmanagement.subscription.dto.PaymentVerifyRequestDTO;
import com.schoolmanagement.subscription.dto.SchoolSubscriptionDTO;

public interface RazorpayService {

    // 1. Order create karo (Razorpay ya Mock)
    PaymentOrderResponseDTO createOrder(Long schoolId, PaymentOrderRequestDTO request);

    // 2. Payment verify karo aur subscription ACTIVE karo
    SchoolSubscriptionDTO verifyPayment(Long schoolId, PaymentVerifyRequestDTO request);
}
