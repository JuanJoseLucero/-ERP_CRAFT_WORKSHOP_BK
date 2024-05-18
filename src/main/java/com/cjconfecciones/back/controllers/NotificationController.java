package com.cjconfecciones.back.controllers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.Query;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

@Named
@RequestScoped
public class NotificationController implements Serializable {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;

    private String data = "";

    Logger log = Logger.getLogger(NotificationController.class.getName());

    public JsonObject sendRemember(){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try{
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select c.id , p.nombre, c.fecha " +
                    "from tpedidocabecera c, " +
                    " tcliente cli, " +
                    " tpersona p " +
                    "where " +
                    "cli.idpersona  = p.cedula " +
                    "and cli.id = c.ccliente " +
                    "and c.fecha between  current_date and current_date  + interval  '2 days'";

            Query query = entityManager.createNativeQuery(sqlQuery);
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            String mensaje = "";
            for(Object[] object : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id", Integer.parseInt(String.valueOf(object[0])));
                obj.add("nombre", String.valueOf(object[1]));
                obj.add("fecha",String.valueOf(object[2]));
                arrayBuilder.add(obj);
                mensaje = mensaje.concat("Código:"+Integer.parseInt(String.valueOf(object[0])) +" \\n" +
                        " Cliente: "+String.valueOf(object[1]) + "\\n" +
                        "entrega: "+String.valueOf(object[2]) +"\\n \\n \\n");
            }
            jsonObjectBuilder.add("error", 0);
            jsonObjectBuilder.add("peticiones", arrayBuilder);
            this.enviarMensajeNotificacion("dLZYNAuyS9qZlYiP8cDl2N:APA91bHTm3fswXMo8GC3HIXX4eABgCky1h1TtA2COtKyWXpzMOmHriY7HtzvZRXT8Rz8P-e9kbgVeiW00LVjlValcRsyVWZ0YbLZ2nD_PoSxeMgcYXkWu8KRvSiKAFFcadWg77d4GiZp",
                    "PEDIDOS PENDIENTES","Tienes pedidos pendientes","L","TRABAJOS PENDIENTES",mensaje);
            this.enviarMensajeNotificacion("fHpkpoIpQrGDKuQJxiR65F:APA91bH7SBrB-kZZTQ8TixRt5Uj0Wvb2aXqAiCvSkufmCmG5j4UzPy3NmR0TpbyeCx1wBDYKQ1ROxCZovYQWpycWQBpxGkViCTwG87zRw-7aL9uLUpmSl8lV5V3iIBd3j5td2Eri1Vxd",
                    "PEDIDOS PENDIENTES","Tienes pedidos pendientes","L","TRABAJOS PENDIENTES",mensaje);
            this.enviarMensajeNotificacion("eGzAZtxfR5qk60gPRwyYAM:APA91bFAsHtm7rzWxsZ664iH8XjmRojX2x7Q_j4H1BJlPb7g75GHOlkFPixlyiwcLSmzKgwXrIT-UPXQGQFI9UgPyCylvqeeX92KLyACUERBs4YHOYqJb4tFZzaRcB3QK60Rflqqwnoh",
                    "PEDIDOS PENDIENTES","Tienes pedidos pendientes","L","TRABAJOS PENDIENTES",mensaje);
        }catch (Exception e){
            jsonObjectBuilder.add("error", 1);
            log.log(Level.SEVERE, "ERROR WHEN getLstAbonos ",e);
        }
        return jsonObjectBuilder.build();
    }


    public void enviarMensajeNotificacion(String tokenInstalacion, String titulo, String mensaje, String tipo, String tituloLectura, String cuerpoLectura)
    {
        HttpsURLConnection urlConnection = null;
        HttpURLConnection http = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedWriter writer = null;
        try {
            //String messageId = PushUtil.getUniqueMessageId();
            String datapush = "{" +
                    "    \"to\": \"" +tokenInstalacion+"\"," +
                    "    \"notification\":{" +
                    "        \"body\":\"" + mensaje + "\"," +
                    "        \"title\":\"" + titulo+ "\"," +
                    "        \"subtitle\":\"subtitulo\"" +
                    "    },"+
                    "	 \"data\": {\n" +
                    "    \"parametro1\": \""+tipo+"\", " +
                    "    \"parametro3\": \"" +tituloLectura+"\"," +
                    "    \"parametro2\": \""+cuerpoLectura+"\" " +
                    "    }" +
                    "}";
            log.info("push enviado ".concat(datapush));
            if (datapush.isEmpty()) {
                return;
            }

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            // Abro la conexion
            if (url.getProtocol().toLowerCase().equals("https")) {
                // Agrego el contexto
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                http = urlConnection;
            } else {
                http = (HttpURLConnection) url.openConnection();
            }
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setUseCaches(false);
            http.setChunkedStreamingMode(1024);
            http.setConnectTimeout(30000);
            http.setReadTimeout(30000);
            http.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            http.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.addRequestProperty("Accept", "application/json");
            http.setRequestProperty("Authorization", "key=AAAAEnv4els:APA91bEDExvcAq8b88OMz5-3zdq6M06eL0wMMLTDeI_yJHCNt1anKy6fcoGJ-oOWwqe77PNO5QlXglHWKeSf-3OSDP_NvgE-yT9pk9x5o3DRtgCPD2rgBnf7MhOjEjqYq4Hbxroq2mX3");
            http.setRequestMethod("POST");
            http.connect();
            outputStream = http.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(datapush);
            writer.close();
            outputStream.flush();
            outputStream.close();
            // Response
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = getDataResponse(http);
                data = IOUtils.toString(inputStream, "UTF-8");
                inputStream.close();
            } else {
                data = "";
            }
            if (!data.equals("")) {
                log.info("-> RESPUESTA PUSH:  " + data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (http != null)
                    http.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if(inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(writer!=null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private InputStream getDataResponse(HttpURLConnection http) throws Exception {
        InputStream inputStream;
        String header = http.getContentEncoding();
        if (header != null && header.equalsIgnoreCase("gzip")) {
            inputStream = new GZIPInputStream(http.getInputStream());
        } else {
            inputStream = http.getInputStream();
        }
        return inputStream;
    }


}
