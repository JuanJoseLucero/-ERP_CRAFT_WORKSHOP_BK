package com.cjconfecciones.back.services;


import com.cjconfecciones.back.controllers.OrderController;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;

@Path("order")
public class OrderServices {

    @Inject
    private OrderController orderController;


    @POST
    @Path("/getOrderById")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject getOrderById(JsonObject jsonObject){
        return orderController.getOrderById(jsonObject);
    }
    @POST
    @Path("/getOrders")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject getOrders(){
        return orderController.getOrders();
    }

    @POST
    @Path("/new")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject newOrder (JsonObject jsonObject){
        return orderController.newOrder(jsonObject);
    }

    @POST
    @Path("/searchClient")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject searchClient (JsonObject jsonObject){
        return orderController.searchClient(jsonObject);
    }
}
