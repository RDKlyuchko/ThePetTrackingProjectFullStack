package fontys.s3.PetTrackingProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DetectionConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}