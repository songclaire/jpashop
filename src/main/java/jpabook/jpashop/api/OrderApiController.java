package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    //==================================================================================== V1 : ENTITY를 그대로 노출시키는 방법
    /**
     * 추천하는 방법 아님 (현재 안돌아감)
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        // 모든 주문 데이터를 조회 (Order 자체만 먼저 가져옴. 관련된 Member, Delivery, OrderItem은 아직 안 가져옴 - LAZY)
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            // Member 엔티티에서 이름을 꺼내서 지연로딩된 데이터를 초기화함 (초기화 = 데이터 가져온다)
            order.getMember().getName();
            // Delivery 엔티티에서 주소를 꺼내서 지연로딩된 데이터를 초기화함
            order.getDelivery().getAddress();

            // 주문상품(OrderItem) 리스트를 꺼내고, 각각의 상품(Item)의 이름을 조회해서 LAZY 데이터 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    //==================================================================================== V2 : ENTITY를 DTO로 변환
    /**
     * 단점 : 쿼리를 너무 많이 날림 (fetch가 필요함)
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        // ⭐ 여기서 받아온 ENTITY인 Order를 DTO로 변환하는 방법
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return collect;
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            // 값을 생성자인 여기로 넘겨서 값을 반환할 것
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());

        }
    }

    @Getter
    static class OrderItemDto {
        // 출력시킬 데이터만 입력
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
            
        }
    }

    //==================================================================================== V3 : ENTITY를 DTO로 변환 (fetch 조인)
    /**
     * fetch와 distinct까지 사용하여 원하는 값을 중복없이 가져옴
     * 단점 : 1:N일 경우 페이징 처리할 수 없음
     * @return
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        for (Order order : orders) {
            System.out.println("order = " + order + " id = " + order.getId());
        }

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }


    //==================================================================================== V4 : ENTITY를 DTO로 변환 (fetch 조인)

}
