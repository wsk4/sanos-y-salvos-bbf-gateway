package com.sanosysalvos.bff_gateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sanosysalvos.bff_gateway.dto.MascotaConsolidadaDTO;

@Service
public class OrquestadorService {

    @Value("${microservicio.mascotas.url}")
    private String mascotasUrl;

    @Value("${microservicio.geolocalizacion.url}")
    private String geolocalizacionUrl;

    private final RestTemplate restTemplate;

    public OrquestadorService() {
        this.restTemplate = new RestTemplate();
    }

    public List<MascotaConsolidadaDTO> obtenerResumenDashboard() {

        // 1. Pedir todos los datos al Microservicio de Mascotas
        ResponseEntity<List<Map<String, Object>>> responseMascotas = restTemplate.exchange(
                mascotasUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }
        );
        List<Map<String, Object>> mascotas = responseMascotas.getBody();

        // 2. Pedir todos los datos al Microservicio de Geolocalización
        ResponseEntity<List<Map<String, Object>>> responseGeo = restTemplate.exchange(
                geolocalizacionUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }
        );
        List<Map<String, Object>> geolocalizaciones = responseGeo.getBody();

        // 3. Crear la lista final que enviaremos a React
        List<MascotaConsolidadaDTO> consolidados = new ArrayList<>();

        if (mascotas != null) {
            // Recorremos cada mascota que llegó de la base de datos de Mascotas
            for (Map<String, Object> mascota : mascotas) {
                // Obtenemos el ID de la mascota (asegúrate de que el nombre del campo coincida con tu JSON real)
                Integer idMascota = (Integer) mascota.get("id");

                // Buscamos en la lista de geolocalizaciones si hay alguna que coincida con este ID
                Map<String, Object> ubicacionMascota = null;
                if (geolocalizaciones != null) {
                    ubicacionMascota = geolocalizaciones.stream()
                            // Asumimos que la geolocalización tiene un campo "mascotaId"
                            .filter(geo -> idMascota.equals(geo.get("mascotaId")))
                            .findFirst()
                            .orElse(null);
                }

                // 4. Construimos el DTO combinando ambas fuentes de información
                MascotaConsolidadaDTO dto = MascotaConsolidadaDTO.builder()
                        .idMascota(idMascota)
                        .nombre((String) mascota.get("nombre"))
                        .raza((String) mascota.get("raza"))
                        .estado((String) mascota.get("estado"))
                        // Si encontramos la ubicación, sacamos la latitud y longitud, si no, lo dejamos en null
                        .latitud(ubicacionMascota != null ? (Double) ubicacionMascota.get("latitud") : null)
                        .longitud(ubicacionMascota != null ? (Double) ubicacionMascota.get("longitud") : null)
                        .build();

                consolidados.add(dto);
            }
        }

        // 5. Retornamos la lista combinada
        return consolidados;
    }
}
