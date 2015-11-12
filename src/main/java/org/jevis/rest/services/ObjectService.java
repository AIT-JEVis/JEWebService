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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.rest.Config;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonObject;

/**
 * This Class handels all the JEVisObject related requests
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/objects")
public class ObjectService {

    /**
     * Get an list of JEVisObject Resource.
     *
     * TODO: maybe use an async response!?
     * https://jersey.java.net/documentation/latest/async.html
     *
     * @param context
     * @param httpHeaders
     * @param root
     * @param inherit
     * @param parent
     * @param name
     * @param detailed
     * @param child
     * @param jclass
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @DefaultValue("true") @QueryParam("root") boolean root,
            @DefaultValue("") @QueryParam("class") String jclass,
            @DefaultValue("true") @QueryParam("inherit") boolean inherit,
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("false") @QueryParam("detail") boolean detailed,
            @QueryParam("parent") long parent,
            @QueryParam("child") long child) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(ObjectService.class.getName()).log(Level.INFO, "GET Objects");
            ds = Config.getJEVisDS(httpHeaders);
            List<JEVisObject> objects = new ArrayList<JEVisObject>();

            if (root) {
                objects = ds.getRootObjects();
            }

            if (!jclass.isEmpty()) {
                JEVisClass filterClass = ds.getJEVisClass(jclass);

                if (filterClass == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("JEVisClass does not exist.").build();
                }

                if (!root) {
                    objects = ds.getObjects(filterClass, inherit);
                } else {
                    //@todo: this may throw an exeption
                    objects = filterClasses(objects, filterClass, inherit);
                }
            }

            List<JsonObject> jsonObjects;
            if (detailed) {
                jsonObjects = JsonFactory.buildDetailedObject(objects);
            } else {
                jsonObjects = JsonFactory.buildObject(objects);
            }

            JsonObject[] returnList = jsonObjects.toArray(new JsonObject[jsonObjects.size()]);

            return Response.ok(returnList).build();

        } catch (JEVisException jex) {
            System.out.println("Error while fetching attribute: ");
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @DELETE
    @Path("/{id}")
    public Response deleteObject(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(ObjectService.class.getName()).log(Level.INFO, "DELETE Object: " + id);

            ds = Config.getJEVisDS(httpHeaders);

            JEVisObject obj = ds.getObject(id);
            obj.delete();
            return Response.status(Response.Status.OK).build();

        } catch (JEVisException jex) {
            System.out.println("Error while fetching attribute: ");
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postObject(
            @Context HttpHeaders httpHeaders,
            JsonObject object) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(ObjectService.class.getName()).log(Level.INFO, "POST Object ");
            ds = Config.getJEVisDS(httpHeaders);

            JEVisObject parent = ds.getObject(object.getParent());
            if (parent == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Parent not found").build();
            }
            JEVisClass jclass = ds.getJEVisClass(object.getJevisClass());
            if (jclass == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("JEVisClass not found").build();
            }

            JEVisObject newObj = parent.buildObject(object.getName(), jclass);
            JsonObject job = JsonFactory.buildObject(newObj);
            return Response.ok(job).build();
        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().entity(jex.toString()).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Get the JEVisObject with the given id.
     *
     * @param context
     * @param httpHeaders
     * @param detailed
     * @param id jevis internal id of an JEVisObject
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @DefaultValue("false") @QueryParam("detail") boolean detailed,
            @DefaultValue("-99999") @PathParam("id") long id) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(ObjectService.class.getName()).log(Level.INFO, "GET Object: " + id);

            ds = Config.getJEVisDS(httpHeaders);

            if (id == -999) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing id path parameter").build();
            }

            JEVisObject obj = ds.getObject(id);
            if (obj == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            JsonObject jobj;
            if (detailed) {
                jobj = JsonFactory.buildDetailedObject(obj);
            } else {
                jobj = JsonFactory.buildObject(obj);
            }

            return Response.ok(jobj).build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Returns an list with only Objects from the given JEVisClass
     *
     * @param objects list to filter
     * @param jclass JEVisClass to filter for
     * @param heirs true if also include the heirs of the JEVisClass
     * @return filtered list
     * @throws JEVisException
     */
    private List<JEVisObject> filterClasses(List<JEVisObject> objects, JEVisClass jclass, boolean heirs) throws JEVisException {
        List<JEVisObject> filtered = new ArrayList<JEVisObject>();
        List<JEVisClass> filterClasses = new ArrayList<JEVisClass>();
        filterClasses.add(jclass);
        if (heirs) {
            filterClasses.addAll(jclass.getHeirs());
        }

        for (JEVisObject obj : objects) {
            for (JEVisClass fclass : filterClasses) {
                if (obj.getJEVisClass().equals(fclass)) {
                    filtered.add(obj);
                }
            }

        }

        return filtered;
    }

}
