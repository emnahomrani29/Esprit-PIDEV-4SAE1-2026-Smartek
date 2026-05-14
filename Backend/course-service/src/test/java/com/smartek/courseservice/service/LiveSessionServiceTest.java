package com.smartek.courseservice.service;

import com.smartek.courseservice.dto.LiveSessionRequest;
import com.smartek.courseservice.dto.LiveSessionResponse;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.DeliveryMode;
import com.smartek.courseservice.entity.LiveSession;
import com.smartek.courseservice.entity.SessionStatus;
import com.smartek.courseservice.exception.BadRequestException;
import com.smartek.courseservice.exception.ResourceNotFoundException;
import com.smartek.courseservice.mapper.LiveSessionMapper;
import com.smartek.courseservice.repository.CourseRepository;
import com.smartek.courseservice.repository.LiveSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LiveSessionService - Tests unitaires")
class LiveSessionServiceTest {

    @Mock
    private LiveSessionRepository liveSessionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LiveSessionMapper liveSessionMapper;

    @InjectMocks
    private LiveSessionService liveSessionService;

    private Course onlineCourse;
    private Course presentielCourse;
    private LiveSession session;
    private LiveSessionRequest request;
    private LiveSessionResponse response;

    @BeforeEach
    void setUp() {
        onlineCourse = Course.builder()
                .courseId(1L).title("Spring Boot Online")
                .duration(LocalDate.now().plusMonths(3)).trainerId(1L)
                .deliveryMode(DeliveryMode.EN_LIGNE).chapters(new ArrayList<>())
                .build();

        presentielCourse = Course.builder()
                .courseId(2L).title("Spring Boot Présentiel")
                .duration(LocalDate.now().plusMonths(3)).trainerId(1L)
                .deliveryMode(DeliveryMode.PRESENTIEL).chapters(new ArrayList<>())
                .build();

        session = LiveSession.builder()
                .sessionId(1L).course(onlineCourse).title("Session 1")
                .trainerId(1L).startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .roomId("room-abc123").status(SessionStatus.SCHEDULED)
                .currentParticipants(0).build();

        request = LiveSessionRequest.builder()
                .courseId(1L).title("Session 1").trainerId(1L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        response = LiveSessionResponse.builder()
                .sessionId(1L).courseId(1L).title("Session 1")
                .roomId("room-abc123").status(SessionStatus.SCHEDULED).build();
    }

    @Nested
    @DisplayName("createSession()")
    class CreateSession {

        @Test
        @DisplayName("Doit créer une session pour un cours EN_LIGNE")
        void shouldCreateSessionForOnlineCourse() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(onlineCourse));
            when(liveSessionMapper.toEntity(any(), any())).thenReturn(session);
            when(liveSessionRepository.save(any(LiveSession.class))).thenReturn(session);
            when(liveSessionMapper.toResponse(any(LiveSession.class), anyString())).thenReturn(response);

            LiveSessionResponse result = liveSessionService.createSession(request);

            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo(1L);
            verify(liveSessionRepository, times(1)).save(any(LiveSession.class));
        }

        @Test
        @DisplayName("Doit lever BadRequestException pour un cours PRESENTIEL")
        void shouldThrowForPresentielCourse() {
            when(courseRepository.findById(2L)).thenReturn(Optional.of(presentielCourse));
            request.setCourseId(2L);

            assertThatThrownBy(() -> liveSessionService.createSession(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cours en ligne");
        }

        @Test
        @DisplayName("Doit lever BadRequestException si endTime avant startTime")
        void shouldThrowWhenEndTimeBeforeStartTime() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(onlineCourse));
            request.setEndTime(LocalDateTime.now().minusDays(1));

            assertThatThrownBy(() -> liveSessionService.createSession(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("heure de fin");
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le cours n'existe pas")
        void shouldThrowWhenCourseNotFound() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());
            request.setCourseId(99L);

            assertThatThrownBy(() -> liveSessionService.createSession(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getSessionsByCourseId()")
    class GetSessionsByCourseId {

        @Test
        @DisplayName("Doit retourner les sessions d'un cours")
        void shouldReturnSessions() {
            when(liveSessionRepository.findByCourseId(1L)).thenReturn(List.of(session));
            when(liveSessionMapper.toResponse(session)).thenReturn(response);

            List<LiveSessionResponse> result = liveSessionService.getSessionsByCourseId(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune session")
        void shouldReturnEmptyList() {
            when(liveSessionRepository.findByCourseId(99L)).thenReturn(List.of());

            List<LiveSessionResponse> result = liveSessionService.getSessionsByCourseId(99L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSessionById()")
    class GetSessionById {

        @Test
        @DisplayName("Doit retourner la session par ID")
        void shouldReturnSessionById() {
            when(liveSessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(liveSessionMapper.toResponse(session)).thenReturn(response);

            LiveSessionResponse result = liveSessionService.getSessionById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si la session n'existe pas")
        void shouldThrowWhenNotFound() {
            when(liveSessionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> liveSessionService.getSessionById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateSessionStatus()")
    class UpdateSessionStatus {

        @Test
        @DisplayName("Doit changer le statut d'une session")
        void shouldUpdateStatus() {
            when(liveSessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(liveSessionRepository.save(any(LiveSession.class))).thenReturn(session);
            when(liveSessionMapper.toResponse(any(LiveSession.class), anyString())).thenReturn(response);

            LiveSessionResponse result = liveSessionService.updateSessionStatus(1L, SessionStatus.ONGOING);

            assertThat(result).isNotNull();
            verify(liveSessionRepository, times(1)).save(any(LiveSession.class));
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si la session n'existe pas")
        void shouldThrowWhenNotFound() {
            when(liveSessionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> liveSessionService.updateSessionStatus(99L, SessionStatus.ONGOING))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteSession()")
    class DeleteSession {

        @Test
        @DisplayName("Doit supprimer une session existante")
        void shouldDeleteSession() {
            when(liveSessionRepository.existsById(1L)).thenReturn(true);

            liveSessionService.deleteSession(1L);

            verify(liveSessionRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si la session n'existe pas")
        void shouldThrowWhenNotFound() {
            when(liveSessionRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> liveSessionService.deleteSession(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getSessionsByTrainerId()")
    class GetSessionsByTrainerId {

        @Test
        @DisplayName("Doit retourner les sessions d'un trainer")
        void shouldReturnSessionsByTrainer() {
            when(liveSessionRepository.findByTrainerIdOrderByStartTimeDesc(1L)).thenReturn(List.of(session));
            when(liveSessionMapper.toResponse(session)).thenReturn(response);

            List<LiveSessionResponse> result = liveSessionService.getSessionsByTrainerId(1L);

            assertThat(result).hasSize(1);
        }
    }
}
