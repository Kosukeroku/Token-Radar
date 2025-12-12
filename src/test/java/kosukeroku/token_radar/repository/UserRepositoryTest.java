package kosukeroku.token_radar.repository;

import kosukeroku.token_radar.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@mail.com");
        testUser.setPassword("encodedPassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    void findByUsername_Success() {
        // given
        Optional<User> found = userRepository.findByUsername("testuser");

        // then
        assertThat(found)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getEmail()).isEqualTo("test@mail.com");
                    assertThat(user.getId()).isNotNull();
                });
    }

    @Test
    void findByUsername_CaseSensitive() {
        // when
        Optional<User> foundLower = userRepository.findByUsername("testuser");
        Optional<User> foundUpper = userRepository.findByUsername("TESTUSER");

        // then
        assertThat(foundLower).isPresent();
        assertThat(foundUpper).isEmpty();
    }

    @Test
    void findByUsername_NotFound() {
        // when
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_True() {
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
    }

    @Test
    void existsByUsername_False() {
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void existsByEmail_True() {
        assertThat(userRepository.existsByEmail("test@mail.com")).isTrue();
    }

    @Test
    void existsByEmail_False() {
        assertThat(userRepository.existsByEmail("nonexistent@mail.com")).isFalse();
    }

    @Test
    void findByEmail_Success() {
        // when
        Optional<User> found = userRepository.findByEmail("test@mail.com");

        // then
        assertThat(found)
                .isPresent()
                .get()
                .extracting(User::getEmail, User::getUsername)
                .containsExactly("test@mail.com", "testuser");
    }

    @Test
    void save_WithDuplicateUsername_ShouldThrowException() {
        // given
        User duplicateUser = new User();
        duplicateUser.setUsername("testuser"); // duplicate
        duplicateUser.setEmail("different@mail.com");
        duplicateUser.setPassword("password");
        duplicateUser.setCreatedAt(LocalDateTime.now());

        // then
            assertThatThrownBy(() -> userRepository.save(duplicateUser))
                    .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAll_ReturnsAllUsers() {
        // given
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setEmail("second@mail.com");
        secondUser.setPassword("password");
        secondUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(secondUser);

        // when
        List<User> allUsers = userRepository.findAll();

        // then
        assertThat(allUsers)
                .hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("testuser", "seconduser");
    }

    @Test
    void delete_RemovesUser() {
        // given
        Long userId = testUser.getId();

        // when
        userRepository.delete(testUser);
        entityManager.flush();

        // then
        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(userRepository.existsByUsername("testuser")).isFalse();
    }

    @Test
    void count_ReturnsCorrectNumber() {
        // given
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@mail.com");
            user.setPassword("password");
            userRepository.save(user);
        }

        // when
        long count = userRepository.count();

        // then
        assertThat(count).isEqualTo(6);
    }

}