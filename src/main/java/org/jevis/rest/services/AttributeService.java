/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest.services;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.rest.Config;
import org.jevis.rest.DebugFilter;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonAttribute;

/**
 * This Class handels all request for JEVisAttributes
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
//@Path("/objects/{id}/attributes/{attribute}")
@Path("/JEWebService/v1/objects/{id}/attributes")
public class AttributeService {

    /**
     * Returns an list of all attributes under the given JEVisClass
     *
     * @param context
     * @param httpHeaders
     * @param id
     * @return
     * @throws JEVisException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(AttributeService.class.getName()).log(Level.INFO, "GET Attributes for Object: " + id);

            ds = Config.getJEVisDS(httpHeaders);

            JEVisObject obj = ds.getObject(id);
            if (obj == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            List<JsonAttribute> atts = JsonFactory.buildAttributes(obj.getAttributes());
            JsonAttribute[] returnList = atts.toArray(new JsonAttribute[atts.size()]);

            return Response.ok(returnList).build();
        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    /**
     * Returns an specific attribute
     *
     * @param context
     * @param httpHeaders
     * @param id
     * @param attribute
     * @return
     * @throws JEVisException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{attribute}")
    public Response getAttribute(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute) throws JEVisException {
        System.out.println("getAttribute: " + attribute);

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(AttributeService.class.getName()).log(Level.INFO, "GET Attribute " + attribute + "for Object: " + id);
            ds = Config.getJEVisDS(httpHeaders);

            JEVisObject obj = ds.getObject(id);
            JsonAttribute att = JsonFactory.buildAttribute(obj.getAttribute(attribute));

            return Response.ok(att).build();

        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }
}
