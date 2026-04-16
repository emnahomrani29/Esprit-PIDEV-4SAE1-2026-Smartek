package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public Application toEntity(ApplicationRequest request) {
        return Application.builder()
                .learnerId(request.getLearnerId())
                .learnerName(request.getLearnerName())
                .learnerEmail(request.getLearnerEmail())
                .coverLetter(request.getCoverLetter())
                .cvBase64(request.getCvBase64())
                .cvFileName(request.getCvFileName())
                .status("PENDING")
                .score(0)
                .build();
    }

    public ApplicationResponse toResponse(Application application) {
        String offerTitle = application.getOffer() != null ? application.getOffer().getTitle() : null;
        Long offerId = application.getOffer() != null ? application.getOffer().getId() : application.getOfferId();

        return ApplicationResponse.builder()
                .id(application.getId())
                .offerId(offerId)
                .offerTitle(offerTitle)
                .learnerId(application.getLearnerId())
                .learnerName(application.getLearnerName())
                .learnerEmail(application.getLearnerEmail())
                .coverLetter(application.getCoverLetter())
                .cvBase64(application.getCvBase64())
                .cvFileName(application.getCvFileName())
                .score(application.getScore())
                .status(application.getStatus())
                .recruiterNote(application.getRecruiterNote())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
