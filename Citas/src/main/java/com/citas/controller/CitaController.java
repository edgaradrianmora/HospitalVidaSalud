package com.citas.controller;

import com.citas.bean.entity.Cita;
import com.citas.model.CitaModel;
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
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaModel citaModel;

    @GetMapping
    public Flux<Cita> Listar() {
        return citaModel.ListarCitas();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> BuscarPorId(@PathVariable Long id) {
        return citaModel.BuscarPorId(id)
                .map(c -> ResponseEntity.ok().<Object>body(c))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .<Object>body(e.getMessage())));
    }

    @GetMapping("/paciente/{pacienteId}")
    public Flux<Cita> BuscarPorPaciente(@PathVariable Long pacienteId) {
        return citaModel.BuscarPorPaciente(pacienteId);
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> Registrar(@RequestBody Cita cita) {
        return citaModel.Registrar(cita)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED)
                        .<Object>body(c))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .<Object>body(e.getMessage())));
    }

    @PutMapping("/{id}/estado/{estado}")
    public Mono<ResponseEntity<Object>> CambiarEstado(
            @PathVariable Long id,
            @PathVariable String estado) {
        return citaModel.CambiarEstado(id, estado)
                .map(c -> ResponseEntity.ok().<Object>body(c))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .<Object>body(e.getMessage())));
    }

    @GetMapping("/aviso")
    public Mono<String> Aviso() {
        return Mono.just("Citas - Hospital Vida Salud");
    }
}