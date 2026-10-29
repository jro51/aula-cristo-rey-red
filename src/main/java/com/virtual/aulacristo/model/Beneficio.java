package com.virtual.aulacristo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// AVCR-200: Crear modelo para beca/descuento
@Entity
@Table(name = "beneficios")
public class Beneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El tipo de beneficio es obligatorio")
    @Column(nullable = false)
    private String tipo; // Ej: Beca 100%, Media Beca 50%, Descuento Fijo

    @NotNull(message = "El valor o porcentaje es obligatorio")
    @DecimalMin(value = "0.0", message = "El valor no puede ser negativo")
    @Column(nullable = false)
    private Double valor; // Ej: 50.0 (porcentaje o monto de descuento)

    @NotBlank(message = "El período de aplicación es obligatorio")
    @Column(nullable = false)
    private String anioPeriodo; // Ej: 2026

    // AVCR-201: Implementar relación del beneficio con estudiante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getAnioPeriodo() {
        return anioPeriodo;
    }

    public void setAnioPeriodo(String anioPeriodo) {
        this.anioPeriodo = anioPeriodo;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }
}
