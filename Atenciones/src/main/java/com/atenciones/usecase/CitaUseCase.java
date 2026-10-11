package com.atenciones.usecase;
import com.atenciones.bean.dto.CitaDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class CitaUseCase {

    private final WebClient.Builder webClientBuilder;

    public Mono<CitaDto> BuscarPorId(Long citaId) {
        return webClientBuilder.build()
                .get()
                .uri("lb://Citas/api/citas/{id}", citaId)
                .retrieve()
                .bodyToMono(CitaDto.class);
    }
}