package com.smartek.authservice.repository;

import com.smartek.authservice.entity.User;
import com.smartek.authservice.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de repository pour UserRepository.
 * Utilise @DataJpaTest avec MySQL configuré dans application-test.yml.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepository - Tests @DataJpaTest")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User learner;
    private User trainer;
    private User admin;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        learner = User.builder()
                .firstName("Alice")
                .email("alice@smartek.com")
                .password("encodedPassword1")
                .phone("0611111111")
                .role(RoleType.LEARNER)
                .experience(2)
                .build();

        trainer = User.builder()
                .firstName("Bob")
                .email("bob@smartek.com")
                .password("encodedPassword2")
                .phone("0622222222")
                .role(RoleType.TRAINER)
                .experience(5)
                .build();

        admin = User.builder()
                .firstName("Charlie")
                .email("charlie@smartek.com")
                .password("encodedPassword3")
                .role(RoleType.ADMIN)
                .experience(0)
                .build();

        userRepository.saveAll(List.of(learner, trainer, admin));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByEmail
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("Retourne l'utilisateur pour un email existant")
        void existingEmail_returnsUser() {
            Optional<User> result = userRepository.findByEmail("alice@smartek.com");

            assertThat(result).isPresent();
            assertThat(result.get().getFirstName()).isEqualTo("Alice");
            assertThat(result.get().getRole()).isEqualTo(RoleType.LEARNER);
        }

        @Test
        @DisplayName("Retourne Optional.empty() pour un email inexistant")
        void unknownEmail_returnsEmpty() {
            Optional<User> result = userRepository.findByEmail("unknown@smartek.com");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Recherche insensible à la casse — email exact requis")
        void exactEmailRequired() {
            Optional<User> result = userRepository.findByEmail("ALICE@SMARTEK.COM");
            assertThat(result).isEmpty(); // JPA findByEmail est case-sensitive par défaut
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // existsByEmail
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("Retourne true pour un email existant")
        void existingEmail_returnsTrue() {
            assertThat(userRepository.existsByEmail("alice@smartek.com")).isTrue();
        }

        @Test
        @DisplayName("Retourne false pour un email inexistant")
        void unknownEmail_returnsFalse() {
            assertThat(userRepository.existsByEmail("nobody@smartek.com")).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByRole
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByRole()")
    class FindByRole {

        @Test
        @DisplayName("Retourne uniquement les LEARNER")
        void learnerRole_returnsOnlyLearners() {
            List<User> learners = userRepository.findByRole(RoleType.LEARNER);

            assertThat(learners).hasSize(1);
            assertThat(learners.get(0).getEmail()).isEqualTo("alice@smartek.com");
        }

        @Test
        @DisplayName("Retourne uniquement les TRAINER")
        void trainerRole_returnsOnlyTrainers() {
            List<User> trainers = userRepository.findByRole(RoleType.TRAINER);

            assertThat(trainers).hasSize(1);
            assertThat(trainers.get(0).getEmail()).isEqualTo("bob@smartek.com");
        }

        @Test
        @DisplayName("Retourne liste vide pour un rôle sans utilisateur")
        void roleWithNoUsers_returnsEmptyList() {
            List<User> sponsors = userRepository.findByRole(RoleType.SPONSOR);
            assertThat(sponsors).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countByRole
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("countByRole()")
    class CountByRole {

        @Test
        @DisplayName("Compte correctement les utilisateurs par rôle")
        void countsCorrectly() {
            assertThat(userRepository.countByRole(RoleType.LEARNER)).isEqualTo(1L);
            assertThat(userRepository.countByRole(RoleType.TRAINER)).isEqualTo(1L);
            assertThat(userRepository.countByRole(RoleType.ADMIN)).isEqualTo(1L);
            assertThat(userRepository.countByRole(RoleType.SPONSOR)).isEqualTo(0L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD de base
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CRUD de base")
    class BasicCrud {

        @Test
        @DisplayName("Sauvegarde et retrouve un utilisateur par ID")
        void saveAndFindById() {
            User newUser = User.builder()
                    .firstName("Diana")
                    .email("diana@smartek.com")
                    .password("encodedPassword4")
                    .role(RoleType.RH_SMARTEK)
                    .experience(3)
                    .build();

            User saved = userRepository.save(newUser);
            assertThat(saved.getUserId()).isNotNull();

            Optional<User> found = userRepository.findById(saved.getUserId());
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("diana@smartek.com");
        }

        @Test
        @DisplayName("Supprime un utilisateur par ID")
        void deleteById() {
            Long id = learner.getUserId();
            userRepository.deleteById(id);
            assertThat(userRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("findAll retourne tous les utilisateurs")
        void findAll_returnsAll() {
            assertThat(userRepository.findAll()).hasSize(3);
        }

        @Test
        @DisplayName("Contrainte d'unicité sur l'email — double save lève une exception")
        void duplicateEmail_throwsException() {
            User duplicate = User.builder()
                    .firstName("Alice2")
                    .email("alice@smartek.com") // email déjà utilisé
                    .password("anotherPassword")
                    .role(RoleType.LEARNER)
                    .experience(0)
                    .build();

            assertThatThrownBy(() -> {
                userRepository.saveAndFlush(duplicate);
            }).isInstanceOf(Exception.class);
        }
    }
}
