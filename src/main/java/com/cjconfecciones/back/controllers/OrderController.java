package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Cliente;
import com.cjconfecciones.back.entities.PedidoCabecera;
import com.cjconfecciones.back.entities.PedidoDetalle;
import com.cjconfecciones.back.entities.Persona;
import com.cjconfecciones.back.util.EnumCJ;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.*;
import jakarta.persistence.*;

import javax.swing.*;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class OrderController {

    @PersistenceUnit(name = "unitPersistence")
    private EntityManagerFactory emf;
    Logger log = Logger.getLogger(OrderController.class.getName());

    public JsonObject getOrderById(JsonObject requestObject){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try{
            String auxId = requestObject.getString("pedidoId");
            Integer id = Integer.parseInt(auxId);
            EntityManager em = emf.createEntityManager();

            /** Get order 4 id***/
            PedidoCabecera pedidoCabecera = em.find(PedidoCabecera.class,id);
            //Gson gson = new Gson();
            Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
            String json = gson.toJson(pedidoCabecera);
            JsonReader reader = Json.createReader(new StringReader(json));
            jsonObjectBuilder = Json.createObjectBuilder(reader.readObject());

            /** Get person **/
            Cliente cliente = em.find(Cliente.class,pedidoCabecera.getCcliente());
            log.info("CLIENT FIND BY ID ".concat(cliente.getIdpersona()));

            Persona person = em.find(Persona.class,cliente.getIdpersona());
            log.info("PERSON FIND BY NAME ".concat( person.getNombre()));

            jsonObjectBuilder.add("pedidoId" , id);
            jsonObjectBuilder.add("nombres" , person.getNombre());
            jsonObjectBuilder.add("identificacion" , person.getCedula());
            jsonObjectBuilder.add("direccion" , person.getDireccion());
            jsonObjectBuilder.add("telefono" , person.getTelefono());


            /**Get detail orders **/
            String sqlDetailOrder = "select d.id , d.unidades , d.descripcion , d.vunitario , d.total , d.fecha, d.valorunitariofinal, d.puntadas, d.subvalorfactura    from cjconfecciones.tpedidodetalle as d where d.ccabecera  = :ccabecera";
            Query query = em.createNativeQuery(sqlDetailOrder);
            query.setParameter("ccabecera",id);
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for(Object[] object : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id", Integer.parseInt(String.valueOf(object[0])));
                obj.add("unidades", Integer.parseInt(String.valueOf(object[1])));
                obj.add("descripcion", String.valueOf(object[2]));
                obj.add("valorUnitario", new BigDecimal(String.valueOf(object[3])));
                obj.add("total",  new BigDecimal(String.valueOf(object[4])));
                obj.add("fecha", String.valueOf(object[5]));
                obj.add("valorFinal", String.valueOf(object[6]));
                obj.add("puntadas", String.valueOf(object[7]));
                obj.add("subValorFactura", String.valueOf(object[8]));
                arrayBuilder.add(obj);
            }
            jsonObjectBuilder.add("lstDetailBill", arrayBuilder);
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR WHEN GETORDERBYID ",e);
        }
        return jsonObjectBuilder.build();
    }

    public JsonObject getOrders(){
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        try{
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select c.id , c.fecha as fechaEntrega, c.total, tp.nombre, tp.direccion, tp.telefono , STRING_AGG(d.descripcion ,', ') , c.freal " +
                    "from cjconfecciones.tpedidocabecera as c, " +
                    "  cjconfecciones.tpedidodetalle as d, " +
                    "  cjconfecciones.tcliente as cli, " +
                    "  cjconfecciones.tpersona as tp " +
                    "where  c.id = d.ccabecera " +
                    "and  c.ccliente = cli.id " +
                    "and  cli.idpersona = tp.cedula " +
                    "group by c.id , c.fecha, c.total, tp.nombre, tp.direccion,tp.telefono " +
                    " order by c.id desc ";

            Query query = entityManager.createNativeQuery(sqlQuery);
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            jsonBuilder.add("error", 0);
            for (Object[] resultado : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id", Integer.parseInt(String.valueOf(resultado[0])));
                obj.add("fechaEntrega", String.valueOf(resultado[1]));
                obj.add("total", new BigDecimal(String.valueOf(resultado[2])));
                obj.add("nombre", String.valueOf(resultado[3]));
                obj.add("direccion", String.valueOf(resultado[4]));
                obj.add("telefono", String.valueOf(resultado[5]));
                obj.add("detalle", String.valueOf(resultado[6]));
                obj.add("freal", String.valueOf(resultado[7]));
                arrayBuilder.add(obj);
            }
            jsonBuilder.add("pedidos", arrayBuilder);
            entityManager.close();
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR TO GET ORDERS ",e);
            jsonBuilder.add("error","1");
        }
        return jsonBuilder.build();
    }

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
            String queryClient = "select id, idpersona  from tcliente where idpersona = :idParamtero ";
            Query query = em.createNativeQuery(queryClient);
            query.setParameter("idParamtero",requestObject.getString("identificacion"));
            List<Object[]> lst = query.getResultList();
            if(lst.isEmpty()){
                log.info("EMPTY LIST");
                cliente.setIdpersona(persona.getCedula());
                em.persist(cliente);
                log.info("STORING CLIENTE");
            }else{
                Object[] celdas = lst.get(0);
                cliente.setId(Integer.parseInt(String.valueOf(celdas[0])));
                cliente.setIdpersona(String.valueOf(celdas[1]));
            }

            PedidoCabecera pedidoCabecera = em.find(PedidoCabecera.class, Integer.parseInt(requestObject.containsKey("pedidoId")? requestObject.getString("pedidoId"):"0"));
            if(pedidoCabecera == null){
                pedidoCabecera = new PedidoCabecera();
                pedidoCabecera.setCcliente(cliente.getId());
                pedidoCabecera.setEstado(EnumCJ.ESTADO_ABIERTO.getEstado());
                //pedidoCabecera.setTotal(requestObject.getJsonObject("cabecera").getJsonNumber("total").bigDecimalValue());
                pedidoCabecera.setTotal(requestObject.getJsonNumber("total").bigDecimalValue());
                //String fechaCadena = requestObject.getJsonObject("cabecera").getString("fecha");
                String fechaCadena = requestObject.getString("fecha");
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                pedidoCabecera.setFecha(sdf.parse(fechaCadena));
                pedidoCabecera.setFreal(new Date());
                em.persist(pedidoCabecera);
                log.info("STORING CABECERA");
            }else{
                log.info("pedidoCabecera found");
                pedidoCabecera.setTotal(requestObject.getJsonNumber("total").bigDecimalValue());
                pedidoCabecera.setCcliente(cliente.getId());
                String fechaCadena = requestObject.getString("fecha");
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                pedidoCabecera.setFecha(sdf.parse(fechaCadena));
                em.merge(pedidoCabecera);
            }

            //JsonObject cabecera = requestObject.getJsonObject("detalle")
            JsonArray detallesJson = requestObject.getJsonArray("lstDetailBill");
            for (int i = 0; i< detallesJson.size(); i++){
                JsonObject detalle  =detallesJson.getJsonObject(i);
                PedidoDetalle pedidoDetalle = new PedidoDetalle();
                if (detalle.containsKey("id")){
                    log.info("ID FOUND");
                    pedidoDetalle = em.find(PedidoDetalle.class, detalle.getInt("id"));
                    if(pedidoDetalle != null) {
                        log.info("Modify Detail");
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                        pedidoDetalle.setFecha(sdf.parse(detalle.getString("fechaCadena")));
                        pedidoDetalle.setUnidades(detalle.getJsonNumber("unidades").bigDecimalValue());
                        pedidoDetalle.setDescripcion(detalle.getString("descripcion"));
                        pedidoDetalle.setVunitario(detalle.getJsonNumber("valorUnitario").bigDecimalValue());
                        pedidoDetalle.setValorunitariofinal(detalle.getJsonNumber("valorFinal").bigDecimalValue());
                        pedidoDetalle.setTotal(detalle.getJsonNumber("total").bigDecimalValue());
                        pedidoDetalle.setPuntadas(detalle.getJsonNumber("puntadas").bigDecimalValue());
                        pedidoDetalle.setCcabecera(pedidoCabecera.getId());
                        pedidoDetalle.setSubvalorfactura(detalle.getJsonNumber("subValorFactura").bigDecimalValue());
                        em.merge(pedidoDetalle);
                    }
                }else{
                    log.info("PEDIDO DETALLE FOUND");
                    pedidoDetalle = new PedidoDetalle();
                    pedidoDetalle.setFecha(new Date());
                    pedidoDetalle.setUnidades(detalle.getJsonNumber("unidades").bigDecimalValue());
                    pedidoDetalle.setDescripcion(detalle.getString("descripcion"));
                    pedidoDetalle.setVunitario(detalle.getJsonNumber("valorUnitario").bigDecimalValue());
                    pedidoDetalle.setValorunitariofinal(detalle.getJsonNumber("valorFinal").bigDecimalValue());
                    pedidoDetalle.setTotal(detalle.getJsonNumber("total").bigDecimalValue());
                    pedidoDetalle.setPuntadas(detalle.getJsonNumber("puntadas").bigDecimalValue());
                    pedidoDetalle.setCcabecera(pedidoCabecera.getId());
                    pedidoDetalle.setSubvalorfactura(detalle.getJsonNumber("subValorFactura").bigDecimalValue());
                    em.persist(pedidoDetalle);
                }
            }
            log.info("REGISTRO GUARDADO CORRECTAMENTE");
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
