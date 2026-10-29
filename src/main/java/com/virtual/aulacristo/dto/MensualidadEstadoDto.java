package com.virtual.aulacristo.dto;

//AVCR-195: Determina estado Pagado/Pendiente
public class MensualidadEstadoDto {
    private String concepto;
    private String estado; // "Pagado" o "Pendiente"
    private Double monto;
    private String fechaPago;

    public MensualidadEstadoDto(String concepto, String estado, Double monto, String fechaPago) {
        this.concepto = concepto;
        this.estado = estado;
        this.monto = monto;
        this.fechaPago = fechaPago;
    }

    // Getters y Setters
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getFechaPago() { return fechaPago; }
    public void setFechaPago(String fechaPago) { this.fechaPago = fechaPago; }
}
