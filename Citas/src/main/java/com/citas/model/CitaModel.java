package com.citas.model;

import java.time.LocalDateTime;

import com.citas.bean.entity.Cita;
import com.citas.configuration.RabbitMQConfiguration;
import com.citas.repository.CitaRepository;
import com.citas.usecase.PacienteUseCase;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class CitaModel {

    private final CitaRepository citaRepository;
    private final PacienteUseCase pacienteUseCase;
    private final RabbitTemplate rabbitTemplate;

    public Flux<Cita> ListarCitas() {
        return citaRepository.findAll();
    }

    public Mono<Cita> BuscarPorId(Long id) {
        return citaRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Cita no encontrada con id: " + id)));
    }

    public Flux<Cita> BuscarPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    public Mono<Cita> Registrar(Cita cita) {
        return pacienteUseCase.BuscarPorId(cita.getPacienteId())
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Paciente no existe con id: "
                        + cita.getPacienteId())))
                .flatMap(paciente -> {
                    if (!"ACTIVO".equals(paciente.getEstado())) {
                        return Mono.error(
                            new RuntimeException("El paciente no esta ACTIVO"));
                    }
                    cita.setEstado("PENDIENTE");
                    cita.setFechaCreacion(LocalDateTime.now());
                    cita.setFechaActualizacion(LocalDateTime.now());
                    return citaRepository.save(cita)
                            .flatMap(guardada ->
                                Publicar(guardada, "CITA_REGISTRADA")
                                .thenReturn(guardada));
                });
    }

    public Mono<Cita> CambiarEstado(Long id, String nuevoEstado) {
        return citaRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Cita no encontrada con id: " + id)))
                .flatMap(cita -> {
                    ValidarTransicion(cita.getEstado(), nuevoEstado);
                    cita.setEstado(nuevoEstado);
                    cita.setFechaActualizacion(LocalDateTime.now());
                    return citaRepository.save(cita)
                            .flatMap(actualizada ->
                                Publicar(actualizada, "CITA_" + nuevoEstado)
                                .thenReturn(actualizada));
                });
    }

    private void ValidarTransicion(String estadoActual, String nuevoEstado) {
        boolean valido = switch (estadoActual) {
            case "PENDIENTE"   ->
                nuevoEstado.equals("CONFIRMADA") ||
                nuevoEstado.equals("CANCELADA");
            case "CONFIRMADA"  ->
                nuevoEstado.equals("ATENDIDA") ||
                nuevoEstado.equals("CANCELADA") ||
                nuevoEstado.equals("AUSENTE");
            default -> false;
        };
        if (!valido) {
            throw new RuntimeException(
                "Transicion no permitida: " + estadoActual + " -> " + nuevoEstado);
        }
    }

    private Mono<Void> Publicar(Cita cita, String tipoEvento) {
        String mensaje = tipoEvento + ": Cita N. " + cita.getId()
                + " - Paciente: " + cita.getPacienteId()
                + " - Especialidad: " + cita.getEspecialidad()
                + " - Estado: " + cita.getEstado();

        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.EXCHANGE,
                RabbitMQConfiguration.ROUTING_KEY,
                mensaje);
            System.out.println("Evento publicado: " + mensaje);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}