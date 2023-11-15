package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Persona;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class OrderController {

    @PersistenceUnit(name = "unitPersistence")
    private EntityManagerFactory emf;

    Logger log = Logger.getLogger(OrderController.class.getName());
    public JsonObject newOrder(JsonObject requestObject){
        JsonObjectBuilder response = null;
        Persona persona = new Persona();
        EntityManager em = emf.createEntityManager();
        EntityTransaction t = em.getTransaction();
        try{
            log.info("INIT METHOD NEW ORDER");
            log.info("STORING PERSON");
            t.begin();
            persona.setCedula(requestObject.getJsonObject("persona").getString("cedula"));
            persona.setNombre(requestObject.getJsonObject("persona").getString("nombre"));
            persona.setApellido(requestObject.getJsonObject("persona").getString("apellido"));
            persona.setTelefono(requestObject.getJsonObject("persona").getString("telefono"));
            persona.setDireccion(requestObject.getJsonObject("persona").getString("direccion"));
            em.persist(persona);
            t.commit();
            response = Json.createObjectBuilder().add("error","1");
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR WHEN STORING THE NEW ORDER");
            t.rollback();
        }
        return  response.build();
    }
}
