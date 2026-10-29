package com.virtual.aulacristo.model;

import java.time.LocalDate;

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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "asignaciones_academicas_estudiante",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_asignacion_estudiante_periodo",
            columnNames = {
                "estudiante_id",
                "periodo_academico_id"
            }
        )
    }
)
public class AsignacionAcademicaEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Estudiante que será asignado
    @NotNull(message = "El estudiante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "estudiante_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_academica_estudiante"
        )
    )
    private Estudiante estudiante;

    // Grado académico seleccionado
    @NotNull(message = "El grado es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "grado_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_academica_grado"
        )
    )
    private Grado grado;

    // Sección correspondiente al grado
    @NotNull(message = "La sección es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "seccion_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_academica_seccion"
        )
    )
    private Seccion seccion;

    // Período académico en el cual se realiza la asignación
    @NotNull(message = "El período académico es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "periodo_academico_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_academica_periodo"
        )
    )
    private PeriodoAcademico periodoAcademico;

    // Fecha en la que se registró la asignación
    @NotNull(message = "La fecha de asignación es obligatoria")
    @Column(
        name = "fecha_asignacion",
        nullable = false
    )
    private LocalDate fechaAsignacion = LocalDate.now();

    // Permite realizar eliminación lógica
    @Column(nullable = false)
    private boolean activo = true;

    public AsignacionAcademicaEstudiante() {
    }

    public AsignacionAcademicaEstudiante(
            Long id,
            Estudiante estudiante,
            Grado grado,
            Seccion seccion,
            PeriodoAcademico periodoAcademico,
            LocalDate fechaAsignacion,
            boolean activo) {

        this.id = id;
        this.estudiante = estudiante;
        this.grado = grado;
        this.seccion = seccion;
        this.periodoAcademico = periodoAcademico;
        this.fechaAsignacion = fechaAsignacion;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Grado getGrado() {
        return grado;
    }

    public void setGrado(Grado grado) {
        this.grado = grado;
    }

    public Seccion getSeccion() {
        return seccion;
    }

    public void setSeccion(Seccion seccion) {
        this.seccion = seccion;
    }

    public PeriodoAcademico getPeriodoAcademico() {
        return periodoAcademico;
    }

    public void setPeriodoAcademico(
            PeriodoAcademico periodoAcademico) {

        this.periodoAcademico = periodoAcademico;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(
            LocalDate fechaAsignacion) {

        this.fechaAsignacion = fechaAsignacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // El nivel se obtiene mediante el grado seleccionado
    public Nivel getNivel() {

        if (grado == null) {
            return null;
        }

        return grado.getNivel();
    }
}