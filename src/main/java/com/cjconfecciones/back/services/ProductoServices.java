package com.cjconfecciones.back.services;

import com.cjconfecciones.back.controllers.ProductoController;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("producto")
public class ProductoServices {

    @Inject
    private ProductoController productoController;

    Logger log = Logger.getLogger(ProductoServices.class.getName());

    @POST
    @Path("autocompleteProduct")
    public Response autocompleteProduct(JsonObject textJson){
        try{
            return Response.ok(productoController.search4Description4Client(textJson)).build();
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR AUTOCOMPLETE ",e);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("saveProducts")
    public Response saveProducts(JsonObject productos){
        try {
            return Response.ok(productoController.persistProduct(productos)).build();
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR SAVE PRODUCTS ",e);
            return  Response.serverError().build();
        }
    }

    @POST
    @Path("update4Id")
    public Response update4Id(JsonObject  data){
        try{
            return Response.ok(productoController.update4Id(data)).build();
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR UPDATE 4 ID ",e);
        }
        return Response.serverError().build();
    }
    @POST
    @Path("get4Id")
    public Response get4Id(JsonObject  data){
        try{
            return Response.ok(productoController.getProductById(data)).build();
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR GET PRODUCT 4 ID ",e);
        }
        return Response.serverError().build();
    }

    @POST
    @Path("getListProducts")
    public Response getListProducts(){
        try{
            return Response.accepted(productoController.getLstProductos()).build();
        }catch (Exception e){
            log.log(Level.SEVERE,"ERROR TO GET PRODUCTS ",e);
            return Response.serverError().build();
        }
    }
}
