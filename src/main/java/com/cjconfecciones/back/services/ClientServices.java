package com.cjconfecciones.back.services;

import com.cjconfecciones.back.controllers.ClienteController;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;

@Path("cliente")
public class ClientServices {

    @Inject
    private ClienteController clienteController;

    @POST
    @Path("/list")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject list(JsonObject request) {
        return clienteController.getAll();
    }

    @POST
    @Path("/get")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject get(JsonObject request) {
        return clienteController.getById(request);
    }

    @POST
    @Path("/create")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject create(JsonObject request) {
        return clienteController.create(request);
    }

    @POST
    @Path("/update")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject update(JsonObject request) {
        return clienteController.update(request);
    }

    @POST
    @Path("/delete")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject delete(JsonObject request) {
        return clienteController.delete(request);
    }
}
