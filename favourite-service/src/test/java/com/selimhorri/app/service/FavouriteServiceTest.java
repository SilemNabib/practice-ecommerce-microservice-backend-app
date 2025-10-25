package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.impl.FavouriteServiceImpl;

/**
 * Comprehensive test suite for FavouriteService
 * Tests all CRUD operations, edge cases, and business logic scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Favourite Service Tests")
class FavouriteServiceTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private Favourite testFavourite;
    private FavouriteDto testFavouriteDto;
    private FavouriteId testFavouriteId;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        testFavouriteId = new FavouriteId(1, 1, now);
        
        testFavourite = Favourite.builder()
                .userId(1)
                .productId(1)
                .likeDate(now)
                .build();

        testFavouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .likeDate(now)
                .build();
    }

    @Nested
    @DisplayName("Find All Operations")
    class FindAllTests {

        @Test
        @DisplayName("Should return all favourites when repository has data")
        void shouldReturnAllFavourites_WhenRepositoryHasData() {
            // Given
            List<Favourite> favourites = Arrays.asList(testFavourite);
            when(favouriteRepository.findAll()).thenReturn(favourites);

            // When
            List<FavouriteDto> result = favouriteService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Integer.valueOf(1), result.get(0).getUserId());
            assertEquals(Integer.valueOf(1), result.get(0).getProductId());
            verify(favouriteRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when repository has no data")
        void shouldReturnEmptyList_WhenRepositoryHasNoData() {
            // Given
            when(favouriteRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<FavouriteDto> result = favouriteService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(favouriteRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find By ID Operations")
    class FindByIdTests {

        @Test
        @DisplayName("Should return favourite when it exists")
        void shouldReturnFavourite_WhenItExists() {
            // Given
            when(favouriteRepository.findById(testFavouriteId)).thenReturn(Optional.of(testFavourite));

            // When
            FavouriteDto result = favouriteService.findById(testFavouriteId);

            // Then
            assertNotNull(result);
            assertEquals(testFavouriteId.getUserId(), result.getUserId());
            assertEquals(testFavouriteId.getProductId(), result.getProductId());
            verify(favouriteRepository).findById(testFavouriteId);
        }

        @Test
        @DisplayName("Should throw exception when favourite does not exist")
        void shouldThrowException_WhenFavouriteDoesNotExist() {
            // Given
            FavouriteId nonExistentId = new FavouriteId(999, 999, LocalDateTime.now());
            when(favouriteRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            FavouriteNotFoundException exception = assertThrows(
                    FavouriteNotFoundException.class,
                    () -> favouriteService.findById(nonExistentId)
            );
            
            assertTrue(exception.getMessage().contains("Favourite with id: [" + nonExistentId + "] not found!"));
            verify(favouriteRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save and return favourite with valid data")
        void shouldSaveAndReturnFavourite_WithValidData() {
            // Given
            when(favouriteRepository.save(any(Favourite.class))).thenReturn(testFavourite);

            // When
            FavouriteDto result = favouriteService.save(testFavouriteDto);

            // Then
            assertNotNull(result);
            assertEquals(testFavouriteDto.getUserId(), result.getUserId());
            assertEquals(testFavouriteDto.getProductId(), result.getProductId());
            verify(favouriteRepository).save(any(Favourite.class));
        }

        @Test
        @DisplayName("Should save new favourite with different user and product")
        void shouldSaveNewFavourite_WithDifferentUserAndProduct() {
            // Given
            FavouriteDto newFavourite = FavouriteDto.builder()
                    .userId(2)
                    .productId(2)
                    .likeDate(LocalDateTime.now())
                    .build();

            Favourite savedFavourite = Favourite.builder()
                    .userId(2)
                    .productId(2)
                    .likeDate(LocalDateTime.now())
                    .build();

            when(favouriteRepository.save(any(Favourite.class))).thenReturn(savedFavourite);

            // When
            FavouriteDto result = favouriteService.save(newFavourite);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(2), result.getUserId());
            assertEquals(Integer.valueOf(2), result.getProductId());
            verify(favouriteRepository).save(any(Favourite.class));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update and return favourite")
        void shouldUpdateAndReturnFavourite() {
            // Given
            when(favouriteRepository.save(any(Favourite.class))).thenReturn(testFavourite);

            // When
            FavouriteDto result = favouriteService.update(testFavouriteDto);

            // Then
            assertNotNull(result);
            assertEquals(testFavouriteDto.getUserId(), result.getUserId());
            verify(favouriteRepository).save(any(Favourite.class));
        }

        @Test
        @DisplayName("Should update favourite with new product")
        void shouldUpdateFavourite_WithNewProduct() {
            // Given
            FavouriteDto updatedFavourite = FavouriteDto.builder()
                    .userId(1)
                    .productId(3)
                    .likeDate(LocalDateTime.now())
                    .build();

            Favourite savedFavourite = Favourite.builder()
                    .userId(1)
                    .productId(3)
                    .likeDate(LocalDateTime.now())
                    .build();

            when(favouriteRepository.save(any(Favourite.class))).thenReturn(savedFavourite);

            // When
            FavouriteDto result = favouriteService.update(updatedFavourite);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(3), result.getProductId());
            verify(favouriteRepository).save(any(Favourite.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete favourite by ID")
        void shouldDeleteFavouriteById() {
            // Given
            doNothing().when(favouriteRepository).deleteById(testFavouriteId);

            // When
            favouriteService.deleteById(testFavouriteId);

            // Then
            verify(favouriteRepository).deleteById(testFavouriteId);
        }

        @Test
        @DisplayName("Should handle delete operation without exceptions")
        void shouldHandleDeleteOperation_WithoutExceptions() {
            // Given
            FavouriteId favouriteId = new FavouriteId(1, 1, LocalDateTime.now());
            doNothing().when(favouriteRepository).deleteById(favouriteId);

            // When & Then
            assertDoesNotThrow(() -> favouriteService.deleteById(favouriteId));
            verify(favouriteRepository).deleteById(favouriteId);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput_Gracefully() {
            // When & Then
            assertThrows(NullPointerException.class, () -> favouriteService.save(null));
        }

        @Test
        @DisplayName("Should handle repository exceptions")
        void shouldHandleRepositoryExceptions() {
            // Given
            when(favouriteRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> favouriteService.findAll());
        }
    }
}
