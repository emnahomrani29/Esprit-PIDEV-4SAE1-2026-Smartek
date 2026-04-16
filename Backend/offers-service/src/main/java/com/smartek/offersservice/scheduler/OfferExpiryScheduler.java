package com.smartek.offersservice.scheduler;

import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that automatically closes expired offers.
 *
 * Business rule: An ACTIVE offer with expiresAt < now is automatically
 * transitioned to EXPIRED status.
 *
 * Runs every hour. Uses @Scheduled with fixedDelay to avoid overlap.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfferExpiryScheduler {

    private final OfferRepository offerRepository;

    /**
     * Runs every hour and marks expired offers as EXPIRED.
     */
    @Scheduled(fixedDelay = 3_600_000) // every 1 hour
    @Transactional
    public void closeExpiredOffers() {
        List<Offer> expired = offerRepository.findExpiredOffers(LocalDateTime.now());

        if (expired.isEmpty()) {
            log.debug("No expired offers found.");
            return;
        }

        expired.forEach(offer -> {
            offer.setStatus(Offer.OfferStatus.EXPIRED);
            log.info("Offer id={} '{}' marked as EXPIRED (was due {})",
                    offer.getId(), offer.getTitle(), offer.getExpiresAt());
        });

        offerRepository.saveAll(expired);
        log.info("Closed {} expired offer(s).", expired.size());
    }
}
