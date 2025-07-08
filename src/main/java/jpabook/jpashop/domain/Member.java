package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    // 하나의 member에 여러개의 Order
    // mappedBy : Order 테이블에 있는 'member' 필드에 의해 Mapping된거야.
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
