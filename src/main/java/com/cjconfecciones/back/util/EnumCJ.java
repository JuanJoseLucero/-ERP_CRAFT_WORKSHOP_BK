package com.cjconfecciones.back.util;


public enum EnumCJ {

    ESTADO_ELIMINADO ("E","ESTADO ELIMINADO"),
    ESTADO_PAGADO ("P","ORDEN DE TRABAJO PAGADO"),
    ESTADO_ABONADO ("AB","ORDEN DE TRABAJO ABONADO"),
    ESTADO_ABIERTO ("A","ORDEN DE TRABAJO ABIERTA");


    private String estado;
    private String descripcion;

    EnumCJ(String estado, String descripcion) {
        this.estado = estado;
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
