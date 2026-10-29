package com.virtual.aulacristo.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.virtual.aulacristo.model.Estudiante;

// AVCR-210: Servicio para exportar el reporte de estudiantes
@Service
public class ReporteExportService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] ENCABEZADOS = {
            "Código", "Apellidos", "Nombres", "DNI",
            "Correo", "Teléfono", "Fecha Nac.",
            "Nivel", "Grado", "Sección", "Estado",
            "Fecha Registro"
    };

    // ──────────────────────────────────────────
    // Exportar a Excel (.xlsx)
    // ──────────────────────────────────────────
    public byte[] generarExcel(List<Estudiante> estudiantes) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet hoja = workbook.createSheet("Estudiantes");

            // Estilo del encabezado
            CellStyle estiloEncabezado = workbook.createCellStyle();
            XSSFFont fuenteEncabezado = (XSSFFont) workbook.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setFillForegroundColor(
                    IndexedColors.DARK_BLUE.getIndex());
            estiloEncabezado.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
            estiloEncabezado.setBorderBottom(BorderStyle.THIN);
            estiloEncabezado.setBorderTop(BorderStyle.THIN);
            estiloEncabezado.setBorderLeft(BorderStyle.THIN);
            estiloEncabezado.setBorderRight(BorderStyle.THIN);

            // Fila de encabezados
            Row filaEncabezado = hoja.createRow(0);
            for (int i = 0; i < ENCABEZADOS.length; i++) {
                var celda = filaEncabezado.createCell(i);
                celda.setCellValue(ENCABEZADOS[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            // Estilo de las celdas de datos
            CellStyle estiloDatos = workbook.createCellStyle();
            estiloDatos.setBorderBottom(BorderStyle.THIN);
            estiloDatos.setBorderTop(BorderStyle.THIN);
            estiloDatos.setBorderLeft(BorderStyle.THIN);
            estiloDatos.setBorderRight(BorderStyle.THIN);

            // Filas de datos
            int numFila = 1;
            for (Estudiante est : estudiantes) {
                Row fila = hoja.createRow(numFila++);

                crearCelda(fila, 0, est.getCodigo(), estiloDatos);
                crearCelda(fila, 1, est.getApellidos(), estiloDatos);
                crearCelda(fila, 2, est.getNombres(), estiloDatos);
                crearCelda(fila, 3, est.getDni(), estiloDatos);
                crearCelda(fila, 4, est.getCorreo(), estiloDatos);
                crearCelda(fila, 5,
                        est.getTelefono() != null
                                ? est.getTelefono() : "—",
                        estiloDatos);
                crearCelda(fila, 6,
                        est.getFechaNacimiento() != null
                                ? est.getFechaNacimiento()
                                .format(FORMATO_FECHA) : "—",
                        estiloDatos);
                crearCelda(fila, 7, est.getNivel(), estiloDatos);
                crearCelda(fila, 8, est.getGrado(), estiloDatos);
                crearCelda(fila, 9, est.getSeccion(), estiloDatos);
                crearCelda(fila, 10,
                        est.isActivo() ? "Activo" : "Inactivo",
                        estiloDatos);
                crearCelda(fila, 11,
                        est.getFechaRegistro() != null
                                ? est.getFechaRegistro()
                                .format(FORMATO_FECHA) : "—",
                        estiloDatos);
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < ENCABEZADOS.length; i++) {
                hoja.autoSizeColumn(i);
            }

            workbook.write(salida);
            return salida.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al generar el archivo Excel: "
                            + e.getMessage(), e);
        }
    }

    private void crearCelda(Row fila, int columna,
                            String valor, CellStyle estilo) {
        var celda = fila.createCell(columna);
        celda.setCellValue(valor != null ? valor : "");
        celda.setCellStyle(estilo);
    }

    // ──────────────────────────────────────────
    // Exportar a PDF
    // ──────────────────────────────────────────
    public byte[] generarPdf(List<Estudiante> estudiantes) {

        try (ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            // Orientación horizontal para las 12 columnas
            Document documento = new Document(
                    PageSize.A4.rotate(), 20, 20, 30, 30);

            PdfWriter.getInstance(documento, salida);
            documento.open();

            // Título del reporte
            Font fuenteTitulo = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 16);
            Paragraph titulo = new Paragraph(
                    "Reporte de Estudiantes", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10);
            documento.add(titulo);

            // Subtítulo con total
            Font fuenteSubtitulo = FontFactory.getFont(
                    FontFactory.HELVETICA, 10);
            Paragraph subtitulo = new Paragraph(
                    "Total de estudiantes: "
                            + estudiantes.size(),
                    fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(15);
            documento.add(subtitulo);

            // Tabla con 12 columnas
            PdfPTable tabla = new PdfPTable(
                    ENCABEZADOS.length);
            tabla.setWidthPercentage(100);

            // Anchos proporcionales de columnas
            float[] anchos = {
                    12f, 9f, 9f, 7f, 12f, 7f,
                    7f, 7f, 9f, 5f, 5f, 7f
            };
            tabla.setWidths(anchos);

            // Encabezados
            Font fuenteEncabezado = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 7,
                    java.awt.Color.WHITE);

            for (String encabezado : ENCABEZADOS) {
                PdfPCell celda = new PdfPCell(
                        new Phrase(encabezado, fuenteEncabezado));
                celda.setBackgroundColor(
                        new java.awt.Color(30, 58, 138));
                celda.setHorizontalAlignment(
                        Element.ALIGN_CENTER);
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            // Datos
            Font fuenteDatos = FontFactory.getFont(
                    FontFactory.HELVETICA, 6);

            for (Estudiante est : estudiantes) {
                agregarCeldaPdf(tabla, est.getCodigo(),
                        fuenteDatos);
                agregarCeldaPdf(tabla, est.getApellidos(),
                        fuenteDatos);
                agregarCeldaPdf(tabla, est.getNombres(),
                        fuenteDatos);
                agregarCeldaPdf(tabla, est.getDni(),
                        fuenteDatos);
                agregarCeldaPdf(tabla, est.getCorreo(),
                        fuenteDatos);
                agregarCeldaPdf(tabla,
                        est.getTelefono() != null
                                ? est.getTelefono() : "—",
                        fuenteDatos);
                agregarCeldaPdf(tabla,
                        est.getFechaNacimiento() != null
                                ? est.getFechaNacimiento()
                                .format(FORMATO_FECHA) : "—",
                        fuenteDatos);
                agregarCeldaPdf(tabla, est.getNivel(),
                        fuenteDatos);
                agregarCeldaPdf(tabla, est.getGrado(),
                        fuenteDatos);
                agregarCeldaPdf(tabla, est.getSeccion(),
                        fuenteDatos);
                agregarCeldaPdf(tabla,
                        est.isActivo() ? "Activo" : "Inactivo",
                        fuenteDatos);
                agregarCeldaPdf(tabla,
                        est.getFechaRegistro() != null
                                ? est.getFechaRegistro()
                                .format(FORMATO_FECHA) : "—",
                        fuenteDatos);
            }

            documento.add(tabla);
            documento.close();

            return salida.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al generar el archivo PDF: "
                            + e.getMessage(), e);
        }
    }

    private void agregarCeldaPdf(PdfPTable tabla,
                                 String valor, Font fuente) {
        PdfPCell celda = new PdfPCell(
                new Phrase(valor != null ? valor : "",
                        fuente));
        celda.setPadding(4);
        tabla.addCell(celda);
    }
}
