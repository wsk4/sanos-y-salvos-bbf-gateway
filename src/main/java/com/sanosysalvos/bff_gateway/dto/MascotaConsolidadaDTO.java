package com.sanosysalvos.bff_gateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MascotaConsolidadaDTO {

    private Integer idMascota;
    private String nombre;
    private String raza;
    private String estado;
    // Información proveniente del MS de Geolocalización
    private Double latitud;
    private Double longitud;
}
