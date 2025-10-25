package com.selimhorri.app.resource;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.ProductService;

/**
 * Comprehensive test suite for ProductResource
 * Tests all REST API endpoints with comprehensive scenarios
 */
@WebMvcTest(ProductResource.class)
@DisplayName("Product Resource Tests")
class ProductResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Smartphone")
                .imageUrl("http://example.com/smartphone.jpg")
                .sku("SMART-001")
                .priceUnit(599.99)
                .quantity(50)
                .categoryDto(CategoryDto.builder()
                        .categoryId(1)
                        .categoryTitle("Electronics")
                        .imageUrl("http://example.com/electronics.jpg")
                        .build())
                .build();
    }

    @Nested
    @DisplayName("GET /api/products - Find All Products")
    class FindAllTests {

        @Test
        @DisplayName("Should return all products when service has data")
        void shouldReturnAllProducts_WhenServiceHasData() throws Exception {
            // Given
            List<ProductDto> products = Arrays.asList(testProductDto);
            when(productService.findAll()).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].productId").value(1))
                    .andExpect(jsonPath("$.collection[0].productTitle").value("Smartphone"))
                    .andExpect(jsonPath("$.collection[0].sku").value("SMART-001"))
                    .andExpect(jsonPath("$.collection[0].priceUnit").value(599.99))
                    .andExpect(jsonPath("$.collection[0].quantity").value(50));

            verify(productService).findAll();
        }

        @Test
        @DisplayName("Should return empty collection when service has no data")
        void shouldReturnEmptyCollection_WhenServiceHasNoData() throws Exception {
            // Given
            when(productService.findAll()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection").isEmpty());

            verify(productService).findAll();
        }

        @Test
        @DisplayName("Should return multiple products with different categories")
        void shouldReturnMultipleProducts_WithDifferentCategories() throws Exception {
            // Given
            CategoryDto clothingCategory = CategoryDto.builder()
                    .categoryId(2)
                    .categoryTitle("Clothing")
                    .imageUrl("http://example.com/clothing.jpg")
                    .build();

            ProductDto clothingProduct = ProductDto.builder()
                    .productId(2)
                    .productTitle("T-Shirt")
                    .imageUrl("http://example.com/tshirt.jpg")
                    .sku("TSHIRT-001")
                    .priceUnit(29.99)
                    .quantity(100)
                    .categoryDto(clothingCategory)
                    .build();

            List<ProductDto> products = Arrays.asList(testProductDto, clothingProduct);
            when(productService.findAll()).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].productTitle").value("Smartphone"))
                    .andExpect(jsonPath("$.collection[1].productTitle").value("T-Shirt"))
                    .andExpect(jsonPath("$.collection[0].categoryDto.categoryTitle").value("Electronics"))
                    .andExpect(jsonPath("$.collection[1].categoryDto.categoryTitle").value("Clothing"));

            verify(productService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id} - Find Product By ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should return product when it exists")
        void shouldReturnProduct_WhenItExists() throws Exception {
            // Given
            Integer productId = 1;
            when(productService.findById(productId)).thenReturn(testProductDto);

            // When & Then
            mockMvc.perform(get("/api/products/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.productTitle").value("Smartphone"))
                    .andExpect(jsonPath("$.sku").value("SMART-001"))
                    .andExpect(jsonPath("$.priceUnit").value(599.99))
                    .andExpect(jsonPath("$.quantity").value(50));

            verify(productService).findById(productId);
        }

        @Test
        @DisplayName("Should return 500 when product does not exist")
        void shouldReturn500_WhenProductDoesNotExist() throws Exception {
            // Given
            Integer productId = 999;
            when(productService.findById(productId)).thenThrow(new RuntimeException("Product not found"));

            // When & Then
            mockMvc.perform(get("/api/products/{productId}", productId))
                    .andExpect(status().is5xxServerError());

            verify(productService).findById(productId);
        }
    }

    @Nested
    @DisplayName("POST /api/products - Create Product")
    class CreateTests {

        @Test
        @DisplayName("Should create product with valid data")
        void shouldCreateProduct_WithValidData() throws Exception {
            // Given
            when(productService.save(any(ProductDto.class))).thenReturn(testProductDto);

            // When & Then
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testProductDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.productTitle").value("Smartphone"))
                    .andExpect(jsonPath("$.sku").value("SMART-001"))
                    .andExpect(jsonPath("$.priceUnit").value(599.99));

            verify(productService).save(any(ProductDto.class));
        }

        @Test
        @DisplayName("Should create product with different category")
        void shouldCreateProduct_WithDifferentCategory() throws Exception {
            // Given
            CategoryDto bookCategory = CategoryDto.builder()
                    .categoryId(2)
                    .categoryTitle("Books")
                    .imageUrl("http://example.com/books.jpg")
                    .build();

            ProductDto newProduct = ProductDto.builder()
                    .productTitle("Programming Book")
                    .sku("BOOK-001")
                    .priceUnit(49.99)
                    .quantity(25)
                    .categoryDto(bookCategory)
                    .build();

            ProductDto savedProduct = ProductDto.builder()
                    .productId(2)
                    .productTitle("Programming Book")
                    .sku("BOOK-001")
                    .priceUnit(49.99)
                    .quantity(25)
                    .categoryDto(bookCategory)
                    .build();

            when(productService.save(any(ProductDto.class))).thenReturn(savedProduct);

            // When & Then
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(2))
                    .andExpect(jsonPath("$.productTitle").value("Programming Book"))
                    .andExpect(jsonPath("$.sku").value("BOOK-001"))
                    .andExpect(jsonPath("$.priceUnit").value(49.99));

            verify(productService).save(any(ProductDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/products - Update Product")
    class UpdateTests {

        @Test
        @DisplayName("Should update product with valid data")
        void shouldUpdateProduct_WithValidData() throws Exception {
            // Given
            when(productService.update(any(ProductDto.class))).thenReturn(testProductDto);

            // When & Then
            mockMvc.perform(put("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testProductDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.productTitle").value("Smartphone"))
                    .andExpect(jsonPath("$.sku").value("SMART-001"));

            verify(productService).update(any(ProductDto.class));
        }

        @Test
        @DisplayName("Should update product with new information")
        void shouldUpdateProduct_WithNewInformation() throws Exception {
            // Given
            ProductDto updatedProduct = ProductDto.builder()
                    .productId(1)
                    .productTitle("Updated Smartphone")
                    .sku("SMART-001-UPDATED")
                    .priceUnit(699.99)
                    .quantity(30)
                    .categoryDto(testProductDto.getCategoryDto())
                    .build();

            when(productService.update(any(ProductDto.class))).thenReturn(updatedProduct);

            // When & Then
            mockMvc.perform(put("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.productTitle").value("Updated Smartphone"))
                    .andExpect(jsonPath("$.sku").value("SMART-001-UPDATED"))
                    .andExpect(jsonPath("$.priceUnit").value(699.99));

            verify(productService).update(any(ProductDto.class));
        }

        @Test
        @DisplayName("Should update product by ID")
        void shouldUpdateProductById() throws Exception {
            // Given
            Integer productId = 1;
            when(productService.update(eq(productId), any(ProductDto.class))).thenReturn(testProductDto);

            // When & Then
            mockMvc.perform(put("/api/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testProductDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.productTitle").value("Smartphone"));

            verify(productService).update(eq(productId), any(ProductDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id} - Delete Product")
    class DeleteTests {

        @Test
        @DisplayName("Should delete product by ID")
        void shouldDeleteProductById() throws Exception {
            // Given
            Integer productId = 1;
            doNothing().when(productService).deleteById(productId);

            // When & Then
            mockMvc.perform(delete("/api/products/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(productService).deleteById(productId);
        }

        @Test
        @DisplayName("Should handle delete operation for non-existent product")
        void shouldHandleDeleteOperation_ForNonExistentProduct() throws Exception {
            // Given
            Integer productId = 999;
            doNothing().when(productService).deleteById(productId);

            // When & Then
            mockMvc.perform(delete("/api/products/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(productService).deleteById(productId);
        }
    }

    @Nested
    @DisplayName("Product Business Logic Tests")
    class ProductBusinessLogicTests {

        @Test
        @DisplayName("Should handle product with zero quantity")
        void shouldHandleProduct_WithZeroQuantity() throws Exception {
            // Given
            ProductDto outOfStockProduct = ProductDto.builder()
                    .productTitle("Out of Stock Product")
                    .sku("OUT-001")
                    .priceUnit(99.99)
                    .quantity(0)
                    .categoryDto(testProductDto.getCategoryDto())
                    .build();

            ProductDto savedProduct = ProductDto.builder()
                    .productId(3)
                    .productTitle("Out of Stock Product")
                    .sku("OUT-001")
                    .priceUnit(99.99)
                    .quantity(0)
                    .categoryDto(testProductDto.getCategoryDto())
                    .build();

            when(productService.save(any(ProductDto.class))).thenReturn(savedProduct);

            // When & Then
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(outOfStockProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(0));

            verify(productService).save(any(ProductDto.class));
        }

        @Test
        @DisplayName("Should handle product with high price")
        void shouldHandleProduct_WithHighPrice() throws Exception {
            // Given
            ProductDto expensiveProduct = ProductDto.builder()
                    .productTitle("Luxury Watch")
                    .sku("LUX-001")
                    .priceUnit(9999.99)
                    .quantity(5)
                    .categoryDto(testProductDto.getCategoryDto())
                    .build();

            ProductDto savedProduct = ProductDto.builder()
                    .productId(4)
                    .productTitle("Luxury Watch")
                    .sku("LUX-001")
                    .priceUnit(9999.99)
                    .quantity(5)
                    .categoryDto(testProductDto.getCategoryDto())
                    .build();

            when(productService.save(any(ProductDto.class))).thenReturn(savedProduct);

            // When & Then
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expensiveProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceUnit").value(9999.99));

            verify(productService).save(any(ProductDto.class));
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJson_InRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/products")
                    .content(objectMapper.writeValueAsString(testProductDto)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptions_Gracefully() throws Exception {
            // Given
            when(productService.findAll()).thenThrow(new RuntimeException("Service unavailable"));

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().is5xxServerError());

            verify(productService).findAll();
        }
    }
}
