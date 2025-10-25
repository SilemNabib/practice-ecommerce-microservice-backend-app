package com.selimhorri.app.resource;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.OrderItemService;

/**
 * Comprehensive test suite for OrderItemResource
 * Tests all REST API endpoints with comprehensive scenarios
 */
@WebMvcTest(OrderItemResource.class)
@DisplayName("Order Item Resource Tests")
class OrderItemResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderItemService orderItemService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderItemDto testOrderItemDto;
    private ProductDto testProductDto;
    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .imageUrl("http://example.com/image.jpg")
                .sku("SKU123")
                .priceUnit(99.99)
                .quantity(10)
                .build();

        testOrderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(199.98)
                .build();

        testOrderItemDto = OrderItemDto.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(2)
                .productDto(testProductDto)
                .orderDto(testOrderDto)
                .build();
    }

    @Nested
    @DisplayName("GET /api/shippings - Find All Order Items")
    class FindAllTests {

        @Test
        @DisplayName("Should return all order items when service has data")
        void shouldReturnAllOrderItems_WhenServiceHasData() throws Exception {
            // Given
            List<OrderItemDto> orderItems = Arrays.asList(testOrderItemDto);
            when(orderItemService.findAll()).thenReturn(orderItems);

            // When & Then
            mockMvc.perform(get("/api/shippings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].orderedQuantity").value(2))
                    .andExpect(jsonPath("$.collection[0].productId").value(1))
                    .andExpect(jsonPath("$.collection[0].orderId").value(1));

            verify(orderItemService).findAll();
        }

        @Test
        @DisplayName("Should return empty collection when service has no data")
        void shouldReturnEmptyCollection_WhenServiceHasNoData() throws Exception {
            // Given
            when(orderItemService.findAll()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/shippings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection").isEmpty());

            verify(orderItemService).findAll();
        }

        @Test
        @DisplayName("Should return multiple order items with different quantities")
        void shouldReturnMultipleOrderItems_WithDifferentQuantities() throws Exception {
            // Given
            OrderItemDto secondOrderItem = OrderItemDto.builder()
                    .productId(2)
                    .orderId(1)
                    .orderedQuantity(5)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            List<OrderItemDto> orderItems = Arrays.asList(testOrderItemDto, secondOrderItem);
            when(orderItemService.findAll()).thenReturn(orderItems);

            // When & Then
            mockMvc.perform(get("/api/shippings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].orderedQuantity").value(2))
                    .andExpect(jsonPath("$.collection[1].orderedQuantity").value(5))
                    .andExpect(jsonPath("$.collection[0].productId").value(1))
                    .andExpect(jsonPath("$.collection[1].productId").value(2));

            verify(orderItemService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/shippings/{orderId}/{productId} - Find Order Item By ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should return order item when it exists")
        void shouldReturnOrderItem_WhenItExists() throws Exception {
            // Given
            when(orderItemService.findById(any(OrderItemId.class))).thenReturn(testOrderItemDto);

            // When & Then
            mockMvc.perform(get("/api/shippings/{orderId}/{productId}", "1", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderedQuantity").value(2))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.orderId").value(1));

            verify(orderItemService).findById(any(OrderItemId.class));
        }

        @Test
        @DisplayName("Should return 500 when order item does not exist")
        void shouldReturn500_WhenOrderItemDoesNotExist() throws Exception {
            // Given
            when(orderItemService.findById(any(OrderItemId.class)))
                    .thenThrow(new RuntimeException("Order item not found"));

            // When & Then
            mockMvc.perform(get("/api/shippings/{orderId}/{productId}", "999", "999"))
                    .andExpect(status().is5xxServerError());

            verify(orderItemService).findById(any(OrderItemId.class));
        }
    }

    @Nested
    @DisplayName("POST /api/shippings - Create Order Item")
    class CreateTests {

        @Test
        @DisplayName("Should create order item with valid data")
        void shouldCreateOrderItem_WithValidData() throws Exception {
            // Given
            when(orderItemService.save(any(OrderItemDto.class))).thenReturn(testOrderItemDto);

            // When & Then
            mockMvc.perform(post("/api/shippings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testOrderItemDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderedQuantity").value(2))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.orderId").value(1));

            verify(orderItemService).save(any(OrderItemDto.class));
        }

        @Test
        @DisplayName("Should create order item with different product")
        void shouldCreateOrderItem_WithDifferentProduct() throws Exception {
            // Given
            OrderItemDto newOrderItem = OrderItemDto.builder()
                    .productId(2)
                    .orderId(2)
                    .orderedQuantity(3)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            OrderItemDto savedOrderItem = OrderItemDto.builder()
                    .productId(2)
                    .orderId(2)
                    .orderedQuantity(3)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            when(orderItemService.save(any(OrderItemDto.class))).thenReturn(savedOrderItem);

            // When & Then
            mockMvc.perform(post("/api/shippings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newOrderItem)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderedQuantity").value(3))
                    .andExpect(jsonPath("$.productId").value(2))
                    .andExpect(jsonPath("$.orderId").value(2));

            verify(orderItemService).save(any(OrderItemDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/shippings - Update Order Item")
    class UpdateTests {

        @Test
        @DisplayName("Should update order item with valid data")
        void shouldUpdateOrderItem_WithValidData() throws Exception {
            // Given
            when(orderItemService.update(any(OrderItemDto.class))).thenReturn(testOrderItemDto);

            // When & Then
            mockMvc.perform(put("/api/shippings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testOrderItemDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderedQuantity").value(2))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.orderId").value(1));

            verify(orderItemService).update(any(OrderItemDto.class));
        }

        @Test
        @DisplayName("Should update order item with new quantity")
        void shouldUpdateOrderItem_WithNewQuantity() throws Exception {
            // Given
            OrderItemDto updatedOrderItem = OrderItemDto.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(4)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            when(orderItemService.update(any(OrderItemDto.class))).thenReturn(updatedOrderItem);

            // When & Then
            mockMvc.perform(put("/api/shippings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedOrderItem)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderedQuantity").value(4))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.orderId").value(1));

            verify(orderItemService).update(any(OrderItemDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/shippings/{orderId}/{productId} - Delete Order Item")
    class DeleteTests {

        @Test
        @DisplayName("Should delete order item by ID")
        void shouldDeleteOrderItemById() throws Exception {
            // Given
            doNothing().when(orderItemService).deleteById(any(OrderItemId.class));

            // When & Then
            mockMvc.perform(delete("/api/shippings/{orderId}/{productId}", "1", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(orderItemService).deleteById(any(OrderItemId.class));
        }

        @Test
        @DisplayName("Should handle delete operation for non-existent order item")
        void shouldHandleDeleteOperation_ForNonExistentOrderItem() throws Exception {
            // Given
            doNothing().when(orderItemService).deleteById(any(OrderItemId.class));

            // When & Then
            mockMvc.perform(delete("/api/shippings/{orderId}/{productId}", "999", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(orderItemService).deleteById(any(OrderItemId.class));
        }
    }

    @Nested
    @DisplayName("Shipping Business Logic Tests")
    class ShippingBusinessLogicTests {

        @Test
        @DisplayName("Should handle order item with zero quantity")
        void shouldHandleOrderItem_WithZeroQuantity() throws Exception {
            // Given
            OrderItemDto zeroQuantityItem = OrderItemDto.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(0)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            when(orderItemService.save(any(OrderItemDto.class))).thenReturn(zeroQuantityItem);

            // When & Then
            mockMvc.perform(post("/api/shippings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(zeroQuantityItem)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderedQuantity").value(0));

            verify(orderItemService).save(any(OrderItemDto.class));
        }

        @Test
        @DisplayName("Should handle order item with high quantity")
        void shouldHandleOrderItem_WithHighQuantity() throws Exception {
            // Given
            OrderItemDto highQuantityItem = OrderItemDto.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(1000)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            when(orderItemService.save(any(OrderItemDto.class))).thenReturn(highQuantityItem);

            // When & Then
            mockMvc.perform(post("/api/shippings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(highQuantityItem)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderedQuantity").value(1000));

            verify(orderItemService).save(any(OrderItemDto.class));
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJson_InRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/shippings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/shippings")
                    .content(objectMapper.writeValueAsString(testOrderItemDto)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptions_Gracefully() throws Exception {
            // Given
            when(orderItemService.findAll()).thenThrow(new RuntimeException("Service unavailable"));

            // When & Then
            mockMvc.perform(get("/api/shippings"))
                    .andExpect(status().is5xxServerError());

            verify(orderItemService).findAll();
        }
    }
}
