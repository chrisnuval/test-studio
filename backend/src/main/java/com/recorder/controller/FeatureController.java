package com.recorder.controller;

import com.recorder.model.FeatureCleanRequest;
import com.recorder.service.FeatureService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/record")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class FeatureController {

    @Inject
    FeatureService featureService;

    @POST
    @Path("/feature")
    public Response generateFeature() {
        return featureService.generateFeature();
    }

    @POST
    @Path("/clean-feature")
    public Response cleanFeature(FeatureCleanRequest request) {
        return featureService.cleanFeature(request);
    }
}
