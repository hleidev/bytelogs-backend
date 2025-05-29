package top.harrylei.forum.service.forum.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "top.harrylei.forum")  // Scan all classes under top.harrylei.forum
public class QuickWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickWebApplication.class, args);
    }

}
