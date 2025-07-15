package com.recorder.controller;

import com.recorder.model.BrowserAction;
import com.recorder.model.Locator;
import com.recorder.service.LocatorService;
import com.recorder.service.RecordingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/record")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecorderController {


    @Inject
    RecordingService recordingService;

    @Inject
    LocatorService locatorService;

    @POST
    @Path("/start")
    public Response startRecording(Map<String, String> payload) {
        String url = payload.get("url");
        return recordingService.start(url);
    }

    @POST
    @Path("/stop")
    public Response stopRecording() {
        return recordingService.stop();
    }

    @POST
    @Path("/playback")
    public Response playback() {
        return recordingService.playback();
    }

    @POST
    @Path("/log")
    public Response logAction(BrowserAction action) {
        return recordingService.log(action);
    }

    @POST
    @Path("/locator")
    public Response logLocator(Locator locator) {
        LocatorService.addLocator(locator);
        return Response.ok().build();
    }

    @GET
    @Path("/locators")
    public Response getLocators() {
        return Response.ok(locatorService.getRawLocators()).build();
    }

    @POST
    @Path("/clear")
    public Response clearAll() {
        RecordingService.clear();
        LocatorService.clear();
        return Response.ok().build();
    }

    @POST
    @Path("/dump")
    public Response dumpAll() {
        return Response.ok(RecordingService.getActions()).build();
    }
}
