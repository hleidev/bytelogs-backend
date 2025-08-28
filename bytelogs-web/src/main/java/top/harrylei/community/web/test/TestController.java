package top.harrylei.community.web.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.harrylei.community.api.model.base.ResVO;

@Slf4j
@RestController
@RequestMapping("/v1/test")
public class TestController {

    @GetMapping("/ping")
    public ResVO<Boolean> test() {
        log.info("测试基本连通性");
        return ResVO.ok(true);
    }
}
