package water_billing_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WaterBillingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(WaterBillingPlatformApplication.class, args);
    }
}