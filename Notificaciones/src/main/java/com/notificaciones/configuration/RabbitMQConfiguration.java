package com.notificaciones.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    public static final String QUEUE_PACIENTES    = "PACIENTES_COLA";
    public static final String EXCHANGE_PACIENTES = "PACIENTES_INTERCAMBIO";
    public static final String KEY_PACIENTES      = "PACIENTES_KEY";

    public static final String QUEUE_CITAS        = "CITAS_COLA";
    public static final String EXCHANGE_CITAS     = "CITAS_INTERCAMBIO";
    public static final String KEY_CITAS          = "CITAS_KEY";

    public static final String QUEUE_ATENCIONES    = "ATENCIONES_COLA";
    public static final String EXCHANGE_ATENCIONES = "ATENCIONES_INTERCAMBIO";
    public static final String KEY_ATENCIONES      = "ATENCIONES_KEY";

    @Bean
    Queue queuePacientes() { return new Queue(QUEUE_PACIENTES, true); }

    @Bean
    DirectExchange exchangePacientes() {
        return new DirectExchange(EXCHANGE_PACIENTES);
    }

    @Bean
    Binding bindingPacientes(Queue queuePacientes,
                              DirectExchange exchangePacientes) {
        return BindingBuilder.bind(queuePacientes)
                .to(exchangePacientes).with(KEY_PACIENTES);
    }

    
    
    
    
    @Bean
    Queue queueCitas() { return new Queue(QUEUE_CITAS, true); }

    @Bean
    DirectExchange exchangeCitas() {
        return new DirectExchange(EXCHANGE_CITAS);
    }

    @Bean
    Binding bindingCitas(Queue queueCitas, DirectExchange exchangeCitas) {
        return BindingBuilder.bind(queueCitas)
                .to(exchangeCitas).with(KEY_CITAS);
    }


    
    
    
    
    @Bean
    Queue queueAtenciones() { return new Queue(QUEUE_ATENCIONES, true); }

    @Bean
    DirectExchange exchangeAtenciones() {
        return new DirectExchange(EXCHANGE_ATENCIONES);
    }

    @Bean
    Binding bindingAtenciones(Queue queueAtenciones,
                               DirectExchange exchangeAtenciones) {
        return BindingBuilder.bind(queueAtenciones)
                .to(exchangeAtenciones).with(KEY_ATENCIONES);
    }
}