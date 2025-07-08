package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 여러개의 orders에 하나의 member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // cascade = All : order 저장할 때 orderItems도 같이 진행됨
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // cascade = All : order 저장할 때 delivery도 같이 진행됨
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    // 주문 상태 [ORDER, CANCEL]
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /**
     * 양방향 연관관계 편의 메서드
     */
    // Order에 추가될 때 member에도 같이 추가되는 메서드
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    // Order에 추가될 때 orderItems에도 같이 추가되는 메서드
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /**
     * 주문 생성 메서드
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    /**
     * 주문 취소 메서드
     */
    public void cancel() {
        // 이미 배송이 완료되어서 취소가 불가능 IllegalStateException("")
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송이 완료된 상품입니다.");
        }
        // set status 변경
        this.setStatus(OrderStatus.CANCEL);

        // 재고 원복
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * 조회 - 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            // 전체 가격을 알기 위해서는 주문 가격과 수량을 알아야 하기 때문에 OrderItem에서 계산해서 가져오기
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

}
