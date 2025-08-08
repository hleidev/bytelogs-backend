package top.harrylei.forum.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目启动类
 *
 * @author harry
 */
@SpringBootApplication(scanBasePackages = "top.harrylei.forum")
@EnableScheduling
public class QuickWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickWebApplication.class, args);
    }

}
