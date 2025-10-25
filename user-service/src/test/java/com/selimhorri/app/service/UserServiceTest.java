package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

/**
 * Comprehensive test suite for UserService
 * Tests all CRUD operations, authentication scenarios, and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private Credential testCredential;
    private CredentialDto testCredentialDto;

    @BeforeEach
    void setUp() {
        testCredential = Credential.builder()
                .credentialId(1)
                .username("johndoe")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        testCredentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("johndoe")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        testUser = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .credential(testCredential)
                .build();

        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .credentialDto(testCredentialDto)
                .build();
    }

    @Nested
    @DisplayName("Find All Operations")
    class FindAllTests {

        @Test
        @DisplayName("Should return all users when repository has data")
        void shouldReturnAllUsers_WhenRepositoryHasData() {
            // Given
            List<User> users = Arrays.asList(testUser);
            when(userRepository.findAll()).thenReturn(users);

            // When
            List<UserDto> result = userService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("John", result.get(0).getFirstName());
            assertEquals("Doe", result.get(0).getLastName());
            assertEquals("john.doe@example.com", result.get(0).getEmail());
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when repository has no data")
        void shouldReturnEmptyList_WhenRepositoryHasNoData() {
            // Given
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<UserDto> result = userService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find By ID Operations")
    class FindByIdTests {

        @Test
        @DisplayName("Should return user when it exists")
        void shouldReturnUser_WhenItExists() {
            // Given
            Integer userId = 1;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            UserDto result = userService.findById(userId);

            // Then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("john.doe@example.com", result.getEmail());
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw exception when user does not exist")
        void shouldThrowException_WhenUserDoesNotExist() {
            // Given
            Integer userId = 999;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            UserObjectNotFoundException exception = assertThrows(
                    UserObjectNotFoundException.class,
                    () -> userService.findById(userId)
            );
            
            assertTrue(exception.getMessage().contains("User with id: 999 not found"));
            verify(userRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("Find By Username Operations")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should return user when username exists")
        void shouldReturnUser_WhenUsernameExists() {
            // Given
            String username = "johndoe";
            when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.of(testUser));

            // When
            UserDto result = userService.findByUsername(username);

            // Then
            assertNotNull(result);
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("johndoe", result.getCredentialDto().getUsername());
            verify(userRepository).findByCredentialUsername(username);
        }

        @Test
        @DisplayName("Should throw exception when username does not exist")
        void shouldThrowException_WhenUsernameDoesNotExist() {
            // Given
            String username = "nonexistent";
            when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.empty());

            // When & Then
            UserObjectNotFoundException exception = assertThrows(
                    UserObjectNotFoundException.class,
                    () -> userService.findByUsername(username)
            );
            
            assertTrue(exception.getMessage().contains("User with username: nonexistent not found"));
            verify(userRepository).findByCredentialUsername(username);
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save and return user with valid data")
        void shouldSaveAndReturnUser_WithValidData() {
            // Given
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.save(testUserDto);

            // Then
            assertNotNull(result);
            assertEquals(testUserDto.getUserId(), result.getUserId());
            assertEquals(testUserDto.getFirstName(), result.getFirstName());
            assertEquals(testUserDto.getLastName(), result.getLastName());
            assertEquals(testUserDto.getEmail(), result.getEmail());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should save new user with different credentials")
        void shouldSaveNewUser_WithDifferentCredentials() {
            // Given
            UserDto newUser = UserDto.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .phone("0987654321")
                    .credentialDto(CredentialDto.builder()
                            .username("janesmith")
                            .password("password456")
                            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                            .isEnabled(true)
                            .isAccountNonExpired(true)
                            .isAccountNonLocked(true)
                            .isCredentialsNonExpired(true)
                            .build())
                    .build();

            User savedUser = User.builder()
                    .userId(2)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .phone("0987654321")
                    .credential(Credential.builder()
                            .credentialId(2)
                            .username("janesmith")
                            .password("password456")
                            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                            .isEnabled(true)
                            .isAccountNonExpired(true)
                            .isAccountNonLocked(true)
                            .isCredentialsNonExpired(true)
                            .build())
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            UserDto result = userService.save(newUser);

            // Then
            assertNotNull(result);
            assertEquals("Jane", result.getFirstName());
            assertEquals("Smith", result.getLastName());
            assertEquals("jane.smith@example.com", result.getEmail());
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update and return user")
        void shouldUpdateAndReturnUser() {
            // Given
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.update(testUserDto);

            // Then
            assertNotNull(result);
            assertEquals(testUserDto.getUserId(), result.getUserId());
            assertEquals(testUserDto.getFirstName(), result.getFirstName());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update user with new information")
        void shouldUpdateUser_WithNewInformation() {
            // Given
            UserDto updatedUser = UserDto.builder()
                    .userId(1)
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .email("john.updated@example.com")
                    .phone("1111111111")
                    .credentialDto(testCredentialDto)
                    .build();

            User savedUser = User.builder()
                    .userId(1)
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .email("john.updated@example.com")
                    .phone("1111111111")
                    .credential(testCredential)
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            UserDto result = userService.update(updatedUser);

            // Then
            assertNotNull(result);
            assertEquals("John Updated", result.getFirstName());
            assertEquals("Doe Updated", result.getLastName());
            assertEquals("john.updated@example.com", result.getEmail());
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            // Given
            Integer userId = 1;
            doNothing().when(userRepository).deleteById(userId);

            // When
            userService.deleteById(userId);

            // Then
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("Should handle delete operation without exceptions")
        void shouldHandleDeleteOperation_WithoutExceptions() {
            // Given
            Integer userId = 1;
            doNothing().when(userRepository).deleteById(userId);

            // When & Then
            assertDoesNotThrow(() -> userService.deleteById(userId));
            verify(userRepository).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("Authentication and Authorization Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should handle user with admin role")
        void shouldHandleUser_WithAdminRole() {
            // Given
            Credential adminCredential = Credential.builder()
                    .credentialId(2)
                    .username("admin")
                    .password("admin123")
                    .roleBasedAuthority(RoleBasedAuthority.ROLE_ADMIN)
                    .isEnabled(true)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .build();

            User adminUser = User.builder()
                    .userId(2)
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@example.com")
                    .phone("0000000000")
                    .credential(adminCredential)
                    .build();

            when(userRepository.findByCredentialUsername("admin")).thenReturn(Optional.of(adminUser));

            // When
            UserDto result = userService.findByUsername("admin");

            // Then
            assertNotNull(result);
            assertEquals(RoleBasedAuthority.ROLE_ADMIN, result.getCredentialDto().getRoleBasedAuthority());
            verify(userRepository).findByCredentialUsername("admin");
        }

        @Test
        @DisplayName("Should handle disabled user account")
        void shouldHandleDisabledUserAccount() {
            // Given
            Credential disabledCredential = Credential.builder()
                    .credentialId(3)
                    .username("disabled")
                    .password("password123")
                    .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                    .isEnabled(false)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .build();

            User disabledUser = User.builder()
                    .userId(3)
                    .firstName("Disabled")
                    .lastName("User")
                    .email("disabled@example.com")
                    .phone("3333333333")
                    .credential(disabledCredential)
                    .build();

            when(userRepository.findByCredentialUsername("disabled")).thenReturn(Optional.of(disabledUser));

            // When
            UserDto result = userService.findByUsername("disabled");

            // Then
            assertNotNull(result);
            assertFalse(result.getCredentialDto().getIsEnabled());
            verify(userRepository).findByCredentialUsername("disabled");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput_Gracefully() {
            // When & Then
            assertThrows(NullPointerException.class, () -> userService.save(null));
        }

        @Test
        @DisplayName("Should handle repository exceptions")
        void shouldHandleRepositoryExceptions() {
            // Given
            when(userRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> userService.findAll());
        }

        @Test
        @DisplayName("Should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            // Given
            Integer invalidUserId = -1;
            when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserObjectNotFoundException.class, () -> userService.findById(invalidUserId));
        }
    }
}
