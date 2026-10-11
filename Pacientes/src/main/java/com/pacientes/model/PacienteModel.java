package com.pacientes.model;

import java.time.LocalDateTime;

import com.pacientes.bean.entity.Paciente;
import com.pacientes.configuration.RabbitMQConfiguration;
import com.pacientes.repository.PacienteRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class PacienteModel {

    private final PacienteRepository pacienteRepository;
    private final RabbitTemplate rabbitTemplate;

    public Flux<Paciente> ListarPacientes() {
        return pacienteRepository.findAll();
    }

    public Mono<Paciente> BuscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Paciente no encontrado con id: " + id)));
    }

    public Mono<Paciente> Registrar(Paciente paciente) {
        return pacienteRepository
                .findByTipoDocumentoAndNumeroDocumento(
                    paciente.getTipoDocumento(),
                    paciente.getNumeroDocumento())
                .flatMap(existente -> Mono.<Paciente>error(
                    new RuntimeException("Ya existe un paciente con ese documento")))
                .switchIfEmpty(Mono.defer(() -> {
                    paciente.setEstado("INACTIVO");
                    paciente.setFechaRegistro(LocalDateTime.now());
                    return pacienteRepository.save(paciente)
                            .flatMap(guardado ->
                                Publicar(guardado, "PACIENTE_REGISTRADO")
                                .thenReturn(guardado));
                }));
    }

    public Mono<Paciente> Actualizar(Long id, Paciente datos) {
        return pacienteRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Paciente no encontrado con id: " + id)))
                .flatMap(existente -> {
                    existente.setNombres(datos.getNombres());
                    existente.setApellidos(datos.getApellidos());
                    existente.setTelefono(datos.getTelefono());
                    existente.setCorreo(datos.getCorreo());
                    existente.setDireccion(datos.getDireccion());
                    return pacienteRepository.save(existente);
                });
    }

    public Mono<Paciente> CambiarEstado(Long id, String estado) {
        return pacienteRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Paciente no encontrado con id: " + id)))
                .flatMap(existente -> {
                    existente.setEstado(estado);
                    return pacienteRepository.save(existente);
                });
    }

    private Mono<Void> Publicar(Paciente paciente, String tipoEvento) {
        String mensaje = tipoEvento + ": Paciente N. " + paciente.getId()
                + " - " + paciente.getNombres() + " " + paciente.getApellidos()
                + " - Documento: " + paciente.getTipoDocumento()
                + " " + paciente.getNumeroDocumento();

        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.EXCHANGE,
                RabbitMQConfiguration.ROUTING_KEY,
                mensaje);
            System.out.println("Evento publicado: " + mensaje);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}