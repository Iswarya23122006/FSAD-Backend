package com.feedback.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResponseRequest {
    private Long formId;
    private List<AnswerRequest> answers;
}
