package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Abono;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class AbonoController {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;

    Logger log = Logger.getLogger(AbonoController.class.getName());

    public JsonObject getLstAbonos(JsonObject requestObject){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try{
            Integer pedidoId = requestObject.getInt("pedidoId");
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select id,fecha ,valor ,ccabecera  from cjconfecciones.tabono t where ccabecera = :ccabecera";
            Query query = entityManager.createNativeQuery(sqlQuery);
            query.setParameter("ccabecera",pedidoId);
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for(Object[] object : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id", Integer.parseInt(String.valueOf(object[0])));
                obj.add("fecha", String.valueOf(object[1]));
                obj.add("valor",new BigDecimal(String.valueOf(object[2])));
                arrayBuilder.add(obj);
            }
            jsonObjectBuilder.add("error", 0);
            jsonObjectBuilder.add("abonos", arrayBuilder);
        }catch (Exception e){
            jsonObjectBuilder.add("error", 1);
            log.log(Level.SEVERE, "ERROR WHEN getLstAbonos ",e);
        }
        return jsonObjectBuilder.build();
    }

    public JsonObject persistAbonos(JsonObject requestObject){
        JsonObjectBuilder response = Json.createObjectBuilder();
        try{
            EntityManager em = emf.createEntityManager();
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            Abono abono = new Abono();
            String fechaCadena = requestObject.getString("fecha");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            abono.setFecha(sdf.parse(fechaCadena));
            abono.setValor(requestObject.getJsonNumber("valor").bigDecimalValue());
            abono.setCcabecera(requestObject.getInt("ccabecera"));
            em.persist(abono);
            transaction.commit();
            response = Json.createObjectBuilder().add("error","0");
            return  response.build();
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR EN PERSIST ABONO ",e);
            response = Json.createObjectBuilder().add("error","1");
            return  response.build();
        }
    }

}
