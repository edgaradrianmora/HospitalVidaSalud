package com.atenciones.model;

import java.time.LocalDateTime;
import com.atenciones.bean.entity.Atencion;
import com.atenciones.configuration.RabbitMQConfiguration;
import com.atenciones.repository.AtencionRepository;
import com.atenciones.usecase.CitaUseCase;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class AtencionModel {

    private final AtencionRepository atencionRepository;
    private final CitaUseCase citaUseCase;
    private final RabbitTemplate rabbitTemplate;

    public Flux<Atencion> ListarAtenciones() {
        return atencionRepository.findAll();
    }

    public Mono<Atencion> BuscarPorId(Long id) {
        return atencionRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Atencion no encontrada con id: " + id)));
    }

    public Flux<Atencion> BuscarPorPaciente(Long pacienteId) {
        return atencionRepository.findByPacienteId(pacienteId);
    }

    public Mono<Atencion> Registrar(Atencion atencion) {
        return citaUseCase.BuscarPorId(atencion.getCitaId())
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Cita no existe con id: "
                        + atencion.getCitaId())))
                .flatMap(cita -> {
                    if (!"CONFIRMADA".equals(cita.getEstado())) {
                        return Mono.<Atencion>error(new RuntimeException(
                            "La cita debe estar CONFIRMADA para registrar atencion"));
                    }
                    atencion.setPacienteId(cita.getPacienteId());
                    atencion.setFechaAtencion(LocalDateTime.now());
                    atencion.setEstado("REGISTRADA");
                    return atencionRepository.save(atencion)
                            .flatMap(guardada ->
                                Publicar(guardada, "ATENCION_REGISTRADA")
                                .thenReturn(guardada));
                });
    }

    public Mono<Atencion> CambiarEstado(Long id, String nuevoEstado) {
        return atencionRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Atencion no encontrada con id: " + id)))
                .flatMap(atencion -> {
                    atencion.setEstado(nuevoEstado);
                    return atencionRepository.save(atencion)
                            .flatMap(actualizada ->
                                Publicar(actualizada, "ATENCION_" + nuevoEstado)
                                .thenReturn(actualizada));
                });
    }

    private Mono<Void> Publicar(Atencion atencion, String tipoEvento) {
        String mensaje = tipoEvento + ": Atencion N. " + atencion.getId()
                + " - Paciente: " + atencion.getPacienteId()
                + " - Cita: " + atencion.getCitaId()
                + " - Area: " + atencion.getArea()
                + " - Estado: " + atencion.getEstado();

        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.EXCHANGE,
                RabbitMQConfiguration.ROUTING_KEY,
                mensaje);
            System.out.println("Evento publicado: " + mensaje);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}