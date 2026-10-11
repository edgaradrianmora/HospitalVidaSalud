package com.atenciones.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaDto {
    private Long id;
    private Long pacienteId;
    private String especialidad;
    private String estado;
}