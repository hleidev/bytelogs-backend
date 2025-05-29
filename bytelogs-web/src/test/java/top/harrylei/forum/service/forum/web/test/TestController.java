package top.harrylei.forum.service.forum.web.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ping")
public class TestController {

    @GetMapping
    public String test() {
        log.info("API test");
        return "pong";
    }
}
