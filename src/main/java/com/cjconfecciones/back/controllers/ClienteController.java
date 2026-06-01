package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Persona;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class ClienteController implements Serializable {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;
    @PersistenceContext(unitName = "unitPersistence")
    private EntityManager em;

    Logger log = Logger.getLogger(ClienteController.class.getName());

    public void newClient (){
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        log.info("NUEVA PERSONA");
        Persona p = new Persona();
        p.setCedula("0104809470");
        em.persist(p);
        transaction.commit();
        log.info("INSERTO CLIENTE");
    }


    public JsonObject searchClient4NameOld(JsonObject requestObject){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try{
            String name = requestObject.getString("name");
            EntityManager entityManager = emf.createEntityManager();
            String sqlQuery = "select cedula, nombre , telefono, direccion  from cjconfecciones.tpersona t where upper(nombre) like  upper(:name)";
            Query query = entityManager.createNativeQuery(sqlQuery);
            query.setParameter("name",name+"%");
            List<Object[]> resultados = query.getResultList();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for(Object[] object : resultados){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("cedula", String.valueOf(object[0]));
                obj.add("nombre", String.valueOf(object[1]));
                obj.add("telefono",String.valueOf(object[2]));
                obj.add("direccion", String.valueOf(object[3]));
                arrayBuilder.add(obj);
            }
            jsonObjectBuilder.add("error", 0);
            jsonObjectBuilder.add("personas", arrayBuilder);
        }catch (Exception e){
            jsonObjectBuilder.add("error", 1);
            log.log(Level.SEVERE, "ERROR WHEN GET PERSONAS ",e);
        }
        return jsonObjectBuilder.build();
    }


    public JsonObject searchClient4Name(JsonObject requestObject){
        JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
        try{
            log.info("LLEGO AL METODO DEL BACK");
            List<Persona> personaList = em.createNamedQuery("Persona.buscarPorNombre", Persona.class)
                    .setParameter("nombre", "%"+requestObject.getString("nombres")+"%")
                    .getResultList();
            JsonArrayBuilder listaNombres = Json.createArrayBuilder();
            personaList.stream().map(p -> Json.createObjectBuilder()
                    .add("cedula", p.getCedula())
                    .add("nombre", p.getNombre())
                    .add("telefono", p.getTelefono())
                    .add("direccion",p.getDireccion())
                    .add("email", p.getEmail() != null ? p.getEmail() : "").build()
            ).forEach(listaNombres::add);
            responseBuilder.add("error","0")
                    .add ("nombres", listaNombres);
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR EN SEARCH CLIENT ",e);
            responseBuilder.add("error", "1");
        }
        return responseBuilder.build();
    }

    public JsonObject getAll(){
        JsonObjectBuilder response = Json.createObjectBuilder();
        try{
            List<Persona> lista = em.createQuery("SELECT p FROM Persona p WHERE (p.activo IS NULL OR p.activo = true) ORDER BY p.nombre", Persona.class).getResultList();
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for(Persona p : lista){
                arr.add(Json.createObjectBuilder()
                        .add("cedula", p.getCedula())
                        .add("nombre", p.getNombre() != null ? p.getNombre() : "")
                        .add("telefono", p.getTelefono() != null ? p.getTelefono() : "")
                        .add("direccion", p.getDireccion() != null ? p.getDireccion() : "")
                        .add("email", p.getEmail() != null ? p.getEmail() : ""));
            }
            response.add("error", "0").add("personas", arr);
        }catch(Exception e){
            log.log(Level.SEVERE, "ERROR EN getAll", e);
            response.add("error", "1");
        }
        return response.build();
    }

    public JsonObject getById(JsonObject request){
        JsonObjectBuilder response = Json.createObjectBuilder();
        try{
            String cedula = request.getString("cedula");
            Persona p = em.find(Persona.class, cedula);
            if(p == null){
                response.add("error", "1").add("mensaje", "Cliente no encontrado");
            }else{
                response.add("error", "0")
                        .add("cedula", p.getCedula())
                        .add("nombre", p.getNombre() != null ? p.getNombre() : "")
                        .add("telefono", p.getTelefono() != null ? p.getTelefono() : "")
                        .add("direccion", p.getDireccion() != null ? p.getDireccion() : "")
                        .add("email", p.getEmail() != null ? p.getEmail() : "");
            }
        }catch(Exception e){
            log.log(Level.SEVERE, "ERROR EN getById", e);
            response.add("error", "1");
        }
        return response.build();
    }

    public JsonObject create(JsonObject request){
        JsonObjectBuilder response = Json.createObjectBuilder();
        EntityManager emTrans = null;
        try{
            String cedula = request.getString("cedula");
            if(em.find(Persona.class, cedula) != null){
                response.add("error", "1").add("mensaje", "La cédula ya existe");
                return response.build();
            }
            Persona p = new Persona();
            p.setCedula(cedula);
            p.setNombre(request.getString("nombre"));
            p.setTelefono(request.containsKey("telefono") ? request.getString("telefono") : null);
            p.setDireccion(request.containsKey("direccion") ? request.getString("direccion") : null);
            p.setEmail(request.containsKey("email") ? request.getString("email") : null);
            p.setActivo(true);
            emTrans = emf.createEntityManager();
            emTrans.getTransaction().begin();
            emTrans.persist(p);
            emTrans.getTransaction().commit();
            response.add("error", "0").add("cedula", p.getCedula());
        }catch(Exception e){
            if(emTrans != null && emTrans.getTransaction().isActive()) emTrans.getTransaction().rollback();
            log.log(Level.SEVERE, "ERROR EN create", e);
            response.add("error", "1");
        }finally{
            if(emTrans != null) emTrans.close();
        }
        return response.build();
    }

    public JsonObject update(JsonObject request){
        JsonObjectBuilder response = Json.createObjectBuilder();
        EntityManager emTrans = null;
        try{
            String cedula = request.getString("cedula");
            Persona p = em.find(Persona.class, cedula);
            if(p == null){
                response.add("error", "1").add("mensaje", "Cliente no encontrado");
                return response.build();
            }
            if(request.containsKey("nombre")) p.setNombre(request.getString("nombre"));
            if(request.containsKey("telefono")) p.setTelefono(request.getString("telefono"));
            if(request.containsKey("direccion")) p.setDireccion(request.getString("direccion"));
            if(request.containsKey("email")) p.setEmail(request.getString("email"));
            emTrans = emf.createEntityManager();
            emTrans.getTransaction().begin();
            emTrans.merge(p);
            emTrans.getTransaction().commit();
            response.add("error", "0");
        }catch(Exception e){
            if(emTrans != null && emTrans.getTransaction().isActive()) emTrans.getTransaction().rollback();
            log.log(Level.SEVERE, "ERROR EN update", e);
            response.add("error", "1");
        }finally{
            if(emTrans != null) emTrans.close();
        }
        return response.build();
    }

    public JsonObject delete(JsonObject request){
        JsonObjectBuilder response = Json.createObjectBuilder();
        EntityManager emTrans = null;
        try{
            String cedula = request.getString("cedula");
            Persona p = em.find(Persona.class, cedula);
            if(p == null){
                response.add("error", "1").add("mensaje", "Cliente no encontrado");
                return response.build();
            }
            emTrans = emf.createEntityManager();
            emTrans.getTransaction().begin();
            Persona pManaged = emTrans.find(Persona.class, cedula);
            if(pManaged != null) {
                pManaged.setActivo(false);
                emTrans.merge(pManaged);
            }
            emTrans.getTransaction().commit();
            response.add("error", "0");
        }catch(Exception e){
            if(emTrans != null && emTrans.getTransaction().isActive()) emTrans.getTransaction().rollback();
            log.log(Level.SEVERE, "ERROR EN delete", e);
            response.add("error", "1");
        }finally{
            if(emTrans != null) emTrans.close();
        }
        return response.build();
    }
}
