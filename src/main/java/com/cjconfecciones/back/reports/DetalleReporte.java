package com.cjconfecciones.back.reports;

import java.math.BigDecimal;

public class DetalleReporte {
    private BigDecimal unidades;
    private String descripcion;
    private BigDecimal valorUnitario;
    private BigDecimal subTotal;
    private String id;

    public DetalleReporte(BigDecimal unidades, String descripcion, BigDecimal valorUnitario, BigDecimal subTotal, String id) {
        this.unidades = unidades;
        this.descripcion = descripcion;
        this.valorUnitario = valorUnitario;
        this.subTotal = subTotal;
        this.id = id;
    }

    public BigDecimal getUnidades() {
        return unidades;
    }

    public void setUnidades(BigDecimal unidades) {
        this.unidades = unidades;
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}