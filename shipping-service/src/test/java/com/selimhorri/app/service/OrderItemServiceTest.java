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

import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.repository.OrderItemRepository;
import com.selimhorri.app.service.impl.OrderItemServiceImpl;

/**
 * Comprehensive test suite for OrderItemService
 * Tests all CRUD operations, shipping scenarios, and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Item Service Tests")
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItem testOrderItem;
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

        testOrderItem = OrderItem.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(2)
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
    @DisplayName("Find All Operations")
    class FindAllTests {

        @Test
        @DisplayName("Should return all order items when repository has data")
        void shouldReturnAllOrderItems_WhenRepositoryHasData() {
            // Given
            List<OrderItem> orderItems = Arrays.asList(testOrderItem);
            when(orderItemRepository.findAll()).thenReturn(orderItems);
            when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(testProductDto);
            when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(testOrderDto);

            // When
            List<OrderItemDto> result = orderItemService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Integer.valueOf(2), result.get(0).getOrderedQuantity());
            assertEquals(Integer.valueOf(1), result.get(0).getProductId());
            assertEquals(Integer.valueOf(1), result.get(0).getOrderId());
            verify(orderItemRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when repository has no data")
        void shouldReturnEmptyList_WhenRepositoryHasNoData() {
            // Given
            when(orderItemRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<OrderItemDto> result = orderItemService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(orderItemRepository).findAll();
        }

        @Test
        @DisplayName("Should return multiple order items with different quantities")
        void shouldReturnMultipleOrderItems_WithDifferentQuantities() {
            // Given
            OrderItem secondOrderItem = OrderItem.builder()
                    .productId(2)
                    .orderId(1)
                    .orderedQuantity(5)
                    .build();

            List<OrderItem> orderItems = Arrays.asList(testOrderItem, secondOrderItem);
            when(orderItemRepository.findAll()).thenReturn(orderItems);
            when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(testProductDto);
            when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(testOrderDto);

            // When
            List<OrderItemDto> result = orderItemService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(Integer.valueOf(2), result.get(0).getOrderedQuantity());
            assertEquals(Integer.valueOf(5), result.get(1).getOrderedQuantity());
            verify(orderItemRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find By ID Operations")
    class FindByIdTests {

        @Test
        @DisplayName("Should return order item when it exists")
        void shouldReturnOrderItem_WhenItExists() {
            // Given
            OrderItemId orderItemId = new OrderItemId(1, 1);
            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(testOrderItem));
            lenient().when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(testProductDto);
            lenient().when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(testOrderDto);

            // When
            OrderItemDto result = orderItemService.findById(orderItemId);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(1), result.getProductId());
            assertEquals(Integer.valueOf(1), result.getOrderId());
            assertEquals(Integer.valueOf(2), result.getOrderedQuantity());
            verify(orderItemRepository).findById(orderItemId);
        }

        @Test
        @DisplayName("Should throw exception when order item does not exist")
        void shouldThrowException_WhenOrderItemDoesNotExist() {
            // Given
            OrderItemId orderItemId = new OrderItemId(999, 999);
            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

            // When & Then
            OrderItemNotFoundException exception = assertThrows(
                    OrderItemNotFoundException.class,
                    () -> orderItemService.findById(orderItemId)
            );

            assertTrue(exception.getMessage().contains("OrderItem with id: OrderItemId(productId=999, orderId=999) not found"));
            verify(orderItemRepository).findById(orderItemId);
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save and return order item with valid data")
        void shouldSaveAndReturnOrderItem_WithValidData() {
            // Given
            when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);

            // When
            OrderItemDto result = orderItemService.save(testOrderItemDto);

            // Then
            assertNotNull(result);
            assertEquals(testOrderItemDto.getOrderedQuantity(), result.getOrderedQuantity());
            assertEquals(testOrderItemDto.getProductId(), result.getProductId());
            assertEquals(testOrderItemDto.getOrderId(), result.getOrderId());
            verify(orderItemRepository).save(any(OrderItem.class));
        }

        @Test
        @DisplayName("Should save new order item with different product")
        void shouldSaveNewOrderItem_WithDifferentProduct() {
            // Given
            OrderItemDto newOrderItem = OrderItemDto.builder()
                    .productId(2)
                    .orderId(2)
                    .orderedQuantity(3)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            OrderItem savedOrderItem = OrderItem.builder()
                    .productId(2)
                    .orderId(2)
                    .orderedQuantity(3)
                    .build();

            when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);

            // When
            OrderItemDto result = orderItemService.save(newOrderItem);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(3), result.getOrderedQuantity());
            assertEquals(Integer.valueOf(2), result.getProductId());
            assertEquals(Integer.valueOf(2), result.getOrderId());
            verify(orderItemRepository).save(any(OrderItem.class));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update and return order item")
        void shouldUpdateAndReturnOrderItem() {
            // Given
            when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);

            // When
            OrderItemDto result = orderItemService.update(testOrderItemDto);

            // Then
            assertNotNull(result);
            assertEquals(testOrderItemDto.getOrderedQuantity(), result.getOrderedQuantity());
            assertEquals(testOrderItemDto.getProductId(), result.getProductId());
            verify(orderItemRepository).save(any(OrderItem.class));
        }

        @Test
        @DisplayName("Should update order item with new quantity")
        void shouldUpdateOrderItem_WithNewQuantity() {
            // Given
            OrderItemDto updatedOrderItem = OrderItemDto.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(4)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            OrderItem savedOrderItem = OrderItem.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(4)
                    .build();

            when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);

            // When
            OrderItemDto result = orderItemService.update(updatedOrderItem);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(4), result.getOrderedQuantity());
            assertEquals(Integer.valueOf(1), result.getProductId());
            verify(orderItemRepository).save(any(OrderItem.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete order item by ID")
        void shouldDeleteOrderItemById() {
            // Given
            OrderItemId orderItemId = new OrderItemId(1, 1);
            doNothing().when(orderItemRepository).deleteById(orderItemId);

            // When
            orderItemService.deleteById(orderItemId);

            // Then
            verify(orderItemRepository).deleteById(orderItemId);
        }

        @Test
        @DisplayName("Should handle delete operation without exceptions")
        void shouldHandleDeleteOperation_WithoutExceptions() {
            // Given
            OrderItemId orderItemId = new OrderItemId(1, 1);
            doNothing().when(orderItemRepository).deleteById(orderItemId);

            // When & Then
            assertDoesNotThrow(() -> orderItemService.deleteById(orderItemId));
            verify(orderItemRepository).deleteById(orderItemId);
        }
    }

    @Nested
    @DisplayName("Shipping Business Logic Tests")
    class ShippingBusinessLogicTests {

        @Test
        @DisplayName("Should handle order item with zero quantity")
        void shouldHandleOrderItem_WithZeroQuantity() {
            // Given
            OrderItemDto zeroQuantityItem = OrderItemDto.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(0)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            OrderItem savedOrderItem = OrderItem.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(0)
                    .build();

            when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);

            // When
            OrderItemDto result = orderItemService.save(zeroQuantityItem);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(0), result.getOrderedQuantity());
            verify(orderItemRepository).save(any(OrderItem.class));
        }

        @Test
        @DisplayName("Should handle order item with high quantity")
        void shouldHandleOrderItem_WithHighQuantity() {
            // Given
            OrderItemDto highQuantityItem = OrderItemDto.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(1000)
                    .productDto(testProductDto)
                    .orderDto(testOrderDto)
                    .build();

            OrderItem savedOrderItem = OrderItem.builder()
                    .productId(1)
                    .orderId(1)
                    .orderedQuantity(1000)
                    .build();

            when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);

            // When
            OrderItemDto result = orderItemService.save(highQuantityItem);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(1000), result.getOrderedQuantity());
            verify(orderItemRepository).save(any(OrderItem.class));
        }
    }

    @Nested
    @DisplayName("External Service Integration Tests")
    class ExternalServiceIntegrationTests {

        @Test
        @DisplayName("Should handle external service calls for product and order data")
        void shouldHandleExternalServiceCalls_ForProductAndOrderData() {
            // Given
            List<OrderItem> orderItems = Arrays.asList(testOrderItem);
            when(orderItemRepository.findAll()).thenReturn(orderItems);
            lenient().when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(testProductDto);
            lenient().when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(testOrderDto);

            // When
            List<OrderItemDto> result = orderItemService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertNotNull(result.get(0).getProductDto());
            assertNotNull(result.get(0).getOrderDto());
        }

        @Test
        @DisplayName("Should handle external service failures gracefully")
        void shouldHandleExternalServiceFailures_Gracefully() {
            // Given
            List<OrderItem> orderItems = Arrays.asList(testOrderItem);
            when(orderItemRepository.findAll()).thenReturn(orderItems);
            lenient().when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                    .thenThrow(new RuntimeException("Product service unavailable"));
            lenient().when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                    .thenThrow(new RuntimeException("Order service unavailable"));

            // When & Then
            assertThrows(RuntimeException.class, () -> orderItemService.findAll());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput_Gracefully() {
            // When & Then
            assertThrows(NullPointerException.class, () -> orderItemService.save(null));
        }

        @Test
        @DisplayName("Should handle repository exceptions")
        void shouldHandleRepositoryExceptions() {
            // Given
            when(orderItemRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> orderItemService.findAll());
        }

        @Test
        @DisplayName("Should handle invalid order item ID")
        void shouldHandleInvalidOrderItemId() {
            // Given
            OrderItemId invalidId = new OrderItemId(-1, -1);
            when(orderItemRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(OrderItemNotFoundException.class, () -> orderItemService.findById(invalidId));
        }
    }
}