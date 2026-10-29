package com.virtual.aulacristo.repository;

import com.virtual.aulacristo.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByEstudianteId(Long estudianteId);
}
