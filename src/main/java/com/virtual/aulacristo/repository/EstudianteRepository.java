package com.virtual.aulacristo.repository;

import java.util.Optional;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.Estudiante;

@Repository 
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    // AVCR-90: Validar si existe un estudiante con el mismo DNI
    boolean existsByDni(String dni);

    // AVCR-93: Buscar estudiante por código o DNI
    Optional<Estudiante> findByCodigoIgnoreCaseOrDni(
            String codigo,
            String dni
    );
    // AVCR-98: Buscar estudiantes aplicando filtros básicos
@Query("""
    SELECT e
    FROM Estudiante e
    WHERE (
        :texto IS NULL
        OR :texto = ''
        OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR e.dni LIKE CONCAT('%', :texto, '%')
        OR LOWER(e.nombres) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR LOWER(CONCAT(e.nombres, ' ', e.apellidos))
            LIKE LOWER(CONCAT('%', :texto, '%'))
    )
    AND (
        :grado IS NULL
        OR :grado = ''
        OR e.grado = :grado
    )
    AND (
        :seccion IS NULL
        OR :seccion = ''
        OR e.seccion = :seccion
    )
    AND (
        :activo IS NULL
        OR e.activo = :activo
    )
    ORDER BY e.apellidos ASC, e.nombres ASC
    """)
List<Estudiante> buscarConFiltros(
        @Param("texto") String texto,
        @Param("grado") String grado,
        @Param("seccion") String seccion,
        @Param("activo") Boolean activo
);

    // AVCR-206 y AVCR-207: Consulta para el reporte de estudiantes
    // con filtros por nivel, grado y sección
    @Query("""
        SELECT e
        FROM Estudiante e
        WHERE (
            :nivel IS NULL
            OR :nivel = ''
            OR LOWER(e.grado) LIKE LOWER(CONCAT('%', :nivel, '%'))
        )
        AND (
            :grado IS NULL
            OR :grado = ''
            OR e.grado = :grado
        )
        AND (
            :seccion IS NULL
            OR :seccion = ''
            OR e.seccion = :seccion
        )
        ORDER BY e.apellidos ASC, e.nombres ASC
        """)
    List<Estudiante> buscarParaReporte(
            @Param("nivel") String nivel,
            @Param("grado") String grado,
            @Param("seccion") String seccion
    );
}