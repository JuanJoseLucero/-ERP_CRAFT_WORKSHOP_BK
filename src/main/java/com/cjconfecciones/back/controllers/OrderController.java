package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Cliente;
import com.cjconfecciones.back.entities.PedidoCabecera;
import com.cjconfecciones.back.entities.PedidoDetalle;
import com.cjconfecciones.back.entities.Persona;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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

            Cliente cliente = new Cliente();
            cliente.setIdpersona(persona.getCedula());
            em.persist(cliente);
            log.info("STORING CLIENTE");

            PedidoCabecera pedidoCabecera = new PedidoCabecera();
            pedidoCabecera.setCcliente(cliente.getId());
            pedidoCabecera.setEstado(requestObject.getJsonObject("cabecera").getString("estado"));
            pedidoCabecera.setTotal(requestObject.getJsonObject("cabecera").getJsonNumber("total").bigDecimalValue());
            String fechaCadena = requestObject.getJsonObject("cabecera").getString("fecha");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            pedidoCabecera.setFecha(sdf.parse(fechaCadena));
            em.persist(pedidoCabecera);

            //JsonObject cabecera = requestObject.getJsonObject("detalle")
            JsonArray detallesJson = requestObject.getJsonArray("detalles");
            for (int i = 0; i< detallesJson.size(); i++){
                JsonObject detalle  =detallesJson.getJsonObject(i);
                PedidoDetalle pedidoDetalle = new PedidoDetalle();
                pedidoDetalle.setFecha(new Date());
                pedidoDetalle.setUnidades(detalle.getJsonNumber("unidades").bigDecimalValue());
                pedidoDetalle.setDescripcion(detalle.getString("descripcion"));
                pedidoDetalle.setVunitario(detalle.getJsonNumber("vunitario").bigDecimalValue());
                pedidoDetalle.setTotal(detalle.getJsonNumber("total").bigDecimalValue());
                em.persist(pedidoDetalle);
            }
            t.commit();
            response = Json.createObjectBuilder().add("error","0");
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR WHEN STORING THE NEW ORDER",e);
            response = Json.createObjectBuilder().add("error","1");
            t.rollback();
        }
        return  response.build();
    }
}
