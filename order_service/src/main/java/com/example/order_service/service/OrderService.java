package com.example.order_service.service;

import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderItemRequest;
import com.example.dto.OrderItemResponse;
import com.example.dto.OrderPageResponse;
import com.example.dto.OrderResponse;
import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.client.UserServiceClient;
import com.example.order_service.dto.AddressResponse;
import com.example.order_service.dto.ProductResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.entity.PaymentMethod;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductServiceClient productServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    public List<OrderItemResponse> getOrderItems(String userId){
        Order order = orderRepository.findByUserId(userId);
        List<OrderItem> orderItems = order.getItems();
        return orderItems.stream().map(orderMapper::toOrderItemResponse).toList();
    }
    public OrderResponse placeOrder(String userId, CreateOrderRequest createOrderRequest) {
        // 1. Thu thập tất cả ProductId từ request của khách
        List<String> productIds = createOrderRequest.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        // 2. Gọi duy nhất 1 lần sang Product Service để lấy data
        List<ProductResponse> productResponses = productServiceClient.getProductsByIds(productIds);

        // 3. Chuyển List thành Map để tìm kiếm bằng ID
        Map<String, ProductResponse> productMap = productResponses.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        AddressResponse addressResponse = userServiceClient.getAddressById(createOrderRequest.getAddressId(),userId);
        Order order = new Order();
        order.setUserId(userId);
        order.setStreet(addressResponse.getStreet());
        order.setCity(addressResponse.getCity());
        order.setDistrict(addressResponse.getDistrict());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.valueOf(createOrderRequest.getPaymentMethod().name()));
        order.setNote(createOrderRequest.getNote());
        order.setCreatedAt(OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        order.setUpdatedAt(OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        order.setItems(new ArrayList<>());
        double totalAmount = 0;

        // 5. Duyệt qua items client gửi lên và khớp với dữ liệu từ Map
        for (OrderItemRequest itemReq : createOrderRequest.getItems()) {
            ProductResponse p = productMap.get(itemReq.getProductId());

            if (p == null) throw new RuntimeException("Sản phẩm không tồn tại: " + itemReq.getProductId());
            if (p.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + p.getName() + " không đủ hàng!");
            }

            OrderItem orderItem = OrderItem.builder()
                    .productId(p.getId())
                    .productName(p.getName())
                    .quantity(itemReq.getQuantity())
                    .priceAtPurchase(p.getPrice())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);
            totalAmount += p.getPrice() * itemReq.getQuantity();
        }
        order.setTotalPrice(totalAmount);
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }
    public void cancelOrder(String id, String userId){
        Order order = orderRepository.findByUserId(userId);
        if (order == null || !order.getId().equals(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại hoặc không thuộc về người dùng");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(OffsetDateTime.now());
        orderRepository.save(order);
    }

    public OrderResponse getOrderById(String id, String userId) {
         Order order = orderRepository.findByUserId(userId);
         if (order == null || !order.getId().equals(id)) {
             throw new RuntimeException("Đơn hàng không tồn tại hoặc không thuộc về người dùng");
         }
         return orderMapper.toOrderResponse(order);
     }

    public OrderPageResponse getMyOrders(String userId, String status, Integer page, Integer size) {
         // Set default values if not provided
         int pageNum = page != null ? page : 0;
         int pageSize = size != null ? size : 10;
         Pageable pageable = PageRequest.of(pageNum, pageSize);

         Page<Order> orderPage;
         if (status != null && !status.isEmpty()) {
             try {
                 OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                 orderPage = orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable);
             } catch (IllegalArgumentException e) {
                 throw new RuntimeException("Trạng thái đơn hàng không hợp lệ");
             }
         } else {
             orderPage = orderRepository.findByUserId(userId, pageable);
         }

         OrderPageResponse response = new OrderPageResponse();
         response.setContent(orderPage.getContent().stream()
                 .map(orderMapper::toOrderResponse)
                 .toList());
         response.setTotalPages(orderPage.getTotalPages());
         response.setTotalElements((int) orderPage.getTotalElements());
         return response;
     }

 }
