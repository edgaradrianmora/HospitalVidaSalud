package com.atenciones.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    public static final String EXCHANGE    = "ATENCIONES_INTERCAMBIO";
    public static final String QUEUE       = "ATENCIONES_COLA";
    public static final String ROUTING_KEY = "ATENCIONES_KEY";

    @Bean
    Queue queue() { return new Queue(QUEUE, true); }

    @Bean
    DirectExchange exchange() { return new DirectExchange(EXCHANGE); }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}