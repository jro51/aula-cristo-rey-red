package com.virtual.aulacristo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MatriculaUpdateDto {

    @NotBlank(message = "El año académico es obligatorio")
    private String anioAcademico;

    @NotBlank(message = "El grado es obligatorio")
    private String estudianteGrado;

    @NotBlank(message = "El correo del estudiante es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String estudianteCorreo;

    private String estudianteTelefono;

    @NotBlank(message = "El correo del apoderado es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String apoderadoCorreo;

    private String apoderadoTelefono;

    // Genera todos los Getters y Setters
    public String getAnioAcademico() { return anioAcademico; }
    public void setAnioAcademico(String anioAcademico) { this.anioAcademico = anioAcademico; }
    public String getEstudianteGrado() { return estudianteGrado; }
    public void setEstudianteGrado(String estudianteGrado) { this.estudianteGrado = estudianteGrado; }
    public String getEstudianteCorreo() { return estudianteCorreo; }
    public void setEstudianteCorreo(String estudianteCorreo) { this.estudianteCorreo = estudianteCorreo; }
    public String getEstudianteTelefono() { return estudianteTelefono; }
    public void setEstudianteTelefono(String estudianteTelefono) { this.estudianteTelefono = estudianteTelefono; }
    public String getApoderadoCorreo() { return apoderadoCorreo; }
    public void setApoderadoCorreo(String apoderadoCorreo) { this.apoderadoCorreo = apoderadoCorreo; }
    public String getApoderadoTelefono() { return apoderadoTelefono; }
    public void setApoderadoTelefono(String apoderadoTelefono) { this.apoderadoTelefono = apoderadoTelefono; }
}