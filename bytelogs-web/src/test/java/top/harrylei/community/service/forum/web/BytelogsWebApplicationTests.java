package top.harrylei.community.service.forum.web;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 应用启动集成测试
 * 需要完整的基础设施（MySQL、Redis、Kafka）才能运行
 * 暂时禁用，避免影响单元测试执行
 */
@SpringBootTest
@Disabled("需要完整的基础设施才能运行")
class BytelogsWebApplicationTests {

    @Test
    void contextLoads() {}

}
