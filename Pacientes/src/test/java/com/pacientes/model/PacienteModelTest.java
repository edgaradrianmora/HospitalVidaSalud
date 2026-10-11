package com.pacientes.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.pacientes.bean.entity.Paciente;
import com.pacientes.configuration.RabbitMQConfiguration;
import com.pacientes.repository.PacienteRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PacienteModelTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PacienteModel pacienteModel;

    private Paciente nuevoPaciente() {
        Paciente p = new Paciente();
        p.setTipoDocumento("DNI");
        p.setNumeroDocumento("12345678");
        p.setNombres("Juan");
        p.setApellidos("Perez");
        p.setTelefono("999111222");
        p.setCorreo("juan@correo.com");
        p.setDireccion("Av. Lima 123");
        return p;
    }

    @Test
    void listarPacientes_devuelveTodos() {
        Paciente a = nuevoPaciente();
        Paciente b = nuevoPaciente();
        b.setNumeroDocumento("87654321");
        when(pacienteRepository.findAll()).thenReturn(Flux.just(a, b));

        StepVerifier.create(pacienteModel.ListarPacientes())
                .expectNext(a)
                .expectNext(b)
                .verifyComplete();
    }

    @Test
    void buscarPorId_existente_devuelvePaciente() {
        Paciente p = nuevoPaciente();
        p.setId(1L);
        when(pacienteRepository.findById(1L)).thenReturn(Mono.just(p));

        StepVerifier.create(pacienteModel.BuscarPorId(1L))
                .assertNext(r -> assertEquals("Juan", r.getNombres()))
                .verifyComplete();
    }

    @Test
    void buscarPorId_inexistente_lanzaError() {
        when(pacienteRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(pacienteModel.BuscarPorId(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("Paciente no encontrado con id: 99"))
                .verify();
    }

    @Test
    void registrar_nuevo_asignaEstadoActivoYPublicaEvento() {
        Paciente p = nuevoPaciente();
        when(pacienteRepository.findByTipoDocumentoAndNumeroDocumento("DNI", "12345678"))
                .thenReturn(Mono.empty());
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> {
            Paciente guardado = inv.getArgument(0);
            guardado.setId(1L);
            return Mono.just(guardado);
        });

        StepVerifier.create(pacienteModel.Registrar(p))
                .assertNext(r -> {
                    assertEquals(1L, r.getId());
                    assertEquals("ACTIVO", r.getEstado());
                    assertNotNull(r.getFechaRegistro());
                })
                .verifyComplete();

        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfiguration.EXCHANGE),
                eq(RabbitMQConfiguration.ROUTING_KEY),
                mensaje.capture());
        assertTrue(mensaje.getValue().toString().contains("PACIENTE_REGISTRADO"));
    }

    @Test
    void registrar_documentoDuplicado_lanzaErrorYNoGuarda() {
        Paciente existente = nuevoPaciente();
        existente.setId(5L);
        when(pacienteRepository.findByTipoDocumentoAndNumeroDocumento("DNI", "12345678"))
                .thenReturn(Mono.just(existente));

        StepVerifier.create(pacienteModel.Registrar(nuevoPaciente()))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("Ya existe un paciente con ese documento"))
                .verify();

        verify(pacienteRepository, never()).save(any(Paciente.class));
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void actualizar_existente_modificaDatosContacto() {
        Paciente existente = nuevoPaciente();
        existente.setId(1L);
        Paciente datos = new Paciente();
        datos.setNombres("Juan Carlos");
        datos.setApellidos("Perez Diaz");
        datos.setTelefono("900000000");
        datos.setCorreo("nuevo@correo.com");
        datos.setDireccion("Jr. Cusco 456");

        when(pacienteRepository.findById(1L)).thenReturn(Mono.just(existente));
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(pacienteModel.Actualizar(1L, datos))
                .assertNext(r -> {
                    assertEquals("Juan Carlos", r.getNombres());
                    assertEquals("900000000", r.getTelefono());
                    assertEquals("nuevo@correo.com", r.getCorreo());
                    assertEquals("12345678", r.getNumeroDocumento());
                })
                .verifyComplete();
    }

    @Test
    void actualizar_inexistente_lanzaError() {
        when(pacienteRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(pacienteModel.Actualizar(99L, new Paciente()))
                .expectErrorMatches(e -> e.getMessage().contains("Paciente no encontrado"))
                .verify();

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void cambiarEstado_existente_actualizaEstado() {
        Paciente existente = nuevoPaciente();
        existente.setId(1L);
        existente.setEstado("ACTIVO");
        when(pacienteRepository.findById(1L)).thenReturn(Mono.just(existente));
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(pacienteModel.CambiarEstado(1L, "INACTIVO"))
                .assertNext(r -> assertEquals("INACTIVO", r.getEstado()))
                .verifyComplete();
    }

    @Test
    void cambiarEstado_inexistente_lanzaError() {
        when(pacienteRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(pacienteModel.CambiarEstado(99L, "INACTIVO"))
                .expectErrorMatches(e -> e.getMessage().contains("Paciente no encontrado"))
                .verify();
    }
}
