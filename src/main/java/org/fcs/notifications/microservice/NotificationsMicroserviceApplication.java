package org.fcs.notifications.microservice;

import org.fcs.notifications.microservice.config.props.MailProperties;
import org.fcs.notifications.microservice.config.props.ServiceClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        MailProperties.class,
        ServiceClientProperties.class
})
public class NotificationsMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationsMicroserviceApplication.class, args);
    }

}
