package com.atenciones.bean.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("atencion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atencion {

    @Id
    private Long id;
    private Long pacienteId;
    private Long citaId;
    private LocalDateTime fechaAtencion;
    private String area;
    private String estado;
    private String observaciones;
}