package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XToOne 관계에서
 * Order를 조회하고
 * Order와 Member(ManyToOne), Order와 Delivery(OneToOne)에 연관이 있게 할 것임
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    //======================================================================================== ENTITY
    /**
     * 이 경우는 Entity를 직접 노출해서 진행한 케이스.
     * 추천하지 않는다!
     * @return
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        // 이렇게 해도 검색조건이 없기 때문에 생짜로 다 들고 올것
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();      //LAZY 강제 초기화
            order.getDelivery().getAddress(); //LAZY 강제 초기화
        }
        return all;
    }

    //======================================================================================== ENTITY -> DTO로 변환후 출력
    // 이 방법으로 진행할 시 문제점 : N+1의 문제로 Order를 조회할 때 그 안에 엮여있는 delivery와 member로 인해 매우 많은 쿼리가 한꺼번에 읽어지는 문제
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        // DTO로 변환
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o)) // dto로 변환
                .collect(Collectors.toList()); // 그것을 list로 변환
        return result;
        // 위 코드 리팩토링
//        return orderRepository.findAllByCriteria(new OrderSearch()).stream()
//                .map(SimpleOrderDto::new)
//                .collect(Collectors.toList()));
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        // DTO가 ENTITY를 받는 것은 문제가 되지 않는다.
        // 생성자에서
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }

    }

    //======================================================================================== ENTITY -> DTO로 변환후 출력 (fetch 사용)
    // 위 문제의 해결방법 : fetch 사용 (OrderRepository)
    // 장점: 요소를 다 들고와서 활용도가 높음
    // 단점: 이 방법에서는 내가 원하는 요소 뿐만 아니라 다른 요소들도 다 들고옴
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    //======================================================================================== DTO로 변환후 출력 (repository에서 특정 컬럼을 쿼리로 지정해서 가져옴)
    // 장점: 결과를 보면 내가 원하는 요소만 select하고 있음
    // 단점: 재사용성이 안좋음
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

//    @Data
//    static class SimpleOrderDto {
//        private Long orderId;
//        private String name;
//        private LocalDateTime orderDate;
//        private OrderStatus orderStatus;
//        private Address address;
//    }

}
