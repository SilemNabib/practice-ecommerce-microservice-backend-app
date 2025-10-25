package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.impl.OrderServiceImpl;

/**
 * Comprehensive test suite for OrderService
 * Tests all CRUD operations, order management scenarios, and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderDto testOrderDto;
    private Cart testCart;
    private CartDto testCartDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testOrder = Order.builder()
                .orderId(1)
                .orderDate(now)
                .orderDesc("Electronics Order")
                .orderFee(299.99)
                .build();

        testCart = Cart.builder()
                .cartId(1)
                .userId(1)
                .orders(Set.of(testOrder))
                .build();

        testOrder.setCart(testCart);

        testOrderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(now)
                .orderDesc("Electronics Order")
                .orderFee(299.99)
                .build();

        testCartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .orderDtos(Set.of(testOrderDto))
                .build();

        testOrderDto.setCartDto(testCartDto);
    }

    @Nested
    @DisplayName("Find All Operations")
    class FindAllTests {

        @Test
        @DisplayName("Should return all orders when repository has data")
        void shouldReturnAllOrders_WhenRepositoryHasData() {
            // Given
            List<Order> orders = Arrays.asList(testOrder);
            when(orderRepository.findAll()).thenReturn(orders);

            // When
            List<OrderDto> result = orderService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Electronics Order", result.get(0).getOrderDesc());
            assertEquals(299.99, result.get(0).getOrderFee());
            assertEquals(Integer.valueOf(1), result.get(0).getOrderId());
            verify(orderRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when repository has no data")
        void shouldReturnEmptyList_WhenRepositoryHasNoData() {
            // Given
            when(orderRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<OrderDto> result = orderService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(orderRepository).findAll();
        }

        @Test
        @DisplayName("Should return multiple orders with different fees")
        void shouldReturnMultipleOrders_WithDifferentFees() {
            // Given
            Cart expensiveCart = Cart.builder()
                    .cartId(2)
                    .userId(2)
                    .orders(Collections.emptySet())
                    .build();

            Cart cheapCart = Cart.builder()
                    .cartId(3)
                    .userId(3)
                    .orders(Collections.emptySet())
                    .build();

            Order expensiveOrder = Order.builder()
                    .orderId(2)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Premium Order")
                    .orderFee(999.99)
                    .cart(expensiveCart)
                    .build();

            Order cheapOrder = Order.builder()
                    .orderId(3)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Budget Order")
                    .orderFee(49.99)
                    .cart(cheapCart)
                    .build();

            List<Order> orders = Arrays.asList(testOrder, expensiveOrder, cheapOrder);
            when(orderRepository.findAll()).thenReturn(orders);

            // When
            List<OrderDto> result = orderService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("Electronics Order", result.get(0).getOrderDesc());
            assertEquals("Premium Order", result.get(1).getOrderDesc());
            assertEquals("Budget Order", result.get(2).getOrderDesc());
            verify(orderRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find By ID Operations")
    class FindByIdTests {

        @Test
        @DisplayName("Should return order when it exists")
        void shouldReturnOrder_WhenItExists() {
            // Given
            Integer orderId = 1;
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            // When
            OrderDto result = orderService.findById(orderId);

            // Then
            assertNotNull(result);
            assertEquals(orderId, result.getOrderId());
            assertEquals("Electronics Order", result.getOrderDesc());
            assertEquals(299.99, result.getOrderFee());
            verify(orderRepository).findById(orderId);
        }

        @Test
        @DisplayName("Should throw exception when order does not exist")
        void shouldThrowException_WhenOrderDoesNotExist() {
            // Given
            Integer orderId = 999;
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            // When & Then
            OrderNotFoundException exception = assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.findById(orderId)
            );
            
            assertTrue(exception.getMessage().contains("Order with id: 999 not found"));
            verify(orderRepository).findById(orderId);
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save and return order with valid data")
        void shouldSaveAndReturnOrder_WithValidData() {
            // Given
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // When
            OrderDto result = orderService.save(testOrderDto);

            // Then
            assertNotNull(result);
            assertEquals(testOrderDto.getOrderId(), result.getOrderId());
            assertEquals(testOrderDto.getOrderDesc(), result.getOrderDesc());
            assertEquals(testOrderDto.getOrderFee(), result.getOrderFee());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should save new order with different cart")
        void shouldSaveNewOrder_WithDifferentCart() {
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

            Cart savedCart = Cart.builder()
                    .cartId(2)
                    .userId(2)
                    .orders(Collections.emptySet())
                    .build();

            Order savedOrder = Order.builder()
                    .orderId(2)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("New Order")
                    .orderFee(199.99)
                    .cart(savedCart)
                    .build();

            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            // When
            OrderDto result = orderService.save(newOrderDto);

            // Then
            assertNotNull(result);
            assertEquals("New Order", result.getOrderDesc());
            assertEquals(199.99, result.getOrderFee());
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update and return order")
        void shouldUpdateAndReturnOrder() {
            // Given
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // When
            OrderDto result = orderService.update(testOrderDto);

            // Then
            assertNotNull(result);
            assertEquals(testOrderDto.getOrderId(), result.getOrderId());
            assertEquals(testOrderDto.getOrderDesc(), result.getOrderDesc());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should update order with new information")
        void shouldUpdateOrder_WithNewInformation() {
            // Given
            OrderDto updatedOrderDto = OrderDto.builder()
                    .orderId(1)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Updated Electronics Order")
                    .orderFee(399.99)
                    .cartDto(testCartDto)
                    .build();

            Order savedOrder = Order.builder()
                    .orderId(1)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Updated Electronics Order")
                    .orderFee(399.99)
                    .cart(testCart)
                    .build();

            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            // When
            OrderDto result = orderService.update(updatedOrderDto);

            // Then
            assertNotNull(result);
            assertEquals("Updated Electronics Order", result.getOrderDesc());
            assertEquals(399.99, result.getOrderFee());
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete order by ID")
        void shouldDeleteOrderById() {
            // Given
            Integer orderId = 1;
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            doNothing().when(orderRepository).delete(any(Order.class));

            // When
            orderService.deleteById(orderId);

            // Then
            verify(orderRepository).findById(orderId);
            verify(orderRepository).delete(any(Order.class));
        }

        @Test
        @DisplayName("Should handle delete operation without exceptions")
        void shouldHandleDeleteOperation_WithoutExceptions() {
            // Given
            Integer orderId = 1;
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            doNothing().when(orderRepository).delete(any(Order.class));

            // When & Then
            assertDoesNotThrow(() -> orderService.deleteById(orderId));
            verify(orderRepository).findById(orderId);
            verify(orderRepository).delete(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Order Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should handle order with zero fee")
        void shouldHandleOrder_WithZeroFee() {
            // Given
            OrderDto freeOrderDto = OrderDto.builder()
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Free Order")
                    .orderFee(0.0)
                    .cartDto(testCartDto)
                    .build();

            Order savedOrder = Order.builder()
                    .orderId(2)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Free Order")
                    .orderFee(0.0)
                    .cart(testCart)
                    .build();

            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            // When
            OrderDto result = orderService.save(freeOrderDto);

            // Then
            assertNotNull(result);
            assertEquals(0.0, result.getOrderFee());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should handle high-value order")
        void shouldHandleHighValueOrder() {
            // Given
            OrderDto expensiveOrderDto = OrderDto.builder()
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Luxury Order")
                    .orderFee(9999.99)
                    .cartDto(testCartDto)
                    .build();

            Order savedOrder = Order.builder()
                    .orderId(3)
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Luxury Order")
                    .orderFee(9999.99)
                    .cart(testCart)
                    .build();

            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            // When
            OrderDto result = orderService.save(expensiveOrderDto);

            // Then
            assertNotNull(result);
            assertEquals(9999.99, result.getOrderFee());
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput_Gracefully() {
            // When & Then
            assertThrows(NullPointerException.class, () -> orderService.save(null));
        }

        @Test
        @DisplayName("Should handle repository exceptions")
        void shouldHandleRepositoryExceptions() {
            // Given
            when(orderRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> orderService.findAll());
        }

        @Test
        @DisplayName("Should handle invalid order ID")
        void shouldHandleInvalidOrderId() {
            // Given
            Integer invalidOrderId = -1;
            when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(OrderNotFoundException.class, () -> orderService.findById(invalidOrderId));
        }

        @Test
        @DisplayName("Should handle order with negative fee")
        void shouldHandleOrder_WithNegativeFee() {
            // Given
            OrderDto invalidOrderDto = OrderDto.builder()
                    .orderDate(LocalDateTime.now())
                    .orderDesc("Invalid Order")
                    .orderFee(-10.0)
                    .cartDto(testCartDto)
                    .build();

            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // When
            OrderDto result = orderService.save(invalidOrderDto);

            // Then
            assertNotNull(result);
            // Note: Business validation should be handled in the service layer
            verify(orderRepository).save(any(Order.class));
        }
    }
}
