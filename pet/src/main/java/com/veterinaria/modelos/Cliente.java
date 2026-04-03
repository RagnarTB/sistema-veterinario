package com.veterinaria.modelos;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String nombre;
    private String apellido;
    private String telefono;
    private String dni;
    // "Un cliente tiene Muchos pacientes"
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true) // mappedBy = "cliente": Le dice a
                                                                                      // Spring "No crees una tabla
                                                                                      // nueva para esto. El dueño de
                                                                                      // esta relación es la variable
                                                                                      // cliente que está en la clase
                                                                                      // Paciente".
    private List<Paciente> pacientes;// cascade = CascadeType.ALL, orphanRemoval = true: Significa que si eliminas a
                                     // un Cliente de la base de datos, automáticamente se eliminarán todas sus
                                     // mascotas para no dejar "perritos huérfanos" en el sistema sin dueño.

    private String email;
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private Boolean esInvitado = false; // Por defecto es false para no romper los clientes actuales

}
