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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import com.cjconfecciones.back.reports.AbonoReporte;
import com.cjconfecciones.back.reports.DetalleReporte;
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
            String sqlQuery ="select c.id , c.fecha as fechaEntrega, c.total, tp.nombre, tp.direccion, tp.telefono , STRING_AGG(tpro.descripcion ,', ') , c.freal, c.estado,      " +
                    "                       e.nombre as estadoconfeccion,      " +
                    "                       c.estadoconfeccion as estadoconfeccionId " +
                    "                     from cjconfecciones.tpedidocabecera as c " +
                    "                       LEFT JOIN cjconfecciones.testadopedido e ON c.estadoconfeccion = e.id,     " +
                    "                       cjconfecciones.tpedidodetalle as d,     " +
                    "                       cjconfecciones.tcliente as cli,     " +
                    "                       cjconfecciones.tpersona as tp,     " +
                    "                       cjconfecciones.tproducto as tpro " +
                    "                     where  c.id = d.ccabecera     " +
                    "                     and c.ccliente = cli.id     " +
                    "                     and cli.idpersona = tp.cedula     " +
                    "                     and d.productoid = tpro.id " +
                    "                     and c.estado not in ('E') " +
                    "                     and c.freal between to_date(:finicial,'dd-MM-yyyy') and to_date(:ffinal,'dd-MM-yyyy') " +
                    "                     group by c.id , c.fecha, c.total, tp.nombre, tp.direccion,tp.telefono, c.estadoconfeccion, e.nombre     " +
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
                obj.add("estadoconfeccion", resultado[9] != null ? String.valueOf(resultado[9]) : "");
                obj.add("estadoconfeccionId", resultado[10] != null ? Integer.parseInt(String.valueOf(resultado[10])) : 0);
                arrayBuilder.add(obj);
            }

            Query queryEstados = entityManager.createNativeQuery(
                "SELECT id, nombre FROM cjconfecciones.testadopedido ORDER BY id"
            );
            List<Object[]> lstEstados = queryEstados.getResultList();
            JsonArrayBuilder estadosArray = Json.createArrayBuilder();
            for (Object[] e : lstEstados) {
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id", Integer.parseInt(String.valueOf(e[0])));
                obj.add("nombre", String.valueOf(e[1]));
                estadosArray.add(obj);
            }
            jsonBuilder.add("lstEstados", estadosArray);

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
                .add("telefono",persona.getTelefono())
                .add("email", persona.getEmail() != null ? persona.getEmail() : "");
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


    /**
     * TODO: Enviar el pedido adjunto en el mail
     * @param requestObject
     * @return
     */
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
                pedidoCabecera.setEstadoConfeccion(1);
                em.persist(pedidoCabecera);
                log.info("STORING CABECERA");

                HistorialEstadoPedido historial = new HistorialEstadoPedido();
                historial.setCpedido(pedidoCabecera.getId());
                historial.setCestado(1);
                historial.setFecha(new Date());
                historial.setUsuario(requestObject.containsKey("usuario") ? requestObject.getString("usuario") : "SISTEMA");
                historial.setObservacion("Pedido ingresado");
                historial.setNotificacionMal((short) 0);
                historial.setNotificacionMovil((short) 0);
                em.persist(historial);
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

            String base64Pdf = getComprobante(pedidoCabecera);
            if (base64Pdf != null) {
                ArrayNode attachments = mapper.createArrayNode();
                ObjectNode attachment = mapper.createObjectNode();
                attachment.put("content", base64Pdf);
                SimpleDateFormat sdfArchivo = new SimpleDateFormat("yyyyMMdd");
                String nombreLimpio = persona.getNombre().replaceAll("[\\\\/:*?\"<>|]", "_");
                String fechaStr = sdfArchivo.format(pedidoCabecera.getFecha());
                String totalStr = pedidoCabecera.getTotal().toString();
                String nombreArchivo = nombreLimpio + "_" + fechaStr + "_" + totalStr;
                attachment.put("name", nombreArchivo + ".pdf");
                attachments.add(attachment);
                payload.set("attachment", attachments);
            }

            //Async
            AsyncEmailSender asyncEmailSender = new AsyncEmailSender();
            String finalBase64Pdf = base64Pdf;
            asyncEmailSender.enviarAsync(() ->{
                try{
                    servicio.enviar(payload);
                    if (finalBase64Pdf != null) {
                        log.info("Correo enviado con comprobante adjunto para pedido: " + pedidoCabecera.getId());
                    }
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

    public JsonObject getComprobanteJson(String pedidoId) {
        JsonObjectBuilder response = Json.createObjectBuilder();
        EntityManager em = emf.createEntityManager();
        try {
            Integer id = Integer.parseInt(pedidoId);
            PedidoCabecera pc = em.find(PedidoCabecera.class, id);
            if (pc == null) {
                return response.add("error", "1").add("message", "Pedido no encontrado").build();
            }
            String base64 = getComprobante(pc);
            if (base64 != null) {
                return response.add("error", "0").add("base64", base64).build();
            } else {
                return response.add("error", "1").add("message", "Error al generar reporte").build();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR AL GENERAR REPORTE JSON", e);
            return response.add("error", "1").add("message", e.getMessage()).build();
        } finally {
            em.close();
        }
    }

    public String getComprobante(PedidoCabecera pedidoCabecera) {
        EntityManager em = emf.createEntityManager();
        try {
            PedidoCabecera pc = em.find(PedidoCabecera.class, pedidoCabecera.getId());
            if (pc != null) {
                log.info("=== CABECERA DEL PEDIDO ===");
                log.info("ID: " + pc.getId());
                log.info("ID Cliente: " + pc.getCcliente());
                log.info("Fecha: " + pc.getFecha());
                log.info("Fecha Real: " + pc.getFreal());
                log.info("Total: " + pc.getTotal());
                log.info("Estado: " + pc.getEstado());

                Query queryCliente = em.createNativeQuery(
                        "SELECT id, idpersona FROM cjconfecciones.tcliente WHERE id = :idCliente");
                queryCliente.setParameter("idCliente", pc.getCcliente());
                List<Object[]> clienteData = queryCliente.getResultList();

                Persona persona = null;
                if (!clienteData.isEmpty()) {
                    String idPersona = String.valueOf(clienteData.get(0)[1]);
                    persona = em.find(Persona.class, idPersona);
                    if (persona != null) {
                        log.info("=== DATOS DEL CLIENTE ===");
                        log.info("Cédula: " + persona.getCedula());
                        log.info("Nombre: " + persona.getNombre());
                        log.info("Dirección: " + persona.getDireccion());
                        log.info("Teléfono: " + persona.getTelefono());
                        log.info("Email: " + (persona.getEmail() != null ? persona.getEmail() : "N/A"));
                    }
                }

                Query queryDetalles = em.createNativeQuery(
                        "SELECT d.id, d.unidades, d.total, p.descripcion, p.valorunitario " +
                        "FROM cjconfecciones.tpedidodetalle d " +
                        "JOIN cjconfecciones.tproducto p ON d.productoid = p.id " +
                        "WHERE d.ccabecera = :ccabecera");
                queryDetalles.setParameter("ccabecera", pc.getId());
                List<Object[]> detalles = queryDetalles.getResultList();

                List<DetalleReporte> listaDetalles = new ArrayList<>();
                for (Object[] d : detalles) {
                    BigDecimal unidades = new BigDecimal(String.valueOf(d[1]));
                    String descripcion = String.valueOf(d[3]);
                    BigDecimal valorUnitario = new BigDecimal(String.valueOf(d[4]));
                    BigDecimal total = new BigDecimal(String.valueOf(d[2]));
                    String id = String.valueOf(d[0]);
                    listaDetalles.add(new DetalleReporte(unidades, descripcion, valorUnitario, total, id));
                    log.info("  Detalle: " + descripcion + " - " + unidades + " x " + valorUnitario);
                }

                Query queryAbonos = em.createNativeQuery(
                        "SELECT id, fecha, valor, ccabecera " +
                        "FROM cjconfecciones.tabono WHERE ccabecera = :ccabecera");
                queryAbonos.setParameter("ccabecera", pc.getId());
                List<Object[]> abonos = queryAbonos.getResultList();

                BigDecimal totalAbonos = BigDecimal.ZERO;
                List<AbonoReporte> listaAbonos = new ArrayList<>();
                for (Object[] a : abonos) {
                    String id = String.valueOf(a[0]);
                    String fecha = String.valueOf(a[1]);
                    BigDecimal valor = new BigDecimal(String.valueOf(a[2]));
                    listaAbonos.add(new AbonoReporte(id, fecha, valor));
                    totalAbonos = totalAbonos.add(new BigDecimal(String.valueOf(a[2])));
                    log.info("  Abono: " + id + " - " + fecha + " - " + valor);
                }
                log.info("  Total Abonado: " + totalAbonos);

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("identification", persona != null ? persona.getCedula() : "");
                parametros.put("name", persona != null ? persona.getNombre() : "");
                parametros.put("direction", persona != null ? persona.getDireccion() : "");
                parametros.put("date", pc.getFecha() != null ? sdf.format(pc.getFecha()) : "");
                parametros.put("total", pc.getTotal() != null ? pc.getTotal().toString() : "0");
                parametros.put("abonos", totalAbonos.toString());
                parametros.put("saldo", pc.getTotal().subtract(totalAbonos).toString());

                JRBeanCollectionDataSource dsDetalles = new JRBeanCollectionDataSource(listaDetalles);
                JRBeanCollectionDataSource dsAbonos = new JRBeanCollectionDataSource(listaAbonos);
                parametros.put("ds", dsDetalles);
                parametros.put("dsAbono", dsAbonos);

                InputStream reportStream = getClass().getResourceAsStream("/reports/BillPrintCJ.jasper");
                JasperReport reporte = (JasperReport) JRLoader.loadObject(reportStream);
                JasperPrint print = JasperFillManager.fillReport(reporte, parametros, new JREmptyDataSource());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(print, baos);
                byte[] pdfBytes = baos.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(pdfBytes);

                log.info("=== REPORTE GENERADO EXITOSAMENTE ===");
                return base64;

            } else {
                log.info("Cabecera no encontrada con ID: " + pedidoCabecera.getId());
                return null;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR AL GENERAR REPORTE", e);
            return null;
        } finally {
            em.close();
        }
    }

    public JsonObject notificarCobros() {
        JsonObjectBuilder response = Json.createObjectBuilder();
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT c.id, c.fecha, tp.nombre, c.estado, c.total " +
                         "FROM cjconfecciones.tpedidocabecera c " +
                         "JOIN cjconfecciones.tcliente cli ON c.ccliente = cli.id " +
                         "JOIN cjconfecciones.tpersona tp ON cli.idpersona = tp.cedula " +
                         "WHERE c.estado IN ('A', 'AB') " +
                         "ORDER BY c.id DESC";
            Query query = em.createNativeQuery(sql);
            List<Object[]> resultados = query.getResultList();

            StringBuilder html = new StringBuilder();
            html.append("<html><body>");
            html.append("<h2>Trabajos por Cobrar</h2>");
            html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse;'>");
            html.append("<tr style='background-color:#f2f2f2;'>");
            html.append("<th>Pedido ID</th><th>Fecha</th><th>Cliente</th><th>Estado</th><th>Total</th>");
            html.append("</tr>");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Object[] row : resultados) {
                html.append("<tr>");
                html.append("<td>").append(row[0]).append("</td>");
                html.append("<td>").append(row[1] != null ? sdf.format(row[1]) : "").append("</td>");
                html.append("<td>").append(row[2] != null ? row[2] : "").append("</td>");
                html.append("<td>").append(row[3] != null ? row[3] : "").append("</td>");
                html.append("<td>").append(row[4] != null ? row[4] : "").append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</body></html>");

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

            ObjectNode sender = mapper.createObjectNode();
            sender.put("name", "CJCONFECCIONES");
            sender.put("email", "cjconfecciones@percha.online");
            payload.set("sender", sender);

            ArrayNode toArray = mapper.createArrayNode();
            ObjectNode to = mapper.createObjectNode();
            String destinatario = propiedades.getParametrosProperties("emailNotificacionCobros");
            to.put("email", destinatario);
            to.put("name", "Notificacion Cobros");
            toArray.add(to);
            payload.set("to", toArray);

            SimpleDateFormat sdfAsunto = new SimpleDateFormat("dd/MM/yyyy");
            payload.put("subject", "Trabajos por Cobrar - " + sdfAsunto.format(new Date()));
            payload.put("htmlContent", html.toString());

            AsyncEmailSender asyncEmailSender = new AsyncEmailSender();
            asyncEmailSender.enviarAsync(() -> {
                try {
                    servicio.enviar(payload);
                    log.info("Notificacion de cobros enviada a: " + destinatario);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "ERROR AL ENVIAR NOTIFICACION DE COBROS", e);
                }
            });
            asyncEmailSender.shutdown();

            response.add("error", "0");
            response.add("message", "Notificacion enviada a: " + destinatario);
            response.add("totalPedidos", resultados.size());
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR EN NOTIFICAR COBROS", e);
            response.add("error", "1");
            response.add("message", e.getMessage());
        } finally {
            em.close();
        }
        return response.build();
    }

    public JsonObject cambiarEstadoConfeccion(JsonObject requestObject){
        JsonObjectBuilder response = Json.createObjectBuilder();
        EntityManager em = emf.createEntityManager();
        EntityTransaction t = em.getTransaction();
        try {
            t.begin();
            Integer pedidoId = Integer.parseInt(requestObject.getString("pedidoId"));
            Integer estadoId = Integer.parseInt(requestObject.getString("estadoId"));

            PedidoCabecera pc = em.find(PedidoCabecera.class, pedidoId);
            if (pc == null) {
                response.add("error", "1").add("message", "Pedido no encontrado");
                return response.build();
            }

            pc.setEstadoConfeccion(estadoId);
            em.merge(pc);

            HistorialEstadoPedido historial = new HistorialEstadoPedido();
            historial.setCpedido(pedidoId);
            historial.setCestado(estadoId);
            historial.setFecha(new Date());
            historial.setUsuario(requestObject.containsKey("usuario") ? requestObject.getString("usuario") : "SISTEMA");
            historial.setObservacion("Cambio de estado de confeccion");
            historial.setNotificacionMal((short) 0);
            historial.setNotificacionMovil((short) 0);
            em.persist(historial);

            t.commit();
            response.add("error", "0");
        } catch (Exception e) {
            t.rollback();
            log.log(Level.SEVERE, "ERROR AL CAMBIAR ESTADO CONFECCION", e);
            response.add("error", "1");
        } finally {
            em.close();
        }
        return response.build();
    }
}
