package com.feedback.system.repository;

import com.feedback.system.entity.FeedbackForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {
    List<FeedbackForm> findByCreatedByEmail(String email);
}
