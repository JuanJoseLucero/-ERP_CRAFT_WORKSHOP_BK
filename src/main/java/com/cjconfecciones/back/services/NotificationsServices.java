package com.cjconfecciones.back.services;

import com.cjconfecciones.back.controllers.NotificationController;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.logging.Logger;

@Path("notifications")
public class NotificationsServices {

     Logger log = Logger.getLogger(NotificationsServices.class.getName());

     @Inject
     private NotificationController notificationController;

     @POST
     @Path("/sendRemember")
     @Produces("application/json")
     @Consumes("application/json")
     public JsonObject getDataDashboard(){
         return notificationController.sendRemember();
     }

}
