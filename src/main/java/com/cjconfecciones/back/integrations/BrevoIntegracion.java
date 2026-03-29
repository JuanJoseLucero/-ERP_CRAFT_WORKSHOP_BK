package com.cjconfecciones.back.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class BrevoIntegracion extends AbstractIntegracionTercero<Object>{

    private final ObjectMapper mapper = new ObjectMapper();

    public BrevoIntegracion(String apiUrl, Map<String, String> headers) {
        super(apiUrl, headers);
    }

    @Override
    public void enviar(Object payload) throws Exception {

    }
}
