package com.smartek.learningmicroservice.service;

import com.smartek.learningmicroservice.dto.LearningStylePreferenceRequest;
import com.smartek.learningmicroservice.dto.LearningStylePreferenceResponse;
import com.smartek.learningmicroservice.entity.LearningStylePreference;
import com.smartek.learningmicroservice.entity.LearningStyleType;
import com.smartek.learningmicroservice.repository.LearningStylePreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningStylePreferenceServiceTest {

    @Mock
    private LearningStylePreferenceRepository repository;

    @InjectMocks
    private LearningStylePreferenceService service;

    private LearningStylePreference preference;
    private LearningStylePreferenceRequest request;

    @BeforeEach
    void setUp() {
        preference = LearningStylePreference.builder()
                .id(1L)
                .learnerId(5L)
                .learnerName("Bob Martin")
                .preferredStyle(LearningStyleType.VISUAL)
                .videoPreferred(true)
                .textPreferred(false)
                .practicalWorkPreferred(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        request = new LearningStylePreferenceRequest(
                LearningStyleType.VISUAL,
                true,
                false,
                true,
                5L,
                "Bob Martin"
        );
    }

    // ===== createOrUpdatePreference =====

    @Test
    void createOrUpdatePreference_createsNew_whenNotExists() {
        when(repository.findByLearnerId(5L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(preference);

        LearningStylePreferenceResponse response = service.createOrUpdatePreference(request);

        assertThat(response).isNotNull();
        assertThat(response.getLearnerId()).isEqualTo(5L);
        verify(repository).save(any(LearningStylePreference.class));
    }

    @Test
    void createOrUpdatePreference_updatesExisting_whenExists() {
        when(repository.findByLearnerId(5L)).thenReturn(Optional.of(preference));
        when(repository.save(any())).thenReturn(preference);

        LearningStylePreferenceResponse response = service.createOrUpdatePreference(request);

        assertThat(response.getPreferredStyle()).isEqualTo(LearningStyleType.VISUAL);
        assertThat(response.getVideoPreferred()).isTrue();
        verify(repository).save(preference);
    }

    @Test
    void createOrUpdatePreference_updatesLearnerName_whenProvided() {
        request.setLearnerName("Bob Updated");
        when(repository.findByLearnerId(5L)).thenReturn(Optional.of(preference));
        when(repository.save(any())).thenReturn(preference);

        service.createOrUpdatePreference(request);

        assertThat(preference.getLearnerName()).isEqualTo("Bob Updated");
    }

    // ===== getByLearnerId =====

    @Test
    void getByLearnerId_found() {
        when(repository.findByLearnerId(5L)).thenReturn(Optional.of(preference));

        LearningStylePreferenceResponse response = service.getByLearnerId(5L);

        assertThat(response.getLearnerId()).isEqualTo(5L);
        assertThat(response.getPreferredStyle()).isEqualTo(LearningStyleType.VISUAL);
    }

    @Test
    void getByLearnerId_notFound_throwsException() {
        when(repository.findByLearnerId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByLearnerId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No learning style preference found");
    }

    // ===== existsForLearner =====

    @Test
    void existsForLearner_returnsTrue_whenExists() {
        when(repository.existsByLearnerId(5L)).thenReturn(true);

        assertThat(service.existsForLearner(5L)).isTrue();
    }

    @Test
    void existsForLearner_returnsFalse_whenNotExists() {
        when(repository.existsByLearnerId(99L)).thenReturn(false);

        assertThat(service.existsForLearner(99L)).isFalse();
    }

    // ===== deleteByLearnerId =====

    @Test
    void deleteByLearnerId_success() {
        when(repository.findByLearnerId(5L)).thenReturn(Optional.of(preference));

        service.deleteByLearnerId(5L);

        verify(repository).delete(preference);
    }

    @Test
    void deleteByLearnerId_notFound_throwsException() {
        when(repository.findByLearnerId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteByLearnerId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No preference to delete");
    }

    // ===== getAllPreferences =====

    @Test
    void getAllPreferences_returnsList() {
        when(repository.findAll()).thenReturn(List.of(preference));

        List<LearningStylePreferenceResponse> result = service.getAllPreferences();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLearnerName()).isEqualTo("Bob Martin");
    }

    @Test
    void getAllPreferences_empty_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<LearningStylePreferenceResponse> result = service.getAllPreferences();

        assertThat(result).isEmpty();
    }
}
