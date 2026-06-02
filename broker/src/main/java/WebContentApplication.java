package broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"broker", "controller", "service", "config", "domain"})
@EnableJpaRepositories(basePackages = "domain")
public class WebContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebContentApplication.class, args);
    }
}
