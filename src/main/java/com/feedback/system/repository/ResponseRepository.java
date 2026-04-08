package com.feedback.system.repository;

import com.feedback.system.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findByFormId(Long formId);
    List<Response> findByStudentId(Long studentId);
    long countByFormCreatedByEmail(String email);
    long countByStudentEmail(String email);
    List<Response> findByFormCreatedByEmail(String email);
    boolean existsByFormIdAndStudentEmail(Long formId, String email);

    @Query("SELECT r.form.id FROM Response r WHERE r.student.email = :email")
    List<Long> findSubmittedFormIdsByStudentEmail(@Param("email") String email);
}
