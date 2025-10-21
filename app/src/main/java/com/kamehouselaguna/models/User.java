package com.kamehouselaguna.models;

import java.util.Date;

/**
 * Modelo de datos para los usuarios
 * Representa un usuario del sistema con su información y rol
 */
public class User {
    private String uid;
    private String email;
    private String rol;
    private Date fechaCreacion;
    private Date ultimoAcceso;

    // Constructor vacío requerido para Firestore
    public User() {
    }

    // Constructor con parámetros principales
    public User(String uid, String email, String rol) {
        this.uid = uid;
        this.email = email;
        this.rol = rol;
        this.fechaCreacion = new Date();
        this.ultimoAcceso = new Date();
    }

    // Getters y Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(Date ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    /**
     * Verifica si el usuario es administrador
     * @return true si el rol es "admin", false en caso contrario
     */
    public boolean isAdmin() {
        return "admin".equals(this.rol);
    }

    /**
     * Verifica si el usuario es un usuario normal
     * @return true si el rol es "usuario", false en caso contrario
     */
    public boolean isUsuario() {
        return "usuario".equals(this.rol);
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", rol='" + rol + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", ultimoAcceso=" + ultimoAcceso +
                '}';
    }
}
