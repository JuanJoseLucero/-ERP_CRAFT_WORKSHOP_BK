package com.cjconfecciones.back.util;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class ClientEndPoint {

    private Logger log = Logger.getLogger(ClientEndPoint.class.getName());

    public <T> T consumirServicosWeb(Class<T> classResponse){
        String url = "http://localhost:8080/back/rest/order/new";
        String json = "{\"nombre\": \"Ejemplo\", \"edad\": 30}";
        T response = null;

        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json));
            HttpResponse responseHttp = httpClient.execute(httpPost);
            if (responseHttp.getCode() == 200){
                log.info("CONSUME OK");
                HttpEntity entity = ((CloseableHttpResponse) responseHttp).getEntity();
                String jsonResponse = entity != null ? EntityUtils.toString(entity):"";
                Type tipoRespuesta = TypeToken.getParameterized(classResponse).getType();
                response = new Gson().fromJson(jsonResponse, tipoRespuesta);
            }
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR WHEN CONSUME WS ",e);
        }
        return response;
    }

    /**
     *     public static <T> T consumirServicioWeb(Class<T> claseRespuesta) {
     *         String url = "http://localhost:8080/back/rest/order/new";
     *         String jsonBody = "{\"nombre\": \"Ejemplo\", \"edad\": 30}";
     *         T respuesta = null;
     *
     *         try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
     *             HttpPost httpPost = new HttpPost(url);
     *             httpPost.setHeader("Content-Type", "application/json");
     *             httpPost.setEntity(new StringEntity(jsonBody));
     *
     *             HttpResponse response = httpClient.execute(httpPost);
     *
     *             if (response.getStatusLine().getStatusCode() == 200) {
     *                 HttpEntity entity = response.getEntity();
     *                 String jsonResponse = entity != null ? EntityUtils.toString(entity) : "";
     *
     *                 // Crear un tipo de dato de respuesta genérica
     *                 Type tipoRespuesta = TypeToken.getParameterized(claseRespuesta).getType();
     *
     *                 // Convertir la respuesta JSON al tipo de dato de la clase genérica
     *                 respuesta = new Gson().fromJson(jsonResponse, tipoRespuesta);
     *             } else {
     *                 System.out.println("Hubo un problema al realizar la solicitud. Código de estado: " + response.getStatusLine().getStatusCode());
     *             }
     *         } catch (Exception e) {
     *             e.printStackTrace();
     *         }
     *
     *         return respuesta;
     *     }
     */
}
