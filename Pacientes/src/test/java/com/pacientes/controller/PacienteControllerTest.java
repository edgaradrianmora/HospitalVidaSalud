package com.pacientes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pacientes.bean.entity.Paciente;
import com.pacientes.model.PacienteModel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PacienteControllerTest {

    @Mock
    private PacienteModel pacienteModel;

    @InjectMocks
    private PacienteController controller;

    private Paciente paciente() {
        Paciente p = new Paciente();
        p.setId(1L);
        p.setNombres("Edgar");
        p.setApellidos("Mora");
        return p;
    }

    @Test
    void aviso_devuelveNombreDelServicio() {
        StepVerifier.create(controller.Aviso())
                .expectNext("MS Pacientes - Hospital Vida Salud")
                .verifyComplete();
    }

    @Test
    void listar_devuelveFlujoDePacientes() {
        when(pacienteModel.ListarPacientes()).thenReturn(Flux.just(paciente()));

        StepVerifier.create(controller.Listar())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void buscarPorId_existente_devuelve200() {
        when(pacienteModel.BuscarPorId(1L)).thenReturn(Mono.just(paciente()));

        StepVerifier.create(controller.BuscarPorId(1L))
                .assertNext(r -> assertEquals(200, r.getStatusCode().value()))
                .verifyComplete();
    }

    @Test
    void buscarPorId_inexistente_devuelve404() {
        when(pacienteModel.BuscarPorId(99L))
                .thenReturn(Mono.error(new RuntimeException("Paciente no encontrado con id: 99")));

        StepVerifier.create(controller.BuscarPorId(99L))
                .assertNext(r -> assertEquals(404, r.getStatusCode().value()))
                .verifyComplete();
    }

    @Test
    void registrar_exitoso_devuelve201() {
        Paciente p = paciente();
        when(pacienteModel.Registrar(p)).thenReturn(Mono.just(p));

        StepVerifier.create(controller.Registrar(p))
                .assertNext(r -> assertEquals(201, r.getStatusCode().value()))
                .verifyComplete();
    }

    @Test
    void registrar_duplicado_devuelve409() {
        Paciente p = paciente();
        when(pacienteModel.Registrar(p))
                .thenReturn(Mono.error(new RuntimeException("Ya existe un paciente con ese documento")));

        StepVerifier.create(controller.Registrar(p))
                .assertNext(r -> {
                    assertEquals(409, r.getStatusCode().value());
                    assertEquals("Ya existe un paciente con ese documento", r.getBody());
                })
                .verifyComplete();
    }
}
