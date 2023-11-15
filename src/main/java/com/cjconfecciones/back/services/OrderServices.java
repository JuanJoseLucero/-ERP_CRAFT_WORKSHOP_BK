package com.cjconfecciones.back.services;


import com.cjconfecciones.back.controllers.OrderController;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("order")
public class OrderServices {

    @Inject
    private OrderController orderController;

    @POST
    @Path("/new")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject newOrder (JsonObject jsonObject){
        return orderController.newOrder(jsonObject);
    }
}
