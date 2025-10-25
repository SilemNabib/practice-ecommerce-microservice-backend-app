package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;

/**
 * Comprehensive test suite for ProductService
 * Tests all CRUD operations, product management scenarios, and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;
    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/category.jpg")
                .build();

        testCategoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/category.jpg")
                .build();

        testProduct = Product.builder()
                .productId(1)
                .productTitle("Smartphone")
                .imageUrl("http://example.com/product.jpg")
                .category(testCategory)
                .sku("SMART-001")
                .priceUnit(599.99)
                .quantity(50)
                .build();

        testProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Smartphone")
                .imageUrl("http://example.com/product.jpg")
                .categoryDto(testCategoryDto)
                .sku("SMART-001")
                .priceUnit(599.99)
                .quantity(50)
                .build();
    }

    @Nested
    @DisplayName("Find All Operations")
    class FindAllTests {

        @Test
        @DisplayName("Should return all products when repository has data")
        void shouldReturnAllProducts_WhenRepositoryHasData() {
            // Given
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findAll()).thenReturn(products);

            // When
            List<ProductDto> result = productService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Smartphone", result.get(0).getProductTitle());
            assertEquals("SMART-001", result.get(0).getSku());
            assertEquals(599.99, result.get(0).getPriceUnit());
            assertEquals(50, result.get(0).getQuantity());
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when repository has no data")
        void shouldReturnEmptyList_WhenRepositoryHasNoData() {
            // Given
            when(productRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<ProductDto> result = productService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Should return multiple products with different categories")
        void shouldReturnMultipleProducts_WithDifferentCategories() {
            // Given
            Category clothingCategory = Category.builder()
                    .categoryId(2)
                    .categoryTitle("Clothing")
                    .imageUrl("http://example.com/clothing.jpg")
                    .build();

            Product clothingProduct = Product.builder()
                    .productId(2)
                    .productTitle("T-Shirt")
                    .imageUrl("http://example.com/tshirt.jpg")
                    .category(clothingCategory)
                    .sku("TSHIRT-001")
                    .priceUnit(29.99)
                    .quantity(100)
                    .build();

            List<Product> products = Arrays.asList(testProduct, clothingProduct);
            when(productRepository.findAll()).thenReturn(products);

            // When
            List<ProductDto> result = productService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Smartphone", result.get(0).getProductTitle());
            assertEquals("T-Shirt", result.get(1).getProductTitle());
            verify(productRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find By ID Operations")
    class FindByIdTests {

        @Test
        @DisplayName("Should return product when it exists")
        void shouldReturnProduct_WhenItExists() {
            // Given
            Integer productId = 1;
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            // When
            ProductDto result = productService.findById(productId);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            assertEquals("Smartphone", result.getProductTitle());
            assertEquals("SMART-001", result.getSku());
            assertEquals(599.99, result.getPriceUnit());
            assertEquals(50, result.getQuantity());
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Should throw exception when product does not exist")
        void shouldThrowException_WhenProductDoesNotExist() {
            // Given
            Integer productId = 999;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When & Then
            ProductNotFoundException exception = assertThrows(
                    ProductNotFoundException.class,
                    () -> productService.findById(productId)
            );
            
            assertTrue(exception.getMessage().contains("Product with id: 999 not found"));
            verify(productRepository).findById(productId);
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save and return product with valid data")
        void shouldSaveAndReturnProduct_WithValidData() {
            // Given
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductDto result = productService.save(testProductDto);

            // Then
            assertNotNull(result);
            assertEquals(testProductDto.getProductId(), result.getProductId());
            assertEquals(testProductDto.getProductTitle(), result.getProductTitle());
            assertEquals(testProductDto.getSku(), result.getSku());
            assertEquals(testProductDto.getPriceUnit(), result.getPriceUnit());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should save new product with different category")
        void shouldSaveNewProduct_WithDifferentCategory() {
            // Given
            CategoryDto newCategory = CategoryDto.builder()
                    .categoryId(2)
                    .categoryTitle("Books")
                    .imageUrl("http://example.com/books.jpg")
                    .build();

            ProductDto newProduct = ProductDto.builder()
                    .productTitle("Programming Book")
                    .sku("BOOK-001")
                    .categoryDto(newCategory)
                    .priceUnit(49.99)
                    .quantity(25)
                    .build();

            Category savedCategory = Category.builder()
                    .categoryId(2)
                    .categoryTitle("Books")
                    .imageUrl("http://example.com/books.jpg")
                    .build();

            Product savedProduct = Product.builder()
                    .productId(2)
                    .productTitle("Programming Book")
                    .sku("BOOK-001")
                    .category(savedCategory)
                    .priceUnit(49.99)
                    .quantity(25)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // When
            ProductDto result = productService.save(newProduct);

            // Then
            assertNotNull(result);
            assertEquals("Programming Book", result.getProductTitle());
            assertEquals("BOOK-001", result.getSku());
            assertEquals(49.99, result.getPriceUnit());
            assertEquals(25, result.getQuantity());
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update and return product")
        void shouldUpdateAndReturnProduct() {
            // Given
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductDto result = productService.update(testProductDto);

            // Then
            assertNotNull(result);
            assertEquals(testProductDto.getProductId(), result.getProductId());
            assertEquals(testProductDto.getProductTitle(), result.getProductTitle());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should update product with new information")
        void shouldUpdateProduct_WithNewInformation() {
            // Given
            ProductDto updatedProduct = ProductDto.builder()
                    .productId(1)
                    .productTitle("Updated Smartphone")
                    .sku("SMART-001-UPDATED")
                    .categoryDto(testCategoryDto)
                    .priceUnit(699.99)
                    .quantity(30)
                    .build();

            Product savedProduct = Product.builder()
                    .productId(1)
                    .productTitle("Updated Smartphone")
                    .sku("SMART-001-UPDATED")
                    .category(testCategory)
                    .priceUnit(699.99)
                    .quantity(30)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // When
            ProductDto result = productService.update(updatedProduct);

            // Then
            assertNotNull(result);
            assertEquals("Updated Smartphone", result.getProductTitle());
            assertEquals("SMART-001-UPDATED", result.getSku());
            assertEquals(699.99, result.getPriceUnit());
            assertEquals(30, result.getQuantity());
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete product by ID")
        void shouldDeleteProductById() {
            // Given
            Integer productId = 1;
            doNothing().when(productRepository).deleteById(productId);

            // When
            productService.deleteById(productId);

            // Then
            verify(productRepository).deleteById(productId);
        }

        @Test
        @DisplayName("Should handle delete operation without exceptions")
        void shouldHandleDeleteOperation_WithoutExceptions() {
            // Given
            Integer productId = 1;
            doNothing().when(productRepository).deleteById(productId);

            // When & Then
            assertDoesNotThrow(() -> productService.deleteById(productId));
            verify(productRepository).deleteById(productId);
        }
    }

    @Nested
    @DisplayName("Product Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should handle product with zero quantity")
        void shouldHandleProduct_WithZeroQuantity() {
            // Given
            ProductDto outOfStockProduct = ProductDto.builder()
                    .productTitle("Out of Stock Product")
                    .sku("OUT-001")
                    .categoryDto(testCategoryDto)
                    .priceUnit(99.99)
                    .quantity(0)
                    .build();

            Product savedProduct = Product.builder()
                    .productId(3)
                    .productTitle("Out of Stock Product")
                    .sku("OUT-001")
                    .category(testCategory)
                    .priceUnit(99.99)
                    .quantity(0)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // When
            ProductDto result = productService.save(outOfStockProduct);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getQuantity());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should handle product with high price")
        void shouldHandleProduct_WithHighPrice() {
            // Given
            ProductDto expensiveProduct = ProductDto.builder()
                    .productTitle("Luxury Watch")
                    .sku("LUX-001")
                    .categoryDto(testCategoryDto)
                    .priceUnit(9999.99)
                    .quantity(5)
                    .build();

            Product savedProduct = Product.builder()
                    .productId(4)
                    .productTitle("Luxury Watch")
                    .sku("LUX-001")
                    .category(testCategory)
                    .priceUnit(9999.99)
                    .quantity(5)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // When
            ProductDto result = productService.save(expensiveProduct);

            // Then
            assertNotNull(result);
            assertEquals(9999.99, result.getPriceUnit());
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput_Gracefully() {
            // When & Then
            assertThrows(NullPointerException.class, () -> productService.save(null));
        }

        @Test
        @DisplayName("Should handle repository exceptions")
        void shouldHandleRepositoryExceptions() {
            // Given
            when(productRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> productService.findAll());
        }

        @Test
        @DisplayName("Should handle invalid product ID")
        void shouldHandleInvalidProductId() {
            // Given
            Integer invalidProductId = -1;
            when(productRepository.findById(invalidProductId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ProductNotFoundException.class, () -> productService.findById(invalidProductId));
        }

        @Test
        @DisplayName("Should handle product with negative price")
        void shouldHandleProduct_WithNegativePrice() {
            // Given
            ProductDto invalidProduct = ProductDto.builder()
                    .productTitle("Invalid Product")
                    .sku("INVALID-001")
                    .categoryDto(testCategoryDto)
                    .priceUnit(-10.0)
                    .quantity(5)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductDto result = productService.save(invalidProduct);

            // Then
            assertNotNull(result);
            // Note: Business validation should be handled in the service layer
            verify(productRepository).save(any(Product.class));
        }
    }
}
