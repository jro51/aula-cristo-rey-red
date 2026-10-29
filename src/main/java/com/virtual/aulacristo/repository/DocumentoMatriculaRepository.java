package com.virtual.aulacristo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.DocumentoMatricula;

@Repository
public interface DocumentoMatriculaRepository extends JpaRepository<DocumentoMatricula, Long> {
    
    List<DocumentoMatricula> findByPrematriculaId(Long prematriculaId);
    
    Optional<DocumentoMatricula> findByPrematriculaIdAndTipoDocumento(Long prematriculaId, String tipoDocumento);
}