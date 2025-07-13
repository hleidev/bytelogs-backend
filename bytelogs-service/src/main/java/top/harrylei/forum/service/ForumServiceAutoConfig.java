package top.harrylei.forum.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("top.harrylei.forum.service")
@MapperScan(basePackages = {
        "top.harrylei.forum.service.user.repository.mapper",
        "top.harrylei.forum.service.article.repository.mapper",
        "top.harrylei.forum.service.comment.repository.mapper",
        "top.harrylei.forum.service.statistics.repository.mapper",
        "top.harrylei.forum.service.notify.repository.mapper",
})
public class ForumServiceAutoConfig {
}
