package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.*;
import com.cjconfecciones.back.integrations.AsyncEmailSender;
import com.cjconfecciones.back.integrations.IntegracionTercero;
import com.cjconfecciones.back.util.ClientEndPoint;
import com.cjconfecciones.back.util.EnumCJ;
import com.cjconfecciones.back.util.Propiedades;
import com.cjconfecciones.back.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.*;
import jakarta.persistence.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class OrderController {

    @PersistenceUnit(name = "unitPersistence")
    private EntityManagerFactory emf;
    @Inject
    private Propiedades propiedades;
    @Inject
    private ClientEndPoint apiRestClient;
    @Inject
    private ProductoController productoController;
    @Inject
    private Util util;

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
            String sqlDetailOrder = "select   " +
                    "                     d.id as codigocabecera, " +
                    "                     d.unidades ,  " +
                    "                     p.descripcion ,  " +
                    "                     d.total as subtotal,  " +
                    "                     d.fecha,  " +
                    "                     p.valorunitario,  " +
                    "                     p.tipoproducto, " +
                    "                     COALESCE (p.puntadas,'0') as puntadas, " +
                    "                     COALESCE (p.valorpuntada,'0') as valorpuntada, " +
                    "                     COALESCE (p.valordiseniocalculado,'0') as valordiseniocalculado, " +
                    "                     COALESCE (p.valordiseniofinal,'0') as valordiseniofinal,  " +
                    "                     p.id  " +
                    "                    from  " +
                    "                     cjconfecciones.tpedidodetalle as d,  " +
                    "                     cjconfecciones.tproducto as p  " +
                    "                    where  " +
                    "                     d.productoid = p.id   " +
                    "                     and d.ccabecera = :ccabecera ";
            Query query = em.createNativeQuery(sqlDetailOrder);
            query.setParameter("ccabecera",id);
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for(Object[] object : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id", Integer.parseInt(String.valueOf(object[0])));
                obj.add("unidades", Integer.parseInt(String.valueOf(object[1])));
                obj.add("descripcion", String.valueOf(object[2]));
                obj.add("subTotal",  new BigDecimal(String.valueOf(object[3])));
                obj.add("fecha", String.valueOf(object[4]));
                obj.add("valorUnitario", new BigDecimal(String.valueOf(object[5])));
                obj.add("tipo", String.valueOf(object[6]));
                obj.add("puntadas", String.valueOf(object[7]));
                obj.add("valorPuntada", String.valueOf(object[8]));
                obj.add("valorDisenioCalculado", String.valueOf(object[9]));
                obj.add("valorDisenioFinal", String.valueOf(object[10]));
                obj.add("idProducto", new BigDecimal(String.valueOf(object[11])));
                arrayBuilder.add(obj);
            }
            jsonObjectBuilder.add("lstDetailBill", arrayBuilder);
            jsonObjectBuilder.add("lstAbonos",this.getListAbono(id));
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR WHEN GETORDERBYID ",e);
        }
        return jsonObjectBuilder.build();
    }

    public JsonArrayBuilder getListAbono(Integer orderId){
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        try{
            EntityManager em = emf.createEntityManager();
            String sqlQuery = "select id,fecha ,valor ,ccabecera  from cjconfecciones.tabono t where ccabecera = :ccabecera";
            Query query = em.createNativeQuery(sqlQuery);
            query.setParameter("ccabecera",orderId);
            List<Object[]> resultados = query.getResultList();
            for(Object[] object : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id", Integer.parseInt(String.valueOf(object[0])));
                obj.add("fecha", String.valueOf(object[1]));
                obj.add("valor",new BigDecimal(String.valueOf(object[2])));
                arrayBuilder.add(obj);
            }

        }catch (Exception e){
            log.log(Level.SEVERE, "error when get List abono ",e);
        }
        return arrayBuilder;
    }

    public JsonObject getOrder4date(JsonObject requestObject){
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        try{
            String finicial = requestObject.getString("finicial");
            String ffinal = requestObject.getString("ffinal");
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery ="select c.id , c.fecha as fechaEntrega, c.total, tp.nombre, tp.direccion, tp.telefono , STRING_AGG(tpro.descripcion ,', ') , c.freal, c.estado      " +
                    "                     from cjconfecciones.tpedidocabecera as c,     " +
                    "                       cjconfecciones.tpedidodetalle as d,     " +
                    "                       cjconfecciones.tcliente as cli,     " +
                    "                       cjconfecciones.tpersona as tp,     " +
                    "                       cjconfecciones.tproducto as tpro "+
                    "                     where  c.id = d.ccabecera     " +
                    "                     and c.ccliente = cli.id     " +
                    "                     and cli.idpersona = tp.cedula     " +
                    "                     and d.productoid = tpro.id " +
                    "                     and c.estado not in ('E') " +
                    "                     and c.freal between to_date(:finicial,'dd-MM-yyyy') and to_date(:ffinal,'dd-MM-yyyy') " +
                    "                     group by c.id , c.fecha, c.total, tp.nombre, tp.direccion,tp.telefono     " +
                    "                      order by c.id desc ";
            Query query = entityManager.createNativeQuery(sqlQuery);
            query.setParameter("finicial",finicial);
            query.setParameter("ffinal",ffinal);
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
                obj.add("estado", String.valueOf(resultado[8]));
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


    public JsonObject getOrders(){
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        try{
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select c.id , c.fecha as fechaEntrega, c.total, tp.nombre, tp.direccion, tp.telefono , STRING_AGG(d.descripcion ,', ') , c.freal, c.estado  " +
                    "from cjconfecciones.tpedidocabecera as c, " +
                    "  cjconfecciones.tpedidodetalle as d, " +
                    "  cjconfecciones.tcliente as cli, " +
                    "  cjconfecciones.tpersona as tp " +
                    "where  c.id = d.ccabecera " +
                    "and  c.ccliente = cli.id " +
                    "and  cli.idpersona = tp.cedula " +
                    " and c.estado not in ('E') "+
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
                obj.add("estado", String.valueOf(resultado[8]));
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
        Gson gson = new Gson();
        log.info("ENTRADA ".concat(gson.toJson(requestObject)));
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
                persona.setEmail(requestObject.getString("email"));
                em.persist(persona);
            }if (personaSearch.getEmail()==null || !personaSearch.getEmail().trim().isEmpty()){
                personaSearch.setEmail(requestObject.getString("email"));
                em.merge(personaSearch);
                log.info("MAIL ACTUALIZADO CORRECTAMENTE");
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
            String detalleConsolidado = "";
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
                        pedidoDetalle.setTotal(detalle.getJsonNumber("subTotal").bigDecimalValue());
                        pedidoDetalle.setCcabecera(pedidoCabecera.getId());
                        em.merge(pedidoDetalle);

                        Producto producto = em.find(Producto.class,detalle.getInt("idProducto"));
                        if(producto != null){
                            log.info("Producto encontrado");
                            producto.setDescripcion(detalle.getString("descripcion"));
                            producto.setValorunitario(detalle.getJsonNumber("valorUnitario").bigDecimalValue());
                            em.merge(producto);
                        }
                    }
                }else{
                    log.info("NUEVO PEDIDO DETALLE");
                    JsonObject createProduct = createNewProduct(detalle);
                    pedidoDetalle = new PedidoDetalle();
                    pedidoDetalle.setFecha(new Date());
                    pedidoDetalle.setUnidades(detalle.getJsonNumber("unidades").bigDecimalValue());
                    pedidoDetalle.setTotal(detalle.getJsonNumber("subTotal")!=null?detalle.getJsonNumber("subTotal").bigDecimalValue():BigDecimal.ZERO);
                    pedidoDetalle.setCcabecera(pedidoCabecera.getId());
                    pedidoDetalle.setProductoid(createProduct.getJsonNumber("codeId").intValue());
                    em.persist(pedidoDetalle);
                }
            }
            log.info("REGISTRO GUARDADO CORRECTAMENTE");
            t.commit();
            /** Envio de notificacion */
            /**
            HashMap<String,Object> map = new HashMap<>();
            map.put("celular",propiedades.getParametrosProperties("notificationNumber"));
            map.put("orderId",String.valueOf(pedidoCabecera.getId()).concat("-").concat(personaSearch.getNombre()!=null?personaSearch.getNombre():persona.getNombre()));
            map.put("status","NUEVA");
            //JsonObject jsonObjectResponse = apiRestClient.consumirServicosWebWS(JsonObject.class, propiedades,map,"1");
            */

            /** Envio de notificacion cliente*/
            /**
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            HashMap<String,Object> mapCliente = new HashMap<>();
            String celularCliente = "593".concat(personaSearch.getTelefono()!=null?personaSearch.getTelefono():persona.getTelefono());
            log.info("CELULAR CLIENTE ".concat(celularCliente));
            map.put("celular",celularCliente);
            map.put("date",  formatter.format(pedidoCabecera.getFecha()));
            map.put("detalle",detalleConsolidado);
            //jsonObjectResponse = apiRestClient.consumirServicosWebWS(JsonObject.class, propiedades,map,"2");
            */

            /**
             * Logica de envio de correos
             */

            if(personaSearch.getEmail()!=null && !personaSearch.getEmail().isEmpty()){
                enviarMail(personaSearch,pedidoCabecera);
            }
            response = Json.createObjectBuilder().add("error","0");
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR WHEN STORING THE NEW ORDER",e);
            response = Json.createObjectBuilder().add("error","1");
            t.rollback();
        }
        return  response.build();
    }

    private void enviarMail(Persona persona, PedidoCabecera pedidoCabecera){
        try{
            Map<String, String> headers = Map.of(
                    "accept", "application/json",
                    "api-key","",
                    "content-type", "application/json"
            );

            IntegracionTercero servicio = IntegracionFactory.crear("brevo",
                    "https://api.brevo.com/v3/smtp/email",
                    headers);

            servicio.connectar();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode payload = mapper.createObjectNode();

            //Sender
            ObjectNode sender = mapper.createObjectNode();
            sender.put("name","CJCONFECCIONES");
            sender.put("email","cjconfecciones@percha.online");
            payload.put("sender",sender);

            //Destinatario
            ArrayNode toArray = mapper.createArrayNode();
            ObjectNode to = mapper.createObjectNode();
            to.put("email",persona.getEmail());
            to.put("name",persona.getNombre());
            toArray.add(to);
            payload.set("to",toArray);

            payload.put("subject","Pedido Registrado: ".concat(String.valueOf(pedidoCabecera.getId())).concat(" ").concat(persona.getNombre()) );
            payload.put("htmlContent","<html><body><h1>Buenos dias estimad@:"
                    .concat(persona.getNombre())
                    .concat(" se ha registrado un pedido con el codigo: ")
                    .concat(String.valueOf(pedidoCabecera.getId()))
                    .concat("</h1></body></html>"));

            //Async
            AsyncEmailSender asyncEmailSender = new AsyncEmailSender();
            asyncEmailSender.enviarAsync(() ->{
                try{
                    servicio.enviar(payload);
                } catch(Exception e){
                    log.log(Level.SEVERE, "ERROR AL ENVIAR EL MAIL ",e);
                }
            });
            asyncEmailSender.shutdown();
        }catch (Exception e){
            log.log(Level.SEVERE,"ERROR Al ENVIAR EL MAIL ",e);
        }
    }

    private JsonObject createNewProduct(JsonObject detalle) {
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonObject respJsonObject = null;
        try {
            String tipo = detalle.getString("tipo");
            JsonObjectBuilder json = Json.createObjectBuilder();
            json.add("codigosri", util.nextValueProduct(emf.createEntityManager(),
                            "seq_producto_sri",
                            propiedades.getParametrosProperties("numeroCerosProductos")))
                    .add("descripcion", detalle.getString("descripcion"))
                    .add("tipoproducto", detalle.getString("tipo"));
            if(tipo.equals(EnumCJ.TIPO_BORDADO.getEstado())){// --> Nop esta reconociendo el tipo
                json.add("puntadas", detalle.getJsonNumber("puntadas").bigDecimalValue())
                        .add("valorPuntada", detalle.getJsonNumber("valorPuntada").bigDecimalValue())
                        .add("valorDisenioCalculado", detalle.getJsonNumber("valorDisenioCalculado").bigDecimalValue())
                        .add("valorDisenioFinal", detalle.getJsonNumber("valorDisenioFinal").bigDecimalValue());
            }else{
                json.add("valorunitario", detalle.getJsonNumber("valorUnitario").bigDecimalValue());
            }
            respJsonObject = this.productoController.persistProduct(json.build());
            return respJsonObject;
        } catch (Exception e) {
            response = Json.createObjectBuilder();
            response.add("error", EnumCJ.ESTADO_ERROR.getEstado());
            log.log(Level.SEVERE, "ERROR TO CREATE NEW PRODUCT ", e);
            return response.build();
        }
    }

    public JsonObject changeStatus(JsonObject requestObject){
        JsonObjectBuilder response = null;
        EntityManager em = emf.createEntityManager();
        EntityTransaction t = em.getTransaction();
        try{
            t.begin();
            String auxId = requestObject.getString("pedidoId");
            Integer id = Integer.parseInt(auxId);
            PedidoCabecera pedidoCabecera = em.find(PedidoCabecera.class,id);
            pedidoCabecera.setEstado(EnumCJ.ESTADO_ELIMINADO.getEstado());
            em.merge(pedidoCabecera);
            response = Json.createObjectBuilder().add("error","0");
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR WHEN STORING THE NEW ORDER",e);
            response = Json.createObjectBuilder().add("error","1");
        }finally {
            t.commit();
        }
        return  response.build();
    }
}
