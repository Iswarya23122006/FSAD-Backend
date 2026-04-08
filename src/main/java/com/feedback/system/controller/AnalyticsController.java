package com.feedback.system.controller;

import com.feedback.system.dto.AnalyticsResponse;
import com.feedback.system.dto.MessageResponse;
import com.feedback.system.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/{formId}")
    public ResponseEntity<?> getAnalytics(@PathVariable Long formId, org.springframework.security.core.Authentication authentication) {
        try {
            String adminEmail = authentication.getName();
            AnalyticsResponse analytics = analyticsService.getAnalytics(formId, adminEmail);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
