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
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.OrderService;

/**
 * Comprehensive test suite for OrderResource
 * Tests all REST API endpoints with comprehensive scenarios
 */
@WebMvcTest(OrderResource.class)
@DisplayName("Order Resource Tests")
class OrderResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto testOrderDto;
    private CartDto testCartDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testOrderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(now)
                .orderDesc("Electronics Order")
                .orderFee(299.99)
                .build();

        testCartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .orderDtos(Collections.emptySet())
                .build();

        testOrderDto.setCartDto(testCartDto);
    }

    @Nested
    @DisplayName("GET /api/orders - Find All Orders")
    class FindAllTests {

        @Test
        @DisplayName("Should return all orders when service has data")
        void shouldReturnAllOrders_WhenServiceHasData() throws Exception {
            // Given
            List<OrderDto> orders = Arrays.asList(testOrderDto);
            when(orderService.findAll()).thenReturn(orders);

            // When & Then
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].orderId").value(1))
                    .andExpect(jsonPath("$.collection[0].orderDesc").value("Electronics Order"))
                    .andExpect(jsonPath("$.collection[0].orderFee").value(299.99));

            verify(orderService).findAll();
        }

        @Test
        @DisplayName("Should return empty collection when service has no data")
        void shouldReturnEmptyCollection_WhenServiceHasNoData() throws Exception {
            // Given
            when(orderService.findAll()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection").isEmpty());

            verify(orderService).findAll();
        }

        @Test
        @DisplayName("Should return multiple orders with different fees")
        void shouldReturnMultipleOrders_WithDifferentFees() throws Exception {
            // Given
            OrderDto expensiveOrder = OrderDto.builder()
                    .orderId(2)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Premium Order")
                    .orderFee(999.99)
                    .cartDto(testCartDto)
                    .build();

            List<OrderDto> orders = Arrays.asList(testOrderDto, expensiveOrder);
            when(orderService.findAll()).thenReturn(orders);

            // When & Then
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].orderDesc").value("Electronics Order"))
                    .andExpect(jsonPath("$.collection[1].orderDesc").value("Premium Order"))
                    .andExpect(jsonPath("$.collection[0].orderFee").value(299.99))
                    .andExpect(jsonPath("$.collection[1].orderFee").value(999.99));

            verify(orderService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id} - Find Order By ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should return order when it exists")
        void shouldReturnOrder_WhenItExists() throws Exception {
            // Given
            Integer orderId = 1;
            when(orderService.findById(orderId)).thenReturn(testOrderDto);

            // When & Then
            mockMvc.perform(get("/api/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.orderDesc").value("Electronics Order"))
                    .andExpect(jsonPath("$.orderFee").value(299.99));

            verify(orderService).findById(orderId);
        }

        @Test
        @DisplayName("Should return 500 when order does not exist")
        void shouldReturn500_WhenOrderDoesNotExist() throws Exception {
            // Given
            Integer orderId = 999;
            when(orderService.findById(orderId)).thenThrow(new RuntimeException("Order not found"));

            // When & Then
            mockMvc.perform(get("/api/orders/{orderId}", orderId))
                    .andExpect(status().is5xxServerError());

            verify(orderService).findById(orderId);
        }
    }

    @Nested
    @DisplayName("POST /api/orders - Create Order")
    class CreateTests {

        @Test
        @DisplayName("Should create order with valid data")
        void shouldCreateOrder_WithValidData() throws Exception {
            // Given
            when(orderService.save(any(OrderDto.class))).thenReturn(testOrderDto);

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testOrderDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.orderDesc").value("Electronics Order"))
                    .andExpect(jsonPath("$.orderFee").value(299.99));

            verify(orderService).save(any(OrderDto.class));
        }

        @Test
        @DisplayName("Should create order with different cart")
        void shouldCreateOrder_WithDifferentCart() throws Exception {
            // Given
            CartDto newCartDto = CartDto.builder()
                    .cartId(2)
                    .userId(2)
                    .orderDtos(Collections.emptySet())
                    .build();

            OrderDto newOrderDto = OrderDto.builder()
                    .orderDate(LocalDateTime.now())
                    .orderDesc("New Order")
                    .orderFee(199.99)
                    .cartDto(newCartDto)
                    .build();

            when(orderService.save(any(OrderDto.class))).thenReturn(newOrderDto);

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newOrderDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderDesc").value("New Order"))
                    .andExpect(jsonPath("$.orderFee").value(199.99));

            verify(orderService).save(any(OrderDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/orders - Update Order")
    class UpdateTests {

        @Test
        @DisplayName("Should update order with valid data")
        void shouldUpdateOrder_WithValidData() throws Exception {
            // Given
            when(orderService.update(any(OrderDto.class))).thenReturn(testOrderDto);

            // When & Then
            mockMvc.perform(put("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testOrderDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.orderDesc").value("Electronics Order"))
                    .andExpect(jsonPath("$.orderFee").value(299.99));

            verify(orderService).update(any(OrderDto.class));
        }

        @Test
        @DisplayName("Should update order by ID with new information")
        void shouldUpdateOrderById_WithNewInformation() throws Exception {
            // Given
            Integer orderId = 1;
            OrderDto updatedOrderDto = OrderDto.builder()
                    .orderId(1)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Updated Electronics Order")
                    .orderFee(399.99)
                    .cartDto(testCartDto)
                    .build();

            when(orderService.update(eq(orderId), any(OrderDto.class))).thenReturn(updatedOrderDto);

            // When & Then
            mockMvc.perform(put("/api/orders/{orderId}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedOrderDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.orderDesc").value("Updated Electronics Order"))
                    .andExpect(jsonPath("$.orderFee").value(399.99));

            verify(orderService).update(eq(orderId), any(OrderDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/orders/{id} - Delete Order")
    class DeleteTests {

        @Test
        @DisplayName("Should delete order by ID")
        void shouldDeleteOrderById() throws Exception {
            // Given
            Integer orderId = 1;
            doNothing().when(orderService).deleteById(orderId);

            // When & Then
            mockMvc.perform(delete("/api/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(orderService).deleteById(orderId);
        }

        @Test
        @DisplayName("Should handle delete operation for non-existent order")
        void shouldHandleDeleteOperation_ForNonExistentOrder() throws Exception {
            // Given
            Integer orderId = 999;
            doNothing().when(orderService).deleteById(orderId);

            // When & Then
            mockMvc.perform(delete("/api/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(orderService).deleteById(orderId);
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJson_InRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/orders")
                    .content(objectMapper.writeValueAsString(testOrderDto)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptions_Gracefully() throws Exception {
            // Given
            when(orderService.findAll()).thenThrow(new RuntimeException("Service unavailable"));

            // When & Then
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().is5xxServerError());

            verify(orderService).findAll();
        }
    }
}
