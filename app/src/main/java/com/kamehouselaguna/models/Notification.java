package com.kamehouselaguna.models;

import java.util.Date;

/**
 * Modelo de datos para las notificaciones
 * Representa una notificación que se envía a los usuarios
 */
public class Notification {
    private String id;
    private String titulo;
    private String mensaje;
    private Date fecha;
    private String autorId;
    private String autorEmail;

    // Constructor vacío requerido para Firestore
    public Notification() {
    }

    // Constructor con parámetros principales
    public Notification(String titulo, String mensaje, String autorId, String autorEmail) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = new Date();
        this.autorId = autorId;
        this.autorEmail = autorEmail;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getAutorId() {
        return autorId;
    }

    public void setAutorId(String autorId) {
        this.autorId = autorId;
    }

    public String getAutorEmail() {
        return autorEmail;
    }

    public void setAutorEmail(String autorEmail) {
        this.autorEmail = autorEmail;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", fecha=" + fecha +
                ", autorId='" + autorId + '\'' +
                ", autorEmail='" + autorEmail + '\'' +
                '}';
    }
}
