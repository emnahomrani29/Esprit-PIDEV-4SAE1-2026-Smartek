package com.smartek.offersservice.repository;

import com.smartek.offersservice.entity.SavedOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedOfferRepository extends JpaRepository<SavedOffer, Long> {

    List<SavedOffer> findByLearnerId(Long learnerId);

    boolean existsByOfferIdAndLearnerId(Long offerId, Long learnerId);

    Optional<SavedOffer> findByOfferIdAndLearnerId(Long offerId, Long learnerId);

    void deleteByOfferIdAndLearnerId(Long offerId, Long learnerId);

    long countByOfferId(Long offerId);
}
