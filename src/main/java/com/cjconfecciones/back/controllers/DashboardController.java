package com.cjconfecciones.back.controllers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class DashboardController {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;

    Logger log = Logger.getLogger(DashboardController.class.getName());

    public JsonObject getDataDashboard(){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try{
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select count(id) " +
                    "from cjconfecciones.tpedidocabecera t " +
                    "where freal >= DATE_TRUNC('month', CURRENT_DATE) " +
                    "and freal < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'";

            Query query = entityManager.createNativeQuery(sqlQuery);
            List<Object[]> resultados = query.getResultList();
            jsonObjectBuilder.add("nroPedidos", new BigDecimal(String.valueOf(resultados.get(0))));

            sqlQuery = "select sum(total)" +
                    "from cjconfecciones.tpedidocabecera t " +
                    "where freal >= DATE_TRUNC('month', CURRENT_DATE) " +
                    "and freal < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'";

             query = entityManager.createNativeQuery(sqlQuery);
             resultados = query.getResultList();
             BigDecimal total = new BigDecimal(String.valueOf(resultados.get(0)));
             jsonObjectBuilder.add("total", total);

            sqlQuery = "select sum (valor)" +
                    "from cjconfecciones.tabono t " +
                    "where fecha >= DATE_TRUNC('month', CURRENT_DATE) " +
                    "and fecha < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' ";

            query = entityManager.createNativeQuery(sqlQuery);
            resultados = query.getResultList();
            BigDecimal cobrado =new BigDecimal(String.valueOf(resultados.get(0)));
            jsonObjectBuilder.add("cobrado", cobrado);
            jsonObjectBuilder.add("porCobrar" ,total.subtract(cobrado));
            jsonObjectBuilder.add("error", 0);
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR DASHBOAD" ,e);
        }
        return  jsonObjectBuilder.build();
    }

}
