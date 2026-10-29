package com.virtual.aulacristo.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "periodos_academicos",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_periodo_anio_numero",
            columnNames = {"anio_academico_id", "numero"}
        ),
        @UniqueConstraint(
            name = "uk_periodo_anio_nombre",
            columnNames = {"anio_academico_id", "nombre"}
        )
    }
)
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del periodo es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El número del periodo es obligatorio")
    @Min(value = 1, message = "El número del periodo debe ser mayor que cero")
    @Column(nullable = false)
    private Integer numero;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean vigente = false;

    @Column(nullable = false)
    private boolean activo = true;

    @NotNull(message = "El año académico es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "anio_academico_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_periodo_anio_academico"
        )
    )
    private AnioAcademico anioAcademico;

    public PeriodoAcademico() {
    }

    public PeriodoAcademico(
            String nombre,
            Integer numero,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            AnioAcademico anioAcademico) {

        this.nombre = nombre;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.anioAcademico = anioAcademico;
        this.vigente = false;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public AnioAcademico getAnioAcademico() {
        return anioAcademico;
    }

    public void setAnioAcademico(
            AnioAcademico anioAcademico) {

        this.anioAcademico = anioAcademico;
    }
}