package com.virtual.aulacristo.model;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cursos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_curso_codigo", columnNames = { "codigo" }),
        @UniqueConstraint(name = "uk_curso_nombre_grado", columnNames = { "nombre", "grado_id" })
})
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código del curso es obligatorio")
    @Column(nullable = false, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @NotNull(message = "Las horas semanales son obligatorias")
    @Min(value = 1, message = "Las horas semanales deben ser mayores que cero")
    @Column(name = "horas_semanales", nullable = false)
    private Integer horasSemanales;

    @Column(nullable = false)
    private boolean activo = true;

    // AVCR-142: Cada curso pertenece a un grado
    @NotNull(message = "El grado es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grado_id", nullable = false, foreignKey = @ForeignKey(name = "fk_curso_grado"))
    private Grado grado;

    public Curso() {
    }

    public Curso(
            Long id,
            String codigo,
            String nombre,
            String descripcion,
            Integer horasSemanales,
            boolean activo,
            Grado grado) {

        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.horasSemanales = horasSemanales;
        this.activo = activo;
        this.grado = grado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Integer getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(Integer horasSemanales) {
        this.horasSemanales = horasSemanales;
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