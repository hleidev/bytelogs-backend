package top.harrylei.forum.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "top.harrylei.forum")
public class QuickWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickWebApplication.class, args);
    }

}
