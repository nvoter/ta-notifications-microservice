package org.fcs.notifications.microservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    public DirectExchange applicationStatusEventsExchange(
            @Value("${app.rabbitmq.application-status.exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue applicationStatusEventsQueue(
            @Value("${app.rabbitmq.application-status.queue}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding applicationStatusEventsBinding(
            @Qualifier("applicationStatusEventsQueue") Queue applicationStatusEventsQueue,
            @Qualifier("applicationStatusEventsExchange") DirectExchange applicationStatusEventsExchange,
            @Value("${app.rabbitmq.application-status.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(applicationStatusEventsQueue)
                .to(applicationStatusEventsExchange)
                .with(routingKey);
    }

    @Bean
    public DirectExchange studentConfirmationCodeEventsExchange(
            @Value("${app.rabbitmq.confirmation-code.exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue studentConfirmationCodeEventsQueue(
            @Value("${app.rabbitmq.confirmation-code.queue}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding studentConfirmationCodeEventsBinding(
            @Qualifier("studentConfirmationCodeEventsQueue") Queue studentConfirmationCodeEventsQueue,
            @Qualifier("studentConfirmationCodeEventsExchange") DirectExchange studentConfirmationCodeEventsExchange,
            @Value("${app.rabbitmq.confirmation-code.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(studentConfirmationCodeEventsQueue)
                .to(studentConfirmationCodeEventsExchange)
                .with(routingKey);
    }

    @Bean
    public DirectExchange employeeConfirmationCodeEventsExchange(
            @Value("${app.rabbitmq.employee-confirmation-code.exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue employeeConfirmationCodeEventsQueue(
            @Value("${app.rabbitmq.employee-confirmation-code.queue}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding employeeConfirmationCodeEventsBinding(
            @Qualifier("employeeConfirmationCodeEventsQueue") Queue employeeConfirmationCodeEventsQueue,
            @Qualifier("employeeConfirmationCodeEventsExchange") DirectExchange employeeConfirmationCodeEventsExchange,
            @Value("${app.rabbitmq.employee-confirmation-code.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(employeeConfirmationCodeEventsQueue)
                .to(employeeConfirmationCodeEventsExchange)
                .with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
