package com.veterinaria.modelos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.veterinaria.modelos.Enums.EstadoVenta;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
@Table(name = "ventas")
@Data
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora;

    @jakarta.persistence.Column(precision = 19, scale = 2)
    private BigDecimal total;

    // Cliente no es @Audited, por eso se declara NOT_AUDITED para evitar
    // EnversMappingException al arrancar el contexto
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // cascade = CascadeType.ALL hace que si guardo la venta, se guarden sus
    // detalles automáticamente
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    private EstadoVenta estado;

    // CajaDiaria no es @Audited, se declara NOT_AUDITED para evitar
    // EnversMappingException al arrancar el contexto
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id")
    private CajaDiaria caja;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.veterinaria.modelos.Enums.MetodoPago metodoPago;

    // Método de conveniencia para mantener sincronizada la relación bidireccional
    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}
