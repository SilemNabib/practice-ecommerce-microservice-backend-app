package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.impl.PaymentServiceImpl;

/**
 * Comprehensive test suite for PaymentService
 * Tests all CRUD operations, payment processing scenarios, and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private PaymentDto testPaymentDto;
    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testOrderDto = OrderDto.builder()
                .orderId(1)
                .orderFee(299.99)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .build();

        testPayment = Payment.builder()
                .paymentId(1)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .orderId(1)
                .isPayed(false)
                .build();

        testPaymentDto = PaymentDto.builder()
                .paymentId(1)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .orderDto(testOrderDto)
                .isPayed(false)
                .build();
    }

    @Nested
    @DisplayName("Find All Operations")
    class FindAllTests {

        @Test
        @DisplayName("Should return all payments when repository has data")
        void shouldReturnAllPayments_WhenRepositoryHasData() {
            // Given
            List<Payment> payments = Arrays.asList(testPayment);
            when(paymentRepository.findAll()).thenReturn(payments);
            lenient().when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(testOrderDto);

            // When
            List<PaymentDto> result = paymentService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(PaymentStatus.IN_PROGRESS, result.get(0).getPaymentStatus());
            assertEquals(Integer.valueOf(1), result.get(0).getPaymentId());
            assertNotNull(result.get(0).getOrderDto());
            assertEquals(Integer.valueOf(1), result.get(0).getOrderDto().getOrderId());
            verify(paymentRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when repository has no data")
        void shouldReturnEmptyList_WhenRepositoryHasNoData() {
            // Given
            when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<PaymentDto> result = paymentService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(paymentRepository).findAll();
        }

        @Test
        @DisplayName("Should return multiple payments with different statuses")
        void shouldReturnMultiplePayments_WithDifferentStatuses() {
            // Given
            Payment completedPayment = Payment.builder()
                    .paymentId(2)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderId(2)
                    .isPayed(true)
                    .build();

            Payment notStartedPayment = Payment.builder()
                    .paymentId(3)
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderId(3)
                    .isPayed(false)
                    .build();

            List<Payment> payments = Arrays.asList(testPayment, completedPayment, notStartedPayment);
            when(paymentRepository.findAll()).thenReturn(payments);

            // When
            List<PaymentDto> result = paymentService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(PaymentStatus.IN_PROGRESS, result.get(0).getPaymentStatus());
            assertEquals(PaymentStatus.COMPLETED, result.get(1).getPaymentStatus());
            assertEquals(PaymentStatus.NOT_STARTED, result.get(2).getPaymentStatus());
            verify(paymentRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find By ID Operations")
    class FindByIdTests {

        @Test
        @DisplayName("Should return payment when it exists")
        void shouldReturnPayment_WhenItExists() {
            // Given
            Integer paymentId = 1;
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
            lenient().when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(testOrderDto);

            // When
            PaymentDto result = paymentService.findById(paymentId);

            // Then
            assertNotNull(result);
            assertEquals(paymentId, result.getPaymentId());
            assertEquals(PaymentStatus.IN_PROGRESS, result.getPaymentStatus());
            assertNotNull(result.getOrderDto());
            assertEquals(Integer.valueOf(1), result.getOrderDto().getOrderId());
            verify(paymentRepository).findById(paymentId);
        }

        @Test
        @DisplayName("Should throw exception when payment does not exist")
        void shouldThrowException_WhenPaymentDoesNotExist() {
            // Given
            Integer paymentId = 999;
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

            // When & Then
            PaymentNotFoundException exception = assertThrows(
                    PaymentNotFoundException.class,
                    () -> paymentService.findById(paymentId)
            );

            assertTrue(exception.getMessage().contains("Payment with id: 999 not found"));
            verify(paymentRepository).findById(paymentId);
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save and return payment with valid data")
        void shouldSaveAndReturnPayment_WithValidData() {
            // Given
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            // When
            PaymentDto result = paymentService.save(testPaymentDto);

            // Then
            assertNotNull(result);
            assertEquals(testPaymentDto.getPaymentId(), result.getPaymentId());
            assertEquals(testPaymentDto.getPaymentStatus(), result.getPaymentStatus());
            assertEquals(testPaymentDto.getIsPayed(), result.getIsPayed());
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should save new payment with different order")
        void shouldSaveNewPayment_WithDifferentOrder() {
            // Given
            OrderDto newOrderDto = OrderDto.builder()
                    .orderId(2)
                    .orderFee(499.99)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Premium Order")
                    .build();

            PaymentDto newPaymentDto = PaymentDto.builder()
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderDto(newOrderDto)
                    .isPayed(false)
                    .build();

            Payment savedPayment = Payment.builder()
                    .paymentId(2)
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderId(2)
                    .isPayed(false)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
            lenient().when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(newOrderDto);

            // When
            PaymentDto result = paymentService.save(newPaymentDto);

            // Then
            assertNotNull(result);
            assertEquals(PaymentStatus.NOT_STARTED, result.getPaymentStatus());
            assertEquals(Integer.valueOf(2), result.getOrderDto().getOrderId());
            assertFalse(result.getIsPayed());
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update and return payment")
        void shouldUpdateAndReturnPayment() {
            // Given
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            // When
            PaymentDto result = paymentService.update(testPaymentDto);

            // Then
            assertNotNull(result);
            assertEquals(testPaymentDto.getPaymentId(), result.getPaymentId());
            assertEquals(testPaymentDto.getPaymentStatus(), result.getPaymentStatus());
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should update payment status to completed")
        void shouldUpdatePaymentStatus_ToCompleted() {
            // Given
            PaymentDto completedPaymentDto = PaymentDto.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderDto(testOrderDto)
                    .isPayed(true)
                    .build();

            Payment savedPayment = Payment.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderId(1)
                    .isPayed(true)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

            // When
            PaymentDto result = paymentService.update(completedPaymentDto);

            // Then
            assertNotNull(result);
            assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
            assertTrue(result.getIsPayed());
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete payment by ID")
        void shouldDeletePaymentById() {
            // Given
            Integer paymentId = 1;
            doNothing().when(paymentRepository).deleteById(paymentId);

            // When
            paymentService.deleteById(paymentId);

            // Then
            verify(paymentRepository).deleteById(paymentId);
        }

        @Test
        @DisplayName("Should handle delete operation without exceptions")
        void shouldHandleDeleteOperation_WithoutExceptions() {
            // Given
            Integer paymentId = 1;
            doNothing().when(paymentRepository).deleteById(paymentId);

            // When & Then
            assertDoesNotThrow(() -> paymentService.deleteById(paymentId));
            verify(paymentRepository).deleteById(paymentId);
        }
    }

    @Nested
    @DisplayName("Payment Processing Tests")
    class PaymentProcessingTests {

        @Test
        @DisplayName("Should handle payment processing workflow")
        void shouldHandlePaymentProcessingWorkflow() {
            // Given
            PaymentDto notStartedPayment = PaymentDto.builder()
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderDto(testOrderDto)
                    .isPayed(false)
                    .build();

            Payment processedPayment = Payment.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.IN_PROGRESS)
                    .orderId(1)
                    .isPayed(false)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(processedPayment);

            // When
            PaymentDto result = paymentService.save(notStartedPayment);

            // Then
            assertNotNull(result);
            assertEquals(PaymentStatus.IN_PROGRESS, result.getPaymentStatus());
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should handle payment not started scenario")
        void shouldHandlePaymentNotStartedScenario() {
            // Given
            PaymentDto notStartedPayment = PaymentDto.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderDto(testOrderDto)
                    .isPayed(false)
                    .build();

            Payment savedPayment = Payment.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.NOT_STARTED)
                    .orderId(1)
                    .isPayed(false)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

            // When
            PaymentDto result = paymentService.update(notStartedPayment);

            // Then
            assertNotNull(result);
            assertEquals(PaymentStatus.NOT_STARTED, result.getPaymentStatus());
            assertFalse(result.getIsPayed());
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should handle successful payment completion")
        void shouldHandleSuccessfulPaymentCompletion() {
            // Given
            PaymentDto successfulPayment = PaymentDto.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderDto(testOrderDto)
                    .isPayed(true)
                    .build();

            Payment savedPayment = Payment.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .orderId(1)
                    .isPayed(true)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

            // When
            PaymentDto result = paymentService.update(successfulPayment);

            // Then
            assertNotNull(result);
            assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
            assertTrue(result.getIsPayed());
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput_Gracefully() {
            // When & Then
            assertThrows(NullPointerException.class, () -> paymentService.save(null));
        }

        @Test
        @DisplayName("Should handle repository exceptions")
        void shouldHandleRepositoryExceptions() {
            // Given
            when(paymentRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> paymentService.findAll());
        }

        @Test
        @DisplayName("Should handle invalid payment ID")
        void shouldHandleInvalidPaymentId() {
            // Given
            Integer invalidPaymentId = -1;
            when(paymentRepository.findById(invalidPaymentId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(PaymentNotFoundException.class, () -> paymentService.findById(invalidPaymentId));
        }

        @Test
        @DisplayName("Should handle payment with high amount")
        void shouldHandlePayment_WithHighAmount() {
            // Given
            OrderDto expensiveOrder = OrderDto.builder()
                    .orderId(1)
                    .orderFee(9999.99)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Expensive Order")
                    .build();

            PaymentDto expensivePayment = PaymentDto.builder()
                    .paymentStatus(PaymentStatus.IN_PROGRESS)
                    .orderDto(expensiveOrder)
                    .isPayed(false)
                    .build();

            Payment savedPayment = Payment.builder()
                    .paymentId(1)
                    .paymentStatus(PaymentStatus.IN_PROGRESS)
                    .orderId(1)
                    .isPayed(false)
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
            lenient().when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(expensiveOrder);

            // When
            PaymentDto result = paymentService.save(expensivePayment);

            // Then
            assertNotNull(result);
            assertNotNull(result.getOrderDto());
            assertEquals(9999.99, result.getOrderDto().getOrderFee());
            verify(paymentRepository).save(any(Payment.class));
        }
    }
}
