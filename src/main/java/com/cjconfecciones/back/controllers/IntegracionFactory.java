package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.integrations.BrevoIntegracion;
import com.cjconfecciones.back.integrations.IntegracionTercero;

import java.util.Map;

public class IntegracionFactory {

    public static IntegracionTercero<?> crear(String tipo, String apiUrl, Map<String, String> headers){
        switch (tipo.toLowerCase()){
            case "brevo":
                return new BrevoIntegracion(apiUrl, headers);
            default:
                throw new IllegalArgumentException("Proveedor no soportado");
        }
    }
}
