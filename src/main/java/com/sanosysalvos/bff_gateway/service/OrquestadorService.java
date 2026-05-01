package com.sanosysalvos.bff_gateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
// ¡Importante! Importamos RestClient en lugar de RestTemplate
import org.springframework.web.client.RestClient;

import com.sanosysalvos.bff_gateway.dto.MascotaConsolidadaDTO;

@Service
public class OrquestadorService {

    @Value("${microservicio.mascotas.url}")
    private String mascotasUrl;

    @Value("${microservicio.geolocalizacion.url}")
    private String geolocalizacionUrl;

    // PUNTO 3: Usamos la API moderna RestClient
    private final RestClient restClient;

    public OrquestadorService() {
        this.restClient = RestClient.create();
    }

    public List<MascotaConsolidadaDTO> obtenerResumenDashboard() {

        List<Map<String, Object>> mascotas = null;
        List<Map<String, Object>> geolocalizaciones = null;

        // PUNTO 2: Manejo de Excepciones y Tolerancia a Fallos
        
        // 1. Pedir datos al Microservicio de Mascotas
        try {
            mascotas = restClient.get()
                    .uri(mascotasUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            // Si Mascotas falla, imprimimos el error en consola y retornamos lista vacía para no tumbar el Frontend
            System.err.println("⚠️ CRÍTICO: Falló la conexión con MS Mascotas: " + e.getMessage());
            return new ArrayList<>(); 
        }

        // 2. Pedir datos al Microservicio de Geolocalización
        try {
            geolocalizaciones = restClient.get()
                    .uri(geolocalizacionUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            // Si Geolocalización falla, capturamos el error pero NO detenemos la ejecución. 
            // Las variables de latitud y longitud quedarán en null, pero las mascotas se mostrarán.
            System.err.println("⚠️ ADVERTENCIA: Falló la conexión con MS Geolocalización: " + e.getMessage());
        }

        // 3. Crear la lista final que enviaremos a React
        List<MascotaConsolidadaDTO> consolidados = new ArrayList<>();

        if (mascotas != null) {
            for (Map<String, Object> mascota : mascotas) {
                Integer idMascota = (Integer) mascota.get("id");

                Map<String, Object> ubicacionMascota = null;
                // Verificamos que la lista de geolocalizaciones exista (no haya fallado)
                if (geolocalizaciones != null) {
                    ubicacionMascota = geolocalizaciones.stream()
                            .filter(geo -> idMascota.equals(geo.get("mascotaId")))
                            .findFirst()
                            .orElse(null);
                }

                // 4. Construimos el DTO
                MascotaConsolidadaDTO dto = MascotaConsolidadaDTO.builder()
                        .idMascota(idMascota)
                        .nombre((String) mascota.get("nombre"))
                        .raza((String) mascota.get("raza"))
                        .estado((String) mascota.get("estado"))
                        // Si se cayó el MS de Geolocalización, ubicacionMascota será null y esto no dará error
                        .latitud(ubicacionMascota != null ? (Double) ubicacionMascota.get("latitud") : null)
                        .longitud(ubicacionMascota != null ? (Double) ubicacionMascota.get("longitud") : null)
                        .build();

                consolidados.add(dto);
            }
        }

        // 5. Retornamos la lista combinada (con o sin geolocalizaciones, pero siempre funcional)
        return consolidados;
    }
}