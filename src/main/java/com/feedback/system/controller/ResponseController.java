package com.feedback.system.controller;

import com.feedback.system.dto.MessageResponse;
import com.feedback.system.dto.ResponseRequest;
import com.feedback.system.entity.Response;
import com.feedback.system.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/response")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitResponse(@RequestBody ResponseRequest responseRequest, Authentication authentication) {
        try {
            responseService.submitResponse(responseRequest, authentication.getName());
            return ResponseEntity.ok(new MessageResponse("Feedback submitted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Admin: total responses received for all their forms
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getResponseCount(Authentication authentication) {
        return ResponseEntity.ok(responseService.countTotalResponsesForAdmin(authentication.getName()));
    }

    // Student: how many forms they have submitted
    @GetMapping("/student/count")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Long> getStudentResponseCount(Authentication authentication) {
        return ResponseEntity.ok(responseService.countStudentResponses(authentication.getName()));
    }

    // Student: get list of form IDs this student has already submitted
    @GetMapping("/student/submitted-form-ids")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Long>> getStudentSubmittedFormIds(Authentication authentication) {
        return ResponseEntity.ok(responseService.getSubmittedFormIdsForStudent(authentication.getName()));
    }

    // Admin: get detailed responses for a form (with student info and answers)
    @GetMapping("/form/{formId}")
    public ResponseEntity<?> getFormResponses(@PathVariable Long formId) {
        try {
            List<Response> responses = responseService.getResponsesForForm(formId);
            List<Map<String, Object>> result = responses.stream().map(r -> {
                Map<String, Object> map = new HashMap<>();
                map.put("studentName", r.getStudent().getName());
                map.put("studentEmail", r.getStudent().getEmail());
                List<Map<String, String>> answers = r.getAnswers().stream().map(a -> {
                    Map<String, String> ans = new HashMap<>();
                    ans.put("question", a.getQuestion().getQuestionText());
                    ans.put("answer", a.getAnswerText() != null ? a.getAnswerText() : "");
                    return ans;
                }).collect(Collectors.toList());
                map.put("answers", answers);
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Admin: get all responses for all their forms
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllResponses(Authentication authentication) {
        try {
            List<Response> responses = responseService.getResponsesForAdmin(authentication.getName());
            List<Map<String, Object>> result = responses.stream().map(r -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("formId", r.getForm().getId());
                map.put("studentName", r.getStudent().getName());
                map.put("studentEmail", r.getStudent().getEmail());
                // For charts, we could add average rating if calculated here
                // For now, let's keep it minimal for performance
                map.put("date", new Date()); // Response entity might need a createdAt field
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
