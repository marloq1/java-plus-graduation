package ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ewm",
        "ru.practicum.feign"
})
@EnableFeignClients(basePackages = {"ru.practicum.feign","ewm.src.main.java.ru.practicum"})
public class EventServiceApp {

    public static void main(String[] args) {

        SpringApplication.run(EventServiceApp.class,args);
    }
}