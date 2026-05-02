package com.smartek.authservice.service;

import com.smartek.authservice.dto.AuthResponse;
import com.smartek.authservice.dto.LoginRequest;
import com.smartek.authservice.dto.RegisterRequest;
import com.smartek.authservice.entity.User;
import com.smartek.authservice.enums.RoleType;
import com.smartek.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitaires")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(1L)
                .firstName("Alice")
                .email("alice@smartek.com")
                .password("encodedPassword")
                .phone("0612345678")
                .role(RoleType.LEARNER)
                .experience(2)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Alice");
        registerRequest.setEmail("alice@smartek.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("0612345678");
        registerRequest.setRole(RoleType.LEARNER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@smartek.com");
        loginRequest.setPassword("password123");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Inscription réussie - retourne token et données utilisateur")
        void register_success() {
            when(userRepository.existsByEmail("alice@smartek.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtService.generateToken(eq("alice@smartek.com"), anyMap())).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo("alice@smartek.com");
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getRole()).isEqualTo(RoleType.LEARNER);
            assertThat(response.getMessage()).isEqualTo("Inscription réussie");

            verify(userRepository).existsByEmail("alice@smartek.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateToken(eq("alice@smartek.com"), anyMap());
        }

        @Test
        @DisplayName("Email déjà utilisé - lève RuntimeException")
        void register_emailAlreadyExists_throwsException() {
            when(userRepository.existsByEmail("alice@smartek.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("déjà utilisé");

            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(anyString(), anyMap());
        }

        @Test
        @DisplayName("Inscription avec image base64 null - ne plante pas")
        void register_withNullImage_success() {
            registerRequest.setImageBase64(null);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtService.generateToken(anyString(), anyMap())).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
        }

        @Test
        @DisplayName("Inscription avec rôle TRAINER - rôle correctement assigné")
        void register_withTrainerRole_success() {
            registerRequest.setRole(RoleType.TRAINER);
            User trainerUser = User.builder()
                    .userId(2L)
                    .firstName("Bob")
                    .email("alice@smartek.com")
                    .role(RoleType.TRAINER)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(trainerUser);
            when(jwtService.generateToken(anyString(), anyMap())).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response.getRole()).isEqualTo(RoleType.TRAINER);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Connexion réussie - retourne token et données utilisateur")
        void login_success() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail("alice@smartek.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(eq("alice@smartek.com"), anyMap())).thenReturn("jwt-token");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo("alice@smartek.com");
            assertThat(response.getMessage()).isEqualTo("Connexion réussie");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail("alice@smartek.com");
        }

        @Test
        @DisplayName("Mauvais mot de passe - lève BadCredentialsException")
        void login_badCredentials_throwsException() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);

            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Utilisateur non trouvé après auth - lève RuntimeException")
        void login_userNotFound_throwsException() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail("alice@smartek.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("non trouvé");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validateUser")
    class ValidateUser {

        @Test
        @DisplayName("Utilisateur existant - retourne true")
        void validateUser_exists_returnsTrue() {
            when(userRepository.existsById(1L)).thenReturn(true);

            boolean result = authService.validateUser(1L);

            assertThat(result).isTrue();
            verify(userRepository).existsById(1L);
        }

        @Test
        @DisplayName("Utilisateur inexistant - retourne false")
        void validateUser_notExists_returnsFalse() {
            when(userRepository.existsById(99L)).thenReturn(false);

            boolean result = authService.validateUser(99L);

            assertThat(result).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("Utilisateur trouvé - retourne les données")
        void getUserById_found_returnsData() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            AuthResponse response = authService.getUserById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("alice@smartek.com");
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getRole()).isEqualTo(RoleType.LEARNER);
        }

        @Test
        @DisplayName("Utilisateur non trouvé - lève RuntimeException")
        void getUserById_notFound_throwsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getUserById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("non trouvé");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllLearners
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllLearners")
    class GetAllLearners {

        @Test
        @DisplayName("Retourne la liste des apprenants")
        void getAllLearners_returnsList() {
            User learner2 = User.builder()
                    .userId(2L).firstName("Bob").email("bob@smartek.com")
                    .role(RoleType.LEARNER).experience(1).build();
            when(userRepository.findByRole(RoleType.LEARNER)).thenReturn(List.of(sampleUser, learner2));

            List<AuthResponse> result = authService.getAllLearners();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(AuthResponse::getRole)
                    .containsOnly(RoleType.LEARNER);
        }

        @Test
        @DisplayName("Aucun apprenant - retourne liste vide")
        void getAllLearners_empty_returnsEmptyList() {
            when(userRepository.findByRole(RoleType.LEARNER)).thenReturn(List.of());

            List<AuthResponse> result = authService.getAllLearners();

            assertThat(result).isEmpty();
        }
    }
}
