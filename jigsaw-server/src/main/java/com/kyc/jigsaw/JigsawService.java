package com.kyc.jigsaw;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/")
public interface JigsawService {

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    String ping();

    @POST
    @Path("/solve")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void solve(
            @FormDataParam("up") InputStream upImage,
            @FormDataParam("left") InputStream leftImage,
            @FormDataParam("right") InputStream rightImage,
            @FormDataParam("down") InputStream downImage) throws IOException;
}
