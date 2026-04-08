package com.feedback.system.dto;

import lombok.Data;

@Data
public class AnswerRequest {
    private Long questionId;
    private String answerText;
    private Long selectedOptionId;
}
