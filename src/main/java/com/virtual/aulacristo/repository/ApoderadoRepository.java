package com.virtual.aulacristo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.Apoderado;

@Repository
public interface ApoderadoRepository extends JpaRepository<Apoderado, Long> {
    boolean existsByDni(String dni);
    Optional<Apoderado> findByDni(String dni);
}