package top.harrylei.forum.web.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.harrylei.forum.api.model.vo.ResVO;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/ping")
    public ResVO<Boolean> test() {
        log.info("测试基本连通性");
        return ResVO.ok(true);
    }
}
