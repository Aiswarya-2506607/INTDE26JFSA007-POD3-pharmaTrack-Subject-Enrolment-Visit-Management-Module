package com.cts.pharmaTrack.module.subjectEnrolment.service;

import com.cts.pharmaTrack.module.subjectEnrolment.exception.DuplicateSubjectCodeException;
import com.cts.pharmaTrack.module.subjectEnrolment.exception.SubjectNotFoundException;
import com.cts.pharmaTrack.module.subjectEnrolment.exception.VisitDeletionNotAllowedException;
import com.cts.pharmaTrack.module.subjectEnrolment.exception.VisitNotFoundException;
import com.cts.pharmaTrack.module.subjectEnrolment.dto.VisitRecordRequest;
import com.cts.pharmaTrack.module.subjectEnrolment.dto.VisitRecordResponse;
import com.cts.pharmaTrack.module.subjectEnrolment.entity.VisitRecord;
import com.cts.pharmaTrack.module.subjectEnrolment.repository.AdverseEventRepository;
import com.cts.pharmaTrack.module.subjectEnrolment.repository.TrialSubjectRepository;
import com.cts.pharmaTrack.module.subjectEnrolment.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Business logic for visit records, including parent-subject validation and
 * deletion guarded by linked adverse events. The primary key {@code visitId}
 * is supplied by the client (e.g. {@code VIS001}).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VisitRecordService {

    private static final Logger logger = LoggerFactory.getLogger(VisitRecordService.class);
    private static final String DEFAULT_STATUS = "Scheduled";

    private final VisitRecordRepository visitRepository;
    private final TrialSubjectRepository subjectRepository;
    private final AdverseEventRepository adverseEventRepository;

    public VisitRecordResponse create(VisitRecordRequest request) {
        logger.info("Executing create with visitId: {}", request.getVisitId());
        if (visitRepository.existsById(request.getVisitId())) {
            throw new DuplicateSubjectCodeException("visitId already exists: " + request.getVisitId());
        }
        requireSubjectExists(request.getSubjectId());
        VisitRecord visit = new VisitRecord();
        visit.setVisitId(request.getVisitId());
        apply(visit, request);
        visit.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : DEFAULT_STATUS);
        return toResponse(visitRepository.save(visit));
    }

    @Transactional(readOnly = true)
    public List<VisitRecordResponse> getAll() {
        logger.info("Executing getAll");
        List<VisitRecord> visits = visitRepository.findAll();
        if (visits.isEmpty()) {
            throw new VisitNotFoundException("No visits found");
        }
        return visits.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VisitRecordResponse getById(String visitId) {
        logger.info("Executing getById with visitId: {}", visitId);
        return toResponse(findOrThrow(visitId));
    }

    @Transactional(readOnly = true)
    public List<VisitRecordResponse> getBySubjectId(String subjectId) {
        logger.info("Executing getBySubjectId with subjectId: {}", subjectId);
        List<VisitRecord> visits = visitRepository.findBySubjectId(subjectId);
        if (visits.isEmpty()) {
            throw new VisitNotFoundException("No visits found for subject");
        }
        return visits.stream().map(this::toResponse).toList();
    }

    public VisitRecordResponse update(VisitRecordRequest request) {
        logger.info("Executing update with visitId: {}", request.getVisitId());
        VisitRecord visit = findOrThrow(request.getVisitId());
        requireSubjectExists(request.getSubjectId());
        apply(visit, request);
        visit.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : visit.getStatus());
        return toResponse(visitRepository.save(visit));
    }

    public void delete(String visitId) {
        logger.info("Executing delete with visitId: {}", visitId);
        VisitRecord visit = findOrThrow(visitId);
        if (adverseEventRepository.existsByVisitId(visitId)) {
            throw new VisitDeletionNotAllowedException();
        }
        visitRepository.delete(visit);
    }

    private void apply(VisitRecord visit, VisitRecordRequest request) {
        visit.setSubjectId(request.getSubjectId());
        visit.setVisitType(request.getVisitType());
        visit.setScheduledDate(request.getScheduledDate());
        visit.setActualDate(request.getActualDate());
        visit.setConductedById(request.getConductedById());
        visit.setObservations(request.getObservations());
        visit.setSampleCollected(request.getSampleCollected());
    }

    private void requireSubjectExists(String subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new SubjectNotFoundException();
        }
    }

    private VisitRecord findOrThrow(String visitId) {
        return visitRepository.findById(visitId)
                .orElseThrow(VisitNotFoundException::new);
    }

    private VisitRecordResponse toResponse(VisitRecord visit) {
        return new VisitRecordResponse(
                visit.getVisitId(),
                visit.getSubjectId(),
                visit.getVisitType(),
                visit.getScheduledDate(),
                visit.getActualDate(),
                visit.getConductedById(),
                visit.getObservations(),
                visit.getSampleCollected(),
                visit.getStatus());
    }
}
