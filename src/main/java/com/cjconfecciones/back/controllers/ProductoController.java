package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Producto;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.Query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class ProductoController {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;

    Logger log = Logger.getLogger(ProductoController.class.getName());

    public JsonObject getLstProductos(){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try {
            log.info("SE PROCEDE A OBTENER TODOS LOS PRODUCTOS");
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select id, codigosri,descripcion,valor,tipoproducto from cjconfecciones.tproducto t ";
            Query query
        }catch (Exception e){
            log.log(Level.SEVERE,"ERROR TO GET PRODUCTOS ",e);
        }
        return jsonObjectBuilder.build();
    }
}
