package com.citas.usecase;

import com.citas.bean.dto.PacienteDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class PacienteUseCase {

    private final WebClient.Builder webClientBuilder;

    public Mono<PacienteDto> BuscarPorId(Long pacienteId) {
        return webClientBuilder.build()
                .get()
                .uri("lb://Pacientes/api/pacientes/{id}", pacienteId)
                .retrieve()
                .bodyToMono(PacienteDto.class);
    }
}