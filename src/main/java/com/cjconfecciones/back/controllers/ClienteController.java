package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Persona;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class ClienteController implements Serializable {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;
    //private EntityManagerFactory emf = Persistence.createEntityManagerFactory("unitPersistence");

    Logger log = Logger.getLogger(ClienteController.class.getName());

    public void newClient (){
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        log.info("NUEVA PERSONA");
        Persona p = new Persona();
        p.setCedula("0104809470");
        em.persist(p);
        transaction.commit();
        log.info("INSERTO CLIENTE");
    }


    public JsonObject searchClient4Name(JsonObject requestObject){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try{
            String name = requestObject.getString("name");
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select cedula, nombre , telefono, direccion  from cjconfecciones.tpersona t where upper(nombre) like  upper(:name)";
            Query query = entityManager.createNativeQuery(sqlQuery);
            query.setParameter("name",name+"%");
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for(Object[] object : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("cedula", String.valueOf(object[0]));
                obj.add("nombre", String.valueOf(object[1]));
                obj.add("telefono",String.valueOf(object[2]));
                obj.add("direccion", String.valueOf(object[3]));
                arrayBuilder.add(obj);
            }
            jsonObjectBuilder.add("error", 0);
            jsonObjectBuilder.add("personas", arrayBuilder);
        }catch (Exception e){
            jsonObjectBuilder.add("error", 1);
            log.log(Level.SEVERE, "ERROR WHEN GET PERSONAS ",e);
        }
        return jsonObjectBuilder.build();
    }




}
