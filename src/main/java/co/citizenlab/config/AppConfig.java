package co.citizenlab.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("application.properties")
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer createPropertySourcesPlaceholderConfigurer() {

        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ObjectMapper createObjectMapper() {

        return new ObjectMapper();
    }
}
