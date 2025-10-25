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
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.PaymentService;

/**
 * Comprehensive test suite for PaymentResource
 * Tests all REST API endpoints with comprehensive scenarios
 */
@WebMvcTest(PaymentResource.class)
@DisplayName("Payment Resource Tests")
class PaymentResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentDto testPaymentDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testPaymentDto = PaymentDto.builder()
                .paymentId(1)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .orderDto(OrderDto.builder()
                        .orderId(1)
                        .orderFee(299.99)
                        .orderDate(now)
                        .orderDesc("Electronics Order")
                        .build())
                .isPayed(false)
                .build();
    }

    @Nested
    @DisplayName("GET /api/payments - Find All Payments")
    class FindAllTests {

        @Test
        @DisplayName("Should return all payments when service has data")
        void shouldReturnAllPayments_WhenServiceHasData() throws Exception {
            // Given
            List<PaymentDto> payments = Arrays.asList(testPaymentDto);
            when(paymentService.findAll()).thenReturn(payments);

            // When & Then
            mockMvc.perform(get("/api/payments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].paymentId").value(1))
                    .andExpect(jsonPath("$.collection[0].paymentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.collection[0].isPayed").value(false));

            verify(paymentService).findAll();
        }

        @Test
        @DisplayName("Should return empty collection when service has no data")
        void shouldReturnEmptyCollection_WhenServiceHasNoData() throws Exception {
            // Given
            when(paymentService.findAll()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/payments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection").isEmpty());

            verify(paymentService).findAll();
        }

        @Test
        @DisplayName("Should return multiple payments with different statuses")
        void shouldReturnMultiplePayments_WithDifferentStatuses() throws Exception {
            // Given
            PaymentDto completedPayment = PaymentDto.builder()
                    .paymentId(2)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderDto(OrderDto.builder()
                            .orderId(2)
                            .orderFee(499.99)
                            .orderDate(LocalDateTime.now())
                            .orderDesc("Premium Order")
                            .build())
                    .isPayed(true)
                    .build();

            List<PaymentDto> payments = Arrays.asList(testPaymentDto, completedPayment);
            when(paymentService.findAll()).thenReturn(payments);

            // When & Then
            mockMvc.perform(get("/api/payments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.collection").isArray())
                    .andExpect(jsonPath("$.collection[0].paymentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.collection[1].paymentStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.collection[0].isPayed").value(false))
                    .andExpect(jsonPath("$.collection[1].isPayed").value(true));

            verify(paymentService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/payments/{id} - Find Payment By ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should return payment when it exists")
        void shouldReturnPayment_WhenItExists() throws Exception {
            // Given
            Integer paymentId = 1;
            when(paymentService.findById(paymentId)).thenReturn(testPaymentDto);

            // When & Then
            mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1))
                    .andExpect(jsonPath("$.paymentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.isPayed").value(false));

            verify(paymentService).findById(paymentId);
        }

        @Test
        @DisplayName("Should return 500 when payment does not exist")
        void shouldReturn500_WhenPaymentDoesNotExist() throws Exception {
            // Given
            Integer paymentId = 999;
            when(paymentService.findById(paymentId)).thenThrow(new RuntimeException("Payment not found"));

            // When & Then
            mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
                    .andExpect(status().is5xxServerError());

            verify(paymentService).findById(paymentId);
        }
    }

    @Nested
    @DisplayName("POST /api/payments - Create Payment")
    class CreateTests {

        @Test
        @DisplayName("Should create payment with valid data")
        void shouldCreatePayment_WithValidData() throws Exception {
            // Given
            when(paymentService.save(any(PaymentDto.class))).thenReturn(testPaymentDto);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testPaymentDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1))
                    .andExpect(jsonPath("$.paymentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.isPayed").value(false));

            verify(paymentService).save(any(PaymentDto.class));
        }

        @Test
        @DisplayName("Should create payment with different order")
        void shouldCreatePayment_WithDifferentOrder() throws Exception {
            // Given
            OrderDto newOrderDto = OrderDto.builder()
                    .orderId(2)
                    .orderFee(199.99)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("New Order")
                    .build();

            PaymentDto newPaymentDto = PaymentDto.builder()
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderDto(newOrderDto)
                    .isPayed(false)
                    .build();

            PaymentDto savedPayment = PaymentDto.builder()
                    .paymentId(2)
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderDto(newOrderDto)
                    .isPayed(false)
                    .build();

            when(paymentService.save(any(PaymentDto.class))).thenReturn(savedPayment);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newPaymentDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(2))
                    .andExpect(jsonPath("$.paymentStatus").value("NOT_STARTED"))
                    .andExpect(jsonPath("$.isPayed").value(false));

            verify(paymentService).save(any(PaymentDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/payments - Update Payment")
    class UpdateTests {

        @Test
        @DisplayName("Should update payment with valid data")
        void shouldUpdatePayment_WithValidData() throws Exception {
            // Given
            when(paymentService.update(any(PaymentDto.class))).thenReturn(testPaymentDto);

            // When & Then
            mockMvc.perform(put("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testPaymentDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1))
                    .andExpect(jsonPath("$.paymentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.isPayed").value(false));

            verify(paymentService).update(any(PaymentDto.class));
        }

        @Test
        @DisplayName("Should update payment status to completed")
        void shouldUpdatePaymentStatus_ToCompleted() throws Exception {
            // Given
            PaymentDto completedPaymentDto = PaymentDto.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderDto(testPaymentDto.getOrderDto())
                    .isPayed(true)
                    .build();

            when(paymentService.update(any(PaymentDto.class))).thenReturn(completedPaymentDto);

            // When & Then
            mockMvc.perform(put("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(completedPaymentDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1))
                    .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.isPayed").value(true));

            verify(paymentService).update(any(PaymentDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/payments/{id} - Delete Payment")
    class DeleteTests {

        @Test
        @DisplayName("Should delete payment by ID")
        void shouldDeletePaymentById() throws Exception {
            // Given
            Integer paymentId = 1;
            doNothing().when(paymentService).deleteById(paymentId);

            // When & Then
            mockMvc.perform(delete("/api/payments/{paymentId}", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(paymentService).deleteById(paymentId);
        }

        @Test
        @DisplayName("Should handle delete operation for non-existent payment")
        void shouldHandleDeleteOperation_ForNonExistentPayment() throws Exception {
            // Given
            Integer paymentId = 999;
            doNothing().when(paymentService).deleteById(paymentId);

            // When & Then
            mockMvc.perform(delete("/api/payments/{paymentId}", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(paymentService).deleteById(paymentId);
        }
    }

    @Nested
    @DisplayName("Payment Processing Tests")
    class PaymentProcessingTests {

        @Test
        @DisplayName("Should handle payment processing workflow")
        void shouldHandlePaymentProcessingWorkflow() throws Exception {
            // Given
            PaymentDto notStartedPayment = PaymentDto.builder()
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderDto(testPaymentDto.getOrderDto())
                    .isPayed(false)
                    .build();

            PaymentDto processedPayment = PaymentDto.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.IN_PROGRESS)
                    .orderDto(testPaymentDto.getOrderDto())
                    .isPayed(false)
                    .build();

            when(paymentService.save(any(PaymentDto.class))).thenReturn(processedPayment);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notStartedPayment)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.isPayed").value(false));

            verify(paymentService).save(any(PaymentDto.class));
        }

        @Test
        @DisplayName("Should handle payment not started scenario")
        void shouldHandlePaymentNotStartedScenario() throws Exception {
            // Given
            PaymentDto notStartedPayment = PaymentDto.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderDto(testPaymentDto.getOrderDto())
                    .isPayed(false)
                    .build();

            when(paymentService.update(any(PaymentDto.class))).thenReturn(notStartedPayment);

            // When & Then
            mockMvc.perform(put("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notStartedPayment)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentStatus").value("NOT_STARTED"))
                    .andExpect(jsonPath("$.isPayed").value(false));

            verify(paymentService).update(any(PaymentDto.class));
        }

        @Test
        @DisplayName("Should handle successful payment completion")
        void shouldHandleSuccessfulPaymentCompletion() throws Exception {
            // Given
            PaymentDto successfulPayment = PaymentDto.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderDto(testPaymentDto.getOrderDto())
                    .isPayed(true)
                    .build();

            when(paymentService.update(any(PaymentDto.class))).thenReturn(successfulPayment);

            // When & Then
            mockMvc.perform(put("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(successfulPayment)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.isPayed").value(true));

            verify(paymentService).update(any(PaymentDto.class));
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJson_InRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/payments")
                    .content(objectMapper.writeValueAsString(testPaymentDto)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptions_Gracefully() throws Exception {
            // Given
            when(paymentService.findAll()).thenThrow(new RuntimeException("Service unavailable"));

            // When & Then
            mockMvc.perform(get("/api/payments"))
                    .andExpect(status().is5xxServerError());

            verify(paymentService).findAll();
        }
    }
}
