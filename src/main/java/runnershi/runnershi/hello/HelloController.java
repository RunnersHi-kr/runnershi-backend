package runnershi.runnershi.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    /**
     * WHO:  아무나 (인증 없이)
     * WHEN: 클라이언트가 GET /hello 요청을 보냈을 때
     * WHERE: http://localhost:8080/hello
     * WHAT:  "Hello Runner's Hi" 라는 문자열을 응답
     * WHY:   스프링 MVC가 정상 동작하는지 테스트하기 위해
     * HOW:   @GetMapping으로 URL을 매핑하고, 문자열을 리턴
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello Runner's Hi";
    }
}
