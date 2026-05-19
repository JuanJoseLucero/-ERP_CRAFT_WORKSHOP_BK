package com.cjconfecciones.back.reports;

import java.math.BigDecimal;

public class AbonoReporte {
    private String id;
    private String fecha;
    private BigDecimal valor;

    public AbonoReporte(String id, String fecha, BigDecimal valor) {
        this.id = id;
        this.fecha = fecha;
        this.valor = valor;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}