package com.example.tt2;

public class Ejercicio {
    private String idEjercicio;
    private String numeroEjercicio;
    private String nombre;
    private String tipo;
    private String descripcion;
    private String nivelDificultad;
    private String objetivo;

    public Ejercicio() {
        // Required for Firestore
    }

    public Ejercicio(String numeroEjercicio, String nombre, String tipo, String descripcion, String nivelDificultad, String objetivo) {
        this.numeroEjercicio = numeroEjercicio;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.nivelDificultad = nivelDificultad;
        this.objetivo = objetivo;
    }

    public Ejercicio(String idEjercicio, String numeroEjercicio, String nombre, String tipo, String descripcion, String nivelDificultad, String objetivo) {
        this.idEjercicio = idEjercicio;
        this.numeroEjercicio = numeroEjercicio;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.nivelDificultad = nivelDificultad;
        this.objetivo = objetivo;
    }

    public String getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(String idEjercicio) {
        this.idEjercicio = idEjercicio;
    }

    public String getNumeroEjercicio() {
        return numeroEjercicio;
    }

    public void setNumeroEjercicio(String numeroEjercicio) {
        this.numeroEjercicio = numeroEjercicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNivelDificultad() {
        return nivelDificultad;
    }

    public void setNivelDificultad(String nivelDificultad) {
        this.nivelDificultad = nivelDificultad;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }
}
