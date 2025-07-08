package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
// Item이라는 싱글 테이블에서 저장될 때 각 요소가 구분되어 들어가기 위한 조치
@DiscriminatorValue("B")
@Getter
@Setter
public class Book extends Item {

    private String author;

    private String isbn;
}
