package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.virtual.aulacristo.model.Apoderado;
import com.virtual.aulacristo.repository.ApoderadoRepository;

@Service
public class ApoderadoService {

    @Autowired
    private ApoderadoRepository apoderadoRepository;

    public List<Apoderado> listarTodos() {
        return apoderadoRepository.findAll();
    }

    public Apoderado registrarApoderado(Apoderado apoderado) {
        if (apoderadoRepository.existsByDni(apoderado.getDni())) {
            throw new IllegalArgumentException("Ya existe un apoderado registrado con el DNI: " + apoderado.getDni());
        }
        return apoderadoRepository.save(apoderado);
    }

    public Apoderado obtenerApoderadoPorId(Long id) {
        return apoderadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apoderado no encontrado con ID: " + id));
    }

    public void actualizarApoderado(Long id, Apoderado apoderadoActualizado) {
        Apoderado apoderadoExistente = obtenerApoderadoPorId(id);

        apoderadoExistente.setNombres(apoderadoActualizado.getNombres());
        apoderadoExistente.setApellidos(apoderadoActualizado.getApellidos());
        apoderadoExistente.setTelefono(apoderadoActualizado.getTelefono());
        apoderadoExistente.setEmail(apoderadoActualizado.getEmail());
        apoderadoExistente.setDireccion(apoderadoActualizado.getDireccion());
        apoderadoExistente.setParentesco(apoderadoActualizado.getParentesco());
        apoderadoExistente.setEstado(apoderadoActualizado.isEstado());

        apoderadoRepository.save(apoderadoExistente);
    }
}
