package com.notificaciones.component;

import com.notificaciones.configuration.RabbitMQConfiguration;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorComponent {

    @RabbitListener(queues = RabbitMQConfiguration.QUEUE_PACIENTES)
    public void ProcesarPacientes(String mensaje) {
        System.out.println("NOTIFICACION [PACIENTES]: " + Traducir(mensaje));
    }

    @RabbitListener(queues = RabbitMQConfiguration.QUEUE_CITAS)
    public void ProcesarCitas(String mensaje) {
        System.out.println("NOTIFICACION [CITAS]: " + Traducir(mensaje));
    }

    @RabbitListener(queues = RabbitMQConfiguration.QUEUE_ATENCIONES)
    public void ProcesarAtenciones(String mensaje) {
        System.out.println("NOTIFICACION [ATENCIONES]: " + Traducir(mensaje));
    }

    private String Traducir(String mensaje) {
        String tipo = mensaje.split(":")[0].trim();
        return switch (tipo) {
            case "PACIENTE_REGISTRADO"   ->
                "Nuevo paciente registrado en el sistema.";
            case "CITA_REGISTRADA"       ->
                "Su cita ha sido registrada exitosamente.";
            case "CITA_CONFIRMADA"       ->
                "Su cita ha sido confirmada.";
            case "CITA_CANCELADA"        ->
                "Su cita ha sido cancelada.";
            case "CITA_ATENDIDA"         ->
                "Su cita fue atendida correctamente.";
            case "CITA_AUSENTE"          ->
                "El paciente no se presento a la cita.";
            case "ATENCION_REGISTRADA"   ->
                "La atencion medica ha sido registrada.";
            case "ATENCION_FINALIZADA"   ->
                "La atencion medica ha finalizado.";
            default -> "Notificacion del sistema: " + mensaje;
        };
    }
}