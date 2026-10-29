package com.virtual.aulacristo.model;

import com.virtual.aulacristo.entity.Usuario;

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
    name = "asignaciones_docente",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_asignacion_docente_curso_grado_seccion",
            columnNames = {
                "docente_id",
                "curso_id",
                "grado_id",
                "seccion_id"
            }
        )
    }
)
public class AsignacionDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El docente es un usuario que tiene el rol DOCENTE
    @NotNull(message = "El docente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "docente_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_docente_usuario"
        )
    )
    private Usuario docente;

    @NotNull(message = "El curso es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "curso_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_docente_curso"
        )
    )
    private Curso curso;

    @NotNull(message = "El grado es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "grado_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_docente_grado"
        )
    )
    private Grado grado;

    @NotNull(message = "La sección es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "seccion_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_asignacion_docente_seccion"
        )
    )
    private Seccion seccion;

    @Column(nullable = false)
    private boolean activo = true;

    public AsignacionDocente() {
    }

    public AsignacionDocente(
            Long id,
            Usuario docente,
            Curso curso,
            Grado grado,
            Seccion seccion,
            boolean activo) {

        this.id = id;
        this.docente = docente;
        this.curso = curso;
        this.grado = grado;
        this.seccion = seccion;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getDocente() {
        return docente;
    }

    public void setDocente(Usuario docente) {
        this.docente = docente;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}