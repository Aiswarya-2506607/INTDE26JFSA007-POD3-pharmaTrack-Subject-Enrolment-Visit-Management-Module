package com.cts.pharmaTrack.module.subjectEnrolment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Inbound payload for creating or updating a trial subject.
 * {@code subjectId} is the client-supplied primary key (e.g. {@code SUB001})
 * and is required on both create and update.
 */
@Getter
@Setter
public class TrialSubjectRequest {

    @NotBlank(message = "subjectId is required")
    private String subjectId;

    @NotBlank(message = "trialId is required")
    private String trialId;

    @NotBlank(message = "siteId is required")
    private String siteId;

    @NotBlank(message = "subjectCode is required")
    private String subjectCode;

    private LocalDate dateOfBirth;

    private String gender;

    private LocalDate consentDate;

    private LocalDate enrolmentDate;

    private String status;
}
