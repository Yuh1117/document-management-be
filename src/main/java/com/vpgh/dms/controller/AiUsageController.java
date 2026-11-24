package com.vpgh.dms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vpgh.dms.model.dto.response.AiUsageRes;
import com.vpgh.dms.service.AiUsageService;

@RestController
@RequestMapping("/api")
public class AiUsageController {
    private static final int MAX_DAYS = 90;

    private final AiUsageService aiUsageService;

    public AiUsageController(AiUsageService aiUsageService) {
        this.aiUsageService = aiUsageService;
    }

    /** Admin only: /api/admin/** requires an admin role (SecurityConfiguration). */
    @GetMapping("/admin/ai-usage")
    public ResponseEntity<AiUsageRes> getUsage(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(aiUsageService.getUsage(Math.clamp(days, 1, MAX_DAYS)));
    }
}
