package com.citas.repository;


import com.citas.bean.entity.Cita;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CitaRepository extends ReactiveCrudRepository<Cita, Long> {

    Flux<Cita> findByPacienteId(Long pacienteId);

    Flux<Cita> findByEstado(String estado);
}