package com.cjconfecciones.back.reports;

public class AbonoReporte {
    private String id;
    private String fecha;
    private String valor;

    public AbonoReporte(String id, String fecha, String valor) {
        this.id = id;
        this.fecha = fecha;
        this.valor = valor;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}