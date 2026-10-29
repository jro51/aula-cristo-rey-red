package com.virtual.aulacristo.service;

import com.virtual.aulacristo.model.DocumentoMatricula;
import com.virtual.aulacristo.model.Prematricula;
import com.virtual.aulacristo.repository.DocumentoMatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoMatriculaRepository documentoRepository;

    @Autowired
    private PrematriculaService prematriculaService;

    @Autowired
    private FileStorageService fileStorageService;

    public List<DocumentoMatricula> listarDocumentosPorSolicitud(Long prematriculaId) {
        return documentoRepository.findByPrematriculaId(prematriculaId);
    }

    @Transactional
    public void subirDocumento(Long prematriculaId, String tipoDocumento, MultipartFile archivo) {
        Prematricula solicitud = prematriculaService.obtenerSolicitudPorId(prematriculaId);

        // AVCR-178: Permitir reemplazar un documento antes de enviar
        Optional<DocumentoMatricula> docExistente = documentoRepository.findByPrematriculaIdAndTipoDocumento(prematriculaId, tipoDocumento);
        
        DocumentoMatricula documento;
        if (docExistente.isPresent()) {
            documento = docExistente.get();
            // Eliminamos el archivo físico viejo para no llenar el disco
            fileStorageService.eliminarArchivo(documento.getRutaArchivo());
        } else {
            documento = new DocumentoMatricula();
            documento.setPrematricula(solicitud);
            documento.setTipoDocumento(tipoDocumento);
        }

        // Guardamos el nuevo archivo físico
        String rutaRelativa = fileStorageService.almacenarArchivo(archivo, prematriculaId);
        
        documento.setNombreArchivoOriginal(archivo.getOriginalFilename());
        documento.setRutaArchivo(rutaRelativa);

        documentoRepository.save(documento);
    }
}