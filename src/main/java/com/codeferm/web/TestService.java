package com.codeferm.web;

/*
 * Copyright (c) Bright House Networks. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 7, 2015
 * steve.goldsmith@mybrighthouse.com
 */
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * MAS SOAP services exposed as REST.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TestService {

    /**
     * Logger.
     */
    //CHECKSTYLE:OFF ConstantName
    private static final Logger log = Logger.getLogger(TestService.class.
            getName());
    //CHECKSTYLE:ON ConstantName

    /**
     * Load configuration.
     */
    @PostConstruct
    public final void init() {
        log.info("PostConstruct");
    }

    /**
     * Return data sent.
     *
     * @param data Test data.
     * @return Data sent.
     */
    @Path("/test")
    @POST
    public final Response getData(final String data) {
        Response response = Response.ok(data).build();
        // Return response
        return response;
    }
}
