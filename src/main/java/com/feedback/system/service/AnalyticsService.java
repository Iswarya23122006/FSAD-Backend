package com.feedback.system.service;

import com.feedback.system.dto.AnalyticsResponse;
import com.feedback.system.entity.*;
import com.feedback.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private FeedbackFormRepository formRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private AnswerRepository answerRepository;

    public AnalyticsResponse getAnalytics(Long formId, String adminEmail) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Error: Form not found."));

        if (adminEmail != null && !form.getCreatedBy().getEmail().equals(adminEmail)) {
            throw new RuntimeException("Error: You do not have permission to view analytics for this form.");
        }

        List<Response> responses = responseRepository.findByFormId(formId);

        AnalyticsResponse analyticsResponse = new AnalyticsResponse();
        analyticsResponse.setFormId(formId);
        analyticsResponse.setFormTitle(form.getTitle());
        analyticsResponse.setTotalResponses(responses.size());

        List<AnalyticsResponse.QuestionAnalytics> questionAnalyticsList = form.getQuestions().stream().map(question -> {
            AnalyticsResponse.QuestionAnalytics qAnalytics = new AnalyticsResponse.QuestionAnalytics();
            qAnalytics.setQuestionId(question.getId());
            qAnalytics.setQuestionText(question.getQuestionText());
            qAnalytics.setType(question.getType().name());

            List<Answer> answers = answerRepository.findByQuestionId(question.getId());

            if (question.getType() == Question.QuestionType.RADIO || question.getType() == Question.QuestionType.CHECKBOX) {
                Map<String, Long> optionCounts = new HashMap<>();
                for (Option option : question.getOptions()) {
                    optionCounts.put(option.getOptionText(), 0L);
                }

                for (Answer answer : answers) {
                    if (answer.getSelectedOption() != null) {
                        String optText = answer.getSelectedOption().getOptionText();
                        optionCounts.put(optText, optionCounts.getOrDefault(optText, 0L) + 1);
                    }
                }
                qAnalytics.setOptionCounts(optionCounts);
            } else if (question.getType() == Question.QuestionType.TEXT) {
                List<String> textAnswers = answers.stream()
                        .map(Answer::getAnswerText)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                qAnalytics.setTextAnswers(textAnswers);
            } else if (question.getType() == Question.QuestionType.STAR) {
                double avgRating = answers.stream()
                        .filter(a -> a.getAnswerText() != null)
                        .mapToDouble(a -> Double.parseDouble(a.getAnswerText()))
                        .average()
                        .orElse(0.0);
                qAnalytics.setAverageRating(avgRating);
            }

            return qAnalytics;
        }).collect(Collectors.toList());

        analyticsResponse.setQuestionAnalytics(questionAnalyticsList);
        return analyticsResponse;
    }
}
