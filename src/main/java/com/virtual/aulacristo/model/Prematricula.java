package com.virtual.aulacristo.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "prematriculas")
public class Prematricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String codigoSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apoderado_id", nullable = false)
    private Apoderado apoderado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrematricula estado = EstadoPrematricula.PENDIENTE;

    @Column(nullable = false)
    private String anioAcademico;

    @Column(nullable = false, updatable = false)
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(length = 500)
    private String motivoObservacion;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoSolicitud() { return codigoSolicitud; }
    public void setCodigoSolicitud(String codigoSolicitud) { this.codigoSolicitud = codigoSolicitud; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Apoderado getApoderado() { return apoderado; }
    public void setApoderado(Apoderado apoderado) { this.apoderado = apoderado; }
    public EstadoPrematricula getEstado() { return estado; }
    public void setEstado(EstadoPrematricula estado) { this.estado = estado; }
    public String getAnioAcademico() { return anioAcademico; }
    public void setAnioAcademico(String anioAcademico) { this.anioAcademico = anioAcademico; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getMotivoObservacion() { return motivoObservacion; }
    public void setMotivoObservacion(String motivoObservacion) { this.motivoObservacion = motivoObservacion; }
}
