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

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferExpiryScheduler {

    private final OfferRepository offerRepository;

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void closeExpiredOffers() {
        List<Offer> expired = offerRepository.findExpiredOffers(LocalDateTime.now());
        if (expired.isEmpty()) { log.debug("No expired offers found."); return; }
        expired.forEach(offer -> {
            offer.setStatus("EXPIRED");
            log.info("Offer id={} '{}' marked as EXPIRED", offer.getId(), offer.getTitle());
        });
        offerRepository.saveAll(expired);
        log.info("Closed {} expired offer(s).", expired.size());
    }
}
