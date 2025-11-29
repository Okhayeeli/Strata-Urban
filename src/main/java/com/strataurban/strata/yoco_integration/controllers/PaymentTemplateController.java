package com.strataurban.strata.yoco_integration.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payment")
public class PaymentTemplateController {


    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(required = false) String transactionRef, @RequestParam(required = false) String bookingId, @RequestParam(required = false) String amount, Model model) {

        model.addAttribute("transactionRef", transactionRef);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("amount", amount);

        return "payment/success";
    }

    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam(required = false) String transactionRef, @RequestParam(required = false) String bookingId, Model model) {

        model.addAttribute("transactionRef", transactionRef);
        model.addAttribute("bookingId", bookingId);

        return "payment/cancel";
    }

    @GetMapping("/failure")
    public String paymentFailure(@RequestParam(required = false) String transactionRef, @RequestParam(required = false) String bookingId, @RequestParam(required = false) String reason, Model model) {

        model.addAttribute("transactionRef", transactionRef);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("reason", reason);

        return "payment/failure";
    }
}