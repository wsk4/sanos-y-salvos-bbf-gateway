package com.sanosysalvos.bff_gateway.controller;

// ¡No olvides importar tu DTO!
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanosysalvos.bff_gateway.dto.MascotaConsolidadaDTO;
import com.sanosysalvos.bff_gateway.service.OrquestadorService;

@RestController
@RequestMapping("/api/bff/v1")
public class BffController {

    private final OrquestadorService orquestadorService;

    public BffController(OrquestadorService orquestadorService) {
        this.orquestadorService = orquestadorService;
    }

    @GetMapping("/dashboard")
    // Cambiamos List<Object> por List<MascotaConsolidadaDTO>
    public ResponseEntity<List<MascotaConsolidadaDTO>> cargarDashboardPrincipal() {

        // El servicio ahora nos entrega la lista perfectamente estructurada y cruzada
        List<MascotaConsolidadaDTO> consolidado = orquestadorService.obtenerResumenDashboard();

        return ResponseEntity.ok(consolidado);
    }
}
