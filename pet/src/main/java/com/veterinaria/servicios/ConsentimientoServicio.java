package com.veterinaria.servicios;

import com.veterinaria.dtos.ConsentimientoRequestDTO;
import com.veterinaria.dtos.ConsentimientoResponseDTO;
import com.veterinaria.modelos.Cirugia;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.ConsentimientoInformado;
import com.veterinaria.respositorios.CirugiaRepositorio;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.ConsentimientoRepositorio;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsentimientoServicio {

    private final ConsentimientoRepositorio consentimientoRepositorio;
    private final CirugiaRepositorio cirugiaRepositorio;
    private final ClienteRepositorio clienteRepositorio;

    @Transactional
    public ConsentimientoResponseDTO generarConsentimiento(ConsentimientoRequestDTO requestDTO) {
        Cirugia cirugia = cirugiaRepositorio.findById(requestDTO.getCirugiaId())
                .orElseThrow(() -> new EntityNotFoundException("Cirugía no encontrada con ID: " + requestDTO.getCirugiaId()));

        Cliente cliente = clienteRepositorio.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + requestDTO.getClienteId()));

        // Verificar si ya existe
        consentimientoRepositorio.findByCirugiaId(cirugia.getId()).ifPresent(c -> {
            throw new IllegalStateException("El consentimiento para esta cirugía ya ha sido generado.");
        });

        String textoLegalBase = "Yo, en mi calidad de propietario o responsable legal del paciente, autorizo a la clínica veterinaria a realizar el procedimiento quirúrgico denominado: " 
                                + cirugia.getTipoCirugia() + ". Comprendo que existe un riesgo operatorio clasificado como " 
                                + cirugia.getRiesgoOperatorio() + ". Acepto los términos y libro de responsabilidades al equipo facultativo ante imprevistos médicos.";

        ConsentimientoInformado consentimiento = new ConsentimientoInformado();
        consentimiento.setFechaEmision(LocalDateTime.now());
        consentimiento.setTextoLegal(textoLegalBase);
        consentimiento.setAceptadoPorCliente(false);
        consentimiento.setCirugia(cirugia);
        consentimiento.setCliente(cliente);

        ConsentimientoInformado guardado = consentimientoRepositorio.save(consentimiento);
        return mapearADTO(guardado);
    }

    @Transactional
    public ConsentimientoResponseDTO aceptarConsentimiento(Long id) {
        ConsentimientoInformado consentimiento = consentimientoRepositorio.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consentimiento no encontrado con ID: " + id));

        consentimiento.setAceptadoPorCliente(true);
        ConsentimientoInformado actualizado = consentimientoRepositorio.save(consentimiento);

        return mapearADTO(actualizado);
    }

    @Transactional(readOnly = true)
    public ConsentimientoResponseDTO obtenerPorCirugia(Long cirugiaId) {
        ConsentimientoInformado consentimiento = consentimientoRepositorio.findByCirugiaId(cirugiaId)
                .orElseThrow(() -> new EntityNotFoundException("Consentimiento no encontrado para la cirugía: " + cirugiaId));
        
        return mapearADTO(consentimiento);
    }

    private ConsentimientoResponseDTO mapearADTO(ConsentimientoInformado consentimiento) {
        ConsentimientoResponseDTO dto = new ConsentimientoResponseDTO();
        dto.setId(consentimiento.getId());
        dto.setFechaEmision(consentimiento.getFechaEmision());
        dto.setTextoLegal(consentimiento.getTextoLegal());
        dto.setAceptadoPorCliente(consentimiento.getAceptadoPorCliente());
        dto.setCirugiaId(consentimiento.getCirugia().getId());
        dto.setClienteId(consentimiento.getCliente().getId());
        dto.setClienteNombre(consentimiento.getCliente().getUsuario().getNombre() + " " + consentimiento.getCliente().getUsuario().getApellido());
        return dto;
    }
}
