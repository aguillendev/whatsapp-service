package com.example.whatsapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrivacyPolicyController {

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "forward:/privacy-policy.html";
    }
}
