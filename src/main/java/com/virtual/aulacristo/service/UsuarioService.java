package com.virtual.aulacristo.service;

import com.virtual.aulacristo.entity.Rol;
import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.repository.RolRepository;
import com.virtual.aulacristo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    // AVCR-74: Métodos específicos de validación
    public boolean existePorUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public void validarUsernameUnico(String username) {
        if (existePorUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario o identificador principal ya está registrado.");
        }
    }

    public void validarUsernameParaActualizar(Long idUsuarioActual, String nuevoUsername) {
        usuarioRepository.findByUsername(nuevoUsername).ifPresent(usuarioEncontrado -> {
            if (!usuarioEncontrado.getIdUsuario().equals(idUsuarioActual)) {
                throw new IllegalArgumentException(
                        "El nuevo nombre de usuario ya se encuentra en uso por otra cuenta.");
            }
        });
    }

    // Operaciones CRUD
    // ==========================================

    // AVCR-70: Implementar registro de usuario
    public Usuario registrarUsuario(Usuario usuario) {
        validarUsernameUnico(usuario.getUsername());

        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol para el usuario.");
        }

        Rol rolAsignado = rolRepository.findById(usuario.getRol().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rol no encontrado con ID: " + usuario.getRol().getId()));
        usuario.setRol(rolAsignado);

        usuario.setEstado(true);
        return usuarioRepository.save(usuario);
    }

    // AVCR-71: Implementar consulta/listado de usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // AVCR-72: Implementar actualización de usuario
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        validarUsernameParaActualizar(id, usuarioActualizado.getUsername());

        usuarioExistente.setUsername(usuarioActualizado.getUsername());

        if (usuarioActualizado.getRol() != null && usuarioActualizado.getRol().getId() != null) {
            Rol rolAsignado = rolRepository.findById(usuarioActualizado.getRol().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Rol no encontrado con ID: " + usuarioActualizado.getRol().getId()));
            usuarioExistente.setRol(rolAsignado);
        }

        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
            usuarioExistente.setPassword(usuarioActualizado.getPassword());
        }

        return usuarioRepository.save(usuarioExistente);
    }

    // AVCR-73: Implementar activación/desactivación de usuario
    public Usuario cambiarEstado(Long id, boolean nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        usuario.setEstado(nuevoEstado);
        return usuarioRepository.save(usuario);
    }

    // AVCR-80: Autenticar usuario para control de sesión y roles
    // AVCR-57: Validar usuario y contraseña
    public Usuario autenticar(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas."));

        if (!usuario.getPassword().equals(password)) {
            throw new IllegalArgumentException("Credenciales inválidas.");
        }

        // AVCR-59: Validar estado activo del usuario
        if (!usuario.getEstado()) {
            throw new IllegalArgumentException("La cuenta se encuentra desactivada.");
        }

        return usuario;
    }
}