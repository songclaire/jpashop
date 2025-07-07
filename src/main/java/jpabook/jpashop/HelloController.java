package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
    // Model : 데이터를 실어서 넘길 수 있음
    public String hello(Model model) {
        // name이 "data"라는 key에 "hello!"라는 값을 담음
        model.addAttribute("data", "hello!");

        // 화면 이름 (template > 'hello.html' 화면 파일)
        return "hello";
    }
}
