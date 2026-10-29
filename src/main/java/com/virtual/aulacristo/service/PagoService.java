package com.virtual.aulacristo.service;

import com.virtual.aulacristo.dto.MensualidadEstadoDto;
import com.virtual.aulacristo.model.Beneficio;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.model.Pago;
import com.virtual.aulacristo.repository.BeneficioRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;
import com.virtual.aulacristo.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//AVCR-190: Crear servicio para registrar pagos
@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private BeneficioRepository beneficioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    private static final double MONTO_BASE_PENSION = 150.0;

    // Listar todos los pagos registrados
    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }

    // Buscar un pago por su ID
    public Optional<Pago> buscarPorId(Long id) {
        return pagoRepository.findById(id);
    }

    // Listar pagos asociados a un estudiante específico
    public List<Pago> listarPorEstudiante(Long estudianteId) {

        if (estudianteId == null) {
            return pagoRepository.findAll();
        }
        return pagoRepository.findByEstudianteId(estudianteId);
    }

    // Registrar o guardar un nuevo pago validando que el estudiante exista
    public Pago guardarPago(Pago pago, Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + estudianteId));

        pago.setEstudiante(estudiante);
        return pagoRepository.save(pago);
    }

    public List<MensualidadEstadoDto> obtenerEstadoMensualidadesPorAnio(Long estudianteId, Integer anio) {
        // Si no se pasa un año, por defecto tomamos el actual (2026)
        // AVCR-198: Implementar filtro por periodo/año
        int anioConsulta = (anio != null) ? anio : 2026;

        List<String> conceptosEsperados = Arrays.asList(
                "Matricula " + anioConsulta, "Pension Marzo", "Pension Abril", "Pension Mayo",
                "Pension Junio", "Pension Julio", "Pension Agosto", "Pension Septiembre",
                "Pension Octubre", "Pension Noviembre", "Pension Diciembre"
        );

        List<Pago> pagosRealizados = listarPorEstudiante(estudianteId);
        List<MensualidadEstadoDto> resultado = new ArrayList<>();

        for (String conceptoEsperado : conceptosEsperados) {
            Pago pagoEncontrado = pagosRealizados.stream()
                    .filter(p -> p.getConcepto() != null && p.getConcepto().toLowerCase().contains(conceptoEsperado.toLowerCase().split(" ")[0]))
                    .findFirst()
                    .orElse(null);

            if (pagoEncontrado != null) {
                resultado.add(new MensualidadEstadoDto(
                        conceptoEsperado,
                        "Pagado",
                        pagoEncontrado.getMonto(),
                        pagoEncontrado.getFechaPago() != null ? pagoEncontrado.getFechaPago().toString() : "-"
                ));
            } else {
                resultado.add(new MensualidadEstadoDto(
                        conceptoEsperado,
                        "Pendiente",
                        0.0,
                        "-"
                ));
            }
        }

        return resultado;
    }

    // Eliminar un pago (por si se requiere corrección)
    public void eliminarPago(Long id) {
        pagoRepository.deleteById(id);
    }


    //AVCR-203: Aplicar el beneficio al calculo correspondiente
    public double calcularMontoConBeneficio(Long estudianteId, String anioPeriodo) {
        // Consultar si el estudiante tiene un beneficio para el período especificado
        Optional<Beneficio> beneficioOpt = beneficioRepository.findByEstudianteIdAndAnioPeriodo(estudianteId, anioPeriodo);

        if (beneficioOpt.isEmpty()) {
            return MONTO_BASE_PENSION; // Sin descuento, paga el monto completo
        }

        Beneficio beneficio = beneficioOpt.get();
        double valorBeneficio = beneficio.getValor();
        String tipoBeneficio = beneficio.getTipo().toLowerCase();

        double montoFinal = MONTO_BASE_PENSION;

        // Lógica según el tipo de beneficio
        if (tipoBeneficio.contains("porcentaje") || tipoBeneficio.contains("media beca") || tipoBeneficio.contains("beca")) {
            // Si es un porcentaje o media beca (ej. valor = 50.0 para 50%)
            if (tipoBeneficio.contains("completa") || valorBeneficio >= 100.0) {
                montoFinal = 0.0; // Beca 100%
            } else {
                // Aplicar descuento porcentual
                montoFinal = MONTO_BASE_PENSION - (MONTO_BASE_PENSION * (valorBeneficio / 100.0));
            }
        } else if (tipoBeneficio.contains("fijo") || tipoBeneficio.contains("descuento")) {
            // Si es un descuento de monto fijo en soles
            montoFinal = Math.max(0.0, MONTO_BASE_PENSION - valorBeneficio);
        }

        return montoFinal;
    }
}
