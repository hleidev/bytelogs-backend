package top.harrylei.community.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 社区服务自动配置类
 *
 * @author harry
 */
@Configuration
@ComponentScan("top.harrylei.community.service")
@MapperScan(basePackages = {
        "top.harrylei.community.service.user.repository.mapper",
        "top.harrylei.community.service.article.repository.mapper",
        "top.harrylei.community.service.comment.repository.mapper",
        "top.harrylei.community.service.statistics.repository.mapper",
        "top.harrylei.community.service.notify.repository.mapper",
        "top.harrylei.community.service.rank.repository.mapper",
        "top.harrylei.community.service.ai.repository.mapper",
})
public class CommunityServiceAutoConfig {
}