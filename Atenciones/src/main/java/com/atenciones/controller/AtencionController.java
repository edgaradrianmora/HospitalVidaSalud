package com.atenciones.controller;

import com.atenciones.bean.entity.Atencion;
import com.atenciones.model.AtencionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/atenciones")
public class AtencionController {

    private final AtencionModel atencionModel;

    @GetMapping
    public Flux<Atencion> Listar() {
        return atencionModel.ListarAtenciones();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> BuscarPorId(@PathVariable Long id) {
        return atencionModel.BuscarPorId(id)
                .map(a -> ResponseEntity.ok().<Object>body(a))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .<Object>body(e.getMessage())));
    }

    @GetMapping("/paciente/{pacienteId}")
    public Flux<Atencion> BuscarPorPaciente(@PathVariable Long pacienteId) {
        return atencionModel.BuscarPorPaciente(pacienteId);
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> Registrar(@RequestBody Atencion atencion) {
        return atencionModel.Registrar(atencion)
                .map(a -> ResponseEntity.status(HttpStatus.CREATED)
                        .<Object>body(a))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .<Object>body(e.getMessage())));
    }

    @PutMapping("/{id}/estado/{estado}")
    public Mono<ResponseEntity<Object>> CambiarEstado(
            @PathVariable Long id,
            @PathVariable String estado) {
        return atencionModel.CambiarEstado(id, estado)
                .map(a -> ResponseEntity.ok().<Object>body(a))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .<Object>body(e.getMessage())));
    }

    @GetMapping("/aviso")
    public Mono<String> Aviso() {
        return Mono.just("MS Atenciones - Hospital Vida Salud");
    }
}