package com.cjconfecciones.back.integrations;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.logging.Logger;

public class AbstractIntegracionTercero <T> implements IntegracionTercero{

    protected String apiUrl;
    protected Map<String,String> headres;

    public AbstractIntegracionTercero(String apiUrl, Map<String, String> headers ){
        this.apiUrl = apiUrl;
        this.headres = headers;
    }

    protected HttpURLConnection crearConexion(String metodo, String urlStr) throws Exception{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(metodo);
        conn.setDoInput(true);
        headres.forEach(conn::setRequestProperty);

        return conn;
    }

    @Override
    public void connectar() throws Exception {
        System.out.println("Conectado a "+ apiUrl);
    }

    @Override
    public void enviar(Object payload) throws Exception {

    }

    @Override
    public Object recuperar(String id) throws Exception {
        return null;
    }
}
