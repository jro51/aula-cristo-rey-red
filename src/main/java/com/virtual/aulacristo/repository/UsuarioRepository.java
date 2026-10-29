package com.virtual.aulacristo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Evitar usuarios duplicados
    boolean existsByUsername(String username);

    // Buscar usuario por nombre de usuario
    Optional<Usuario> findByUsername(String username);

    // Obtener solamente usuarios activos de un determinado rol
    List<Usuario> findByRol_NombreIgnoreCaseAndEstadoTrueOrderByUsernameAsc(
        String nombreRol
    );
}