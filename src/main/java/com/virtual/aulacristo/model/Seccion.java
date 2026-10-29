package com.virtual.aulacristo.model;

import jakarta.persistence.UniqueConstraint;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "secciones",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_seccion_nombre_grado",
            columnNames = {
                "nombre",
                "grado_id"
            }
        )
    }
)
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la sección es obligatorio")
    @Column(nullable = false, length = 20)
    private String nombre;

    @Column(length = 150)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    // AVCR-127: Muchas secciones pueden pertenecer a un grado
    @NotNull(message = "El grado es obligatorio")
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "grado_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_seccion_grado"
        )
    )
    private Grado grado;

    public Seccion() {
    }

    public Seccion(
            Long id,
            String nombre,
            String descripcion,
            boolean activo,
            Grado grado) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.grado = grado;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Grado getGrado() {
        return grado;
    }

    public void setGrado(Grado grado) {
        this.grado = grado;
    }
}