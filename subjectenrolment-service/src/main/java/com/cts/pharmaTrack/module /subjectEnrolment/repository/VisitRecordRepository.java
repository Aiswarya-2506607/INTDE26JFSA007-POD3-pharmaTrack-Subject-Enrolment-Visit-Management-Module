package com.cts.pharmaTrack.module.subjectEnrolment.repository;

import com.cts.pharmaTrack.module.subjectEnrolment.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRecordRepository extends JpaRepository<VisitRecord, String> {

    List<VisitRecord> findBySubjectId(String subjectId);

    boolean existsBySubjectId(String subjectId);
}
