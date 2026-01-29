package com.cjconfecciones.back.services;

import com.cjconfecciones.back.controllers.ClienteController;
import jakarta.annotation.Generated;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;

import java.util.logging.Logger;

@Path("test")
public class PersonServices {

    @Inject
    private ClienteController clienteController;

    Logger log = Logger.getLogger(PersonServices.class.getName());

    @GET
    @Path("persist")
    public void persistPersonServices(){
        log.info("WEB SERVICES STARTED");
        clienteController.newClient();
    }

    @POST
    @Path("/search")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject search(JsonObject jsonObject){
        return clienteController.searchClient4Name(jsonObject);
    }

    @POST
    @Path("search4name")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject search4name(JsonObject jsonObject){return clienteController.searchClient4Name(jsonObject);}
}
