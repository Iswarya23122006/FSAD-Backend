package com.feedback.system.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AnalyticsResponse {
    private Long formId;
    private String formTitle;
    private int totalResponses;
    private List<QuestionAnalytics> questionAnalytics;

    @Data
    public static class QuestionAnalytics {
        private Long questionId;
        private String questionText;
        private String type;
        private Map<String, Long> optionCounts; // For RADIO, CHECKBOX
        private List<String> textAnswers; // For TEXT
        private Double averageRating; // For STAR
    }
}
