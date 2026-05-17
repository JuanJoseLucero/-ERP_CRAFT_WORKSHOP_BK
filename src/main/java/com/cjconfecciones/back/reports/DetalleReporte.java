package com.cjconfecciones.back.reports;

public class DetalleReporte {
    private String unidades;
    private String descripcion;
    private String valorUnitarioFinal;
    private String subValorFactura;
    private String id;

    public DetalleReporte(String unidades, String descripcion, String valorUnitarioFinal, String subValorFactura, String id) {
        this.unidades = unidades;
        this.descripcion = descripcion;
        this.valorUnitarioFinal = valorUnitarioFinal;
        this.subValorFactura = subValorFactura;
        this.id = id;
    }

    public String getUnidades() { return unidades; }
    public void setUnidades(String unidades) { this.unidades = unidades; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getValorUnitarioFinal() { return valorUnitarioFinal; }
    public void setValorUnitarioFinal(String valorUnitarioFinal) { this.valorUnitarioFinal = valorUnitarioFinal; }
    public String getSubValorFactura() { return subValorFactura; }
    public void setSubValorFactura(String subValorFactura) { this.subValorFactura = subValorFactura; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}