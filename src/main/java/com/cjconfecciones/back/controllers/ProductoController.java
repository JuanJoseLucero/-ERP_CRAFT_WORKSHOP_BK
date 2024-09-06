package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Producto;
import com.cjconfecciones.back.util.EnumCJ;
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

import java.math.BigDecimal;
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
        JsonArrayBuilder lstProducts = Json.createArrayBuilder();
        try {
            log.info("SE PROCEDE A OBTENER TODOS LOS PRODUCTOS");
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select id, codigosri,descripcion,valor,tipoproducto from cjconfecciones.tproducto t ";
            Query query = entityManager.createNativeQuery(sqlQuery);
            List<Object[]> resultados = query.getResultList();
            for (Object[] item : resultados){
                JsonObjectBuilder producto = Json.createObjectBuilder();
                producto.add("id", String.valueOf(item[0]));
                producto.add("codigosri",String.valueOf(item[1]));
                producto.add("descripcion",String.valueOf(item[2]));
                producto.add("valor",new BigDecimal(String.valueOf(item[3])));
                producto.add("tipoproducto", String.valueOf(item[4]));
                lstProducts.add(producto);
            }
            jsonObjectBuilder.add("error", EnumCJ.ESTADO_OK.getEstado());
            jsonObjectBuilder.add("productos",lstProducts);
        }catch (Exception e){
            log.log(Level.SEVERE,"ERROR TO GET PRODUCTOS ",e);
        }
        return jsonObjectBuilder.build();
    }
}
