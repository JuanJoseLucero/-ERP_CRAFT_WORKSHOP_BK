package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Producto;
import com.cjconfecciones.back.util.EnumCJ;
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
public class ProductoController {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;

    Logger log = Logger.getLogger(ProductoController.class.getName());

    public JsonObject search4Description4Client(JsonObject data){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try {
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery ="select  tp.descripcion , tp.valorunitario  " +
                    "from  tpersona p, " +
                    "  tcliente c, " +
                    "  tpedidocabecera pc, " +
                    "  tpedidodetalle pd, " +
                    "  tproducto tp " +
                    "  where p.cedula = c.idpersona  " +
                    "  and c.id =pc.ccliente " +
                    "  and pd.ccabecera = pc.id " +
                    "  and tp.id = pd.productoid  " +
                    "  and pc.estado <> 'E' " +
                    "  and p.cedula = :cedula " +
                    "  and tp.descripcion like :description ";
            Query query = entityManager.createNativeQuery(sqlQuery);
            query.setParameter("cedula",data.getString("cedula"));
            query.setParameter("description","%".concat(data.getString("descripcion")).concat("%"));
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayJson = Json.createArrayBuilder();
            for(Object[] celdas : resultados){
                JsonObjectBuilder item = Json.createObjectBuilder();
                item.add("descripcion", String.valueOf(celdas[0]))
                        .add("valorUnitario",String.valueOf(celdas[1]));
                arrayJson.add(item);
            }
            jsonObjectBuilder.add("error" ,EnumCJ.ESTADO_OK.getEstado())
                    .add("products",arrayJson);
        }catch (Exception e ){
            log.log(Level.SEVERE, "ERROR TO SEARCH DESCRIPTION ",e);
            jsonObjectBuilder.add("error",EnumCJ.ESTADO_ERROR.getEstado());
        }
        return jsonObjectBuilder.build();
    }

    public JsonObject persistProduct(JsonObject data){
        JsonObjectBuilder response = Json.createObjectBuilder();
        try{
            Producto producto = Producto.builder()
                    .codigosri(data.getString("codigosri"))
                    .descripcion(data.getString("descripcion"))
                    .subtotal(data.getJsonNumber("valor").bigDecimalValue())
                    .tipoproducto(data.getString("tipoproducto"))
                    .valorunitario( data.getJsonNumber("valorunitario").bigDecimalValue())
                    .numeroprendas(data.getJsonNumber("unidades").bigDecimalValue())
                    .tipoproducto(data.getString("tipoproducto"))
                    .tipoproductosri("B")
                    .build();
            EntityManager em = emf.createEntityManager();
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            em.persist(producto);
            transaction.commit();
            response = Json.createObjectBuilder();
            response.add("error", EnumCJ.ESTADO_OK.getEstado());
            response.add("codeId" ,producto.getId());
        }catch (Exception e){
            response = Json.createObjectBuilder();
            response.add("error", EnumCJ.ESTADO_ERROR.getEstado());
            log.log(Level.SEVERE, "ERROR TO PERSIST PRODUCTS ",e);
        }
        return response.build();
    }

    public JsonObject update4Id(JsonObject data){
        JsonObjectBuilder response = Json.createObjectBuilder();
        try{
            Producto producto = Producto.builder()
                    .id(data.getInt("id"))
                    .codigosri(data.getString("codigosri"))
                    .descripcion(data.getString("descripcion"))
                    .subtotal(data.getJsonNumber("valor").bigDecimalValue())
                    .tipoproducto(data.getString("tipoproducto"))
                    .build();
            EntityManager entityManager = emf.createEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.merge(producto);
            transaction.commit();
            response.add("error", EnumCJ.ESTADO_OK.getEstado());
            return response.build();
        }catch (Exception e){
            response = Json.createObjectBuilder();
            response.add("error", EnumCJ.ESTADO_ERROR.getEstado());
            log.log(Level.SEVERE, "ERROR TO PERSIST PRODUCTS ",e);
            return response.build();
        }
    }

    public JsonObject getProductById(JsonObject data){
        JsonObjectBuilder response = Json.createObjectBuilder();
        try{
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery ="select id, codigosri, descripcion, valor, tipoproducto from cjconfecciones.tproducto where id = :id";
            Query query = entityManager.createNativeQuery(sqlQuery);
            query.setParameter("id",data.getInt("id"));
            List<Object[]> resultados = query.getResultList();
            response.add("id", String.valueOf(resultados.get(0)[0]));
            response.add("codigosri", String.valueOf(resultados.get(0)[1]));
            response.add("descripcion", String.valueOf(resultados.get(0)[2]));
            response.add("valor", new BigDecimal(String.valueOf(resultados.get(0)[3])));
            response.add("tipoproducto", String.valueOf(resultados.get(0)[4]));
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR GET TO PRODUCT BY ID",e);
            response = Json.createObjectBuilder();
            response.add("error", EnumCJ.ESTADO_ERROR.getEstado());
        }
        return response.build();
    }


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
            jsonObjectBuilder.add("error", EnumCJ.ESTADO_ERROR.getEstado());
            log.log(Level.SEVERE,"ERROR TO GET PRODUCTOS ",e);
        }
        return jsonObjectBuilder.build();
    }
}
