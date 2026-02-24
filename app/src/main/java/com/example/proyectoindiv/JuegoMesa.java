package com.example.proyectoindiv;

public class JuegoMesa {
    private int id;
    private String nombre;
    private String jugadores;
    private int duracion;
    private int jugado;
    private int propiedad;
    private String fecha;

    // Constructor
    public JuegoMesa(int id, String nombre, String jugadores, int duracion, int jugado, int propiedad, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.jugadores = jugadores;
        this.duracion = duracion;
        this.jugado = jugado;
        this.propiedad = propiedad;
        this.fecha = fecha;

    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getJugadores() { return jugadores; }
    public void setJugadores(String jugadores) { this.jugadores = jugadores; }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    public int getJugado() { return jugado; }
    public void setJugado(int jugado) { this.jugado = jugado; }

    public int getPropiedad() { return propiedad; }
    public void setPropiedad(int propiedad) { this.propiedad = propiedad; }
    public String getFecha() { return fecha; }
}