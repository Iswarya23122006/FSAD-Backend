package com.feedback.system.controller;

import com.feedback.system.dto.FormRequest;
import com.feedback.system.dto.MessageResponse;
import com.feedback.system.dto.QuestionRequest;
import com.feedback.system.entity.FeedbackForm;
import com.feedback.system.entity.Question;
import com.feedback.system.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/form")
public class FormController {
    @Autowired
    private FormService formService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createForm(@RequestBody FormRequest formRequest, Authentication authentication) {
        try {
            FeedbackForm form = formService.createForm(formRequest, authentication.getName());
            return ResponseEntity.ok(form);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/question/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addQuestion(@RequestBody QuestionRequest questionRequest) {
        try {
            Question question = formService.addQuestion(questionRequest);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<FeedbackForm>> getAllForms(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(formService.getAllForms(null, "ROLE_STUDENT"));
        }
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("ROLE_STUDENT");
        return ResponseEntity.ok(formService.getAllForms(email, role));
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupData() {
        try {
            formService.cleanupAllData();
            return ResponseEntity.ok(new MessageResponse("All form data cleared successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFormById(@PathVariable Long id) {
        try {
            FeedbackForm form = formService.getFormById(id);
            return ResponseEntity.ok(form);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteForm(@PathVariable Long id) {
        try {
            formService.deleteForm(id);
            return ResponseEntity.ok(new MessageResponse("Form deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
