package com.bodega.pedidos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "El rol es obligatorio (ADMINISTRADOR, ENCARGADO o CAJERO)")
    @Column(nullable = false)
    private String rol; // "ROLE_ADMINISTRADOR", "ROLE_ENCARGADO", "ROLE_CAJERO"

    private String nombres;

    private Integer edad;

    private java.time.LocalDate fechaInicio;

    private java.time.LocalDate fechaFin;
}
