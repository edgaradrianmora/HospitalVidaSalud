package com.pacientes.controller;

import com.pacientes.bean.entity.Paciente;
import com.pacientes.model.PacienteModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteModel pacienteModel;

    @GetMapping
    public Flux<Paciente> Listar() {
        return pacienteModel.ListarPacientes();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Paciente>> BuscarPorId(@PathVariable Long id) {
        return pacienteModel.BuscarPorId(id)
                .map(p -> ResponseEntity.ok(p))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> Registrar(@RequestBody Paciente paciente) {
        return pacienteModel.Registrar(paciente)
                .map(p -> ResponseEntity.status(HttpStatus.CREATED)
                        .<Object>body(p))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .<Object>body(e.getMessage())));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> Actualizar(
            @PathVariable Long id,
            @RequestBody Paciente paciente) {
        return pacienteModel.Actualizar(id, paciente)
                .map(p -> ResponseEntity.ok().<Object>body(p))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .<Object>body(e.getMessage())));
    }

    @PutMapping("/{id}/estado/{estado}")
    public Mono<ResponseEntity<Object>> CambiarEstado(
            @PathVariable Long id,
            @PathVariable String estado) {
        return pacienteModel.CambiarEstado(id, estado)
                .map(p -> ResponseEntity.ok().<Object>body(p))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .<Object>body(e.getMessage())));
    }

    @GetMapping("/aviso")
    public Mono<String> Aviso() {
        return Mono.just("MS Pacientes - Hospital Vida Salud");
    }
}