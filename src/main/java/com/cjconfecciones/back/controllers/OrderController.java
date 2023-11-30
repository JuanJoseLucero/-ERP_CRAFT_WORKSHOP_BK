package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Cliente;
import com.cjconfecciones.back.entities.PedidoCabecera;
import com.cjconfecciones.back.entities.PedidoDetalle;
import com.cjconfecciones.back.entities.Persona;
import com.cjconfecciones.back.util.EnumCJ;
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

    public JsonObject searchClient(JsonObject requestObject){
        JsonObjectBuilder response = null;
        try{
            log.info("Init search client");
            EntityManager em = emf.createEntityManager();
            String id = requestObject.getString("identificacion");
            Persona persona = em.find(Persona.class,id);
            if (persona !=null){
                response = Json.createObjectBuilder()
                .add("identificacion",persona.getCedula())
                .add("nombres",persona.getNombre())
                .add("direccion", persona.getDireccion())
                .add("telefono",persona.getTelefono());
            }else{
                log.info("CLIENT NOT FOUND");
                response = Json.createObjectBuilder().add("error","1");
            }
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR TO SEARCH CLIENT ",e);
            response = Json.createObjectBuilder().add("error","1");
        }
        return  response.build();
    }


    public JsonObject newOrder(JsonObject requestObject){
        JsonObjectBuilder response = null;
        Persona persona = new Persona();
        EntityManager em = emf.createEntityManager();
        EntityTransaction t = em.getTransaction();
        try{
            log.info("INIT METHOD NEW ORDER");
            log.info("STORING PERSON");
            t.begin();

            Persona personaSearch = em.find(Persona.class,requestObject.getString("identificacion"));
            if (personaSearch ==null){
                persona.setCedula(requestObject.getString("identificacion"));
                persona.setNombre(requestObject.getString("nombres"));
                persona.setTelefono(requestObject.getString("telefono"));
                persona.setDireccion(requestObject.getString("direccion"));
                em.persist(persona);
            }

            Cliente cliente = new Cliente();
            cliente.setIdpersona(persona.getCedula());
            em.persist(cliente);
            log.info("STORING CLIENTE");

            PedidoCabecera pedidoCabecera = new PedidoCabecera();
            pedidoCabecera.setCcliente(cliente.getId());
            pedidoCabecera.setEstado(EnumCJ.ESTADO_ABIERTO.getEstado());
            //pedidoCabecera.setTotal(requestObject.getJsonObject("cabecera").getJsonNumber("total").bigDecimalValue());
            pedidoCabecera.setTotal(requestObject.getJsonNumber("total").bigDecimalValue());
            //String fechaCadena = requestObject.getJsonObject("cabecera").getString("fecha");
            String fechaCadena = requestObject.getString("fecha");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            pedidoCabecera.setFecha(sdf.parse(fechaCadena));
            em.persist(pedidoCabecera);
            log.info("STORING CABECERA");

            //JsonObject cabecera = requestObject.getJsonObject("detalle")
            JsonArray detallesJson = requestObject.getJsonArray("lstDetailBill");
            for (int i = 0; i< detallesJson.size(); i++){
                JsonObject detalle  =detallesJson.getJsonObject(i);
                PedidoDetalle pedidoDetalle = new PedidoDetalle();
                pedidoDetalle.setFecha(new Date());
                pedidoDetalle.setUnidades(detalle.getJsonNumber("unidades").bigDecimalValue());
                pedidoDetalle.setDescripcion(detalle.getString("descripcion"));
                pedidoDetalle.setVunitario(detalle.getJsonNumber("valorUnitario").bigDecimalValue());
                pedidoDetalle.setTotal(detalle.getJsonNumber("total").bigDecimalValue());
                pedidoDetalle.setCcabecera(pedidoCabecera.getId());
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
