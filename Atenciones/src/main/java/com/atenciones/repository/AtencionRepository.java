package com.atenciones.repository;

import com.atenciones.bean.entity.Atencion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AtencionRepository extends ReactiveCrudRepository<Atencion, Long> {
    Flux<Atencion> findByPacienteId(Long pacienteId);
    Flux<Atencion> findByCitaId(Long citaId);
}