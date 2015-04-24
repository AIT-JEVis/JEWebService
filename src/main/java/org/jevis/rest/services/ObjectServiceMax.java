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
import javax.ws.rs.core.SecurityContext;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.rest.JEVisConnectionCache;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonObject;

/**
 * This Class handels all the JEVisObject related requests
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/bigobjects")
public class ObjectServiceMax {

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
     * @param child
     * @param jclass
     * @param level
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObject(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @DefaultValue("true") @QueryParam("root") boolean root,
            @DefaultValue("") @QueryParam("class") String jclass,
            @DefaultValue("true") @QueryParam("inherit") boolean inherit,
            @DefaultValue("") @QueryParam("name") String name,
            @QueryParam("parent") long parent,
            @QueryParam("child") long child,
            @DefaultValue("2") @QueryParam("level") int level
    ) {

        try {
//            JEVisDataSource ds = DSConnectionHandler.getInstance().getDataSource(httpHeaders.getRequestHeaders().getFirst(AuthFilter.HTTP_HEADER_USER));
            JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());

            List<JEVisObject> objects = new ArrayList<JEVisObject>();

            System.out.println("root: " + root + " inherit: " + inherit);

            if (root) {
                System.out.println("get roots: " + root);
                objects = ds.getRootObjects();
                System.out.println("Total Root objects: " + objects.size());
            }

            if (!jclass.isEmpty()) {

                System.out.println("get classes: " + jclass);
                JEVisClass filterClass = ds.getJEVisClass(jclass);
                System.out.println("JEVisClass: " + filterClass);

                if (filterClass == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("JEVisClass does not exist.").build();
                }

                if (!root) {
                    System.out.println("Without roots filter");
                    objects = ds.getObjects(filterClass, inherit);
                    System.out.println("Total Objects with class: " + objects.size());
                }

                objects = filterClasses(objects, filterClass, inherit);
                System.out.println("Total Objects after Classfilter: " + objects.size());
            }
            List<JsonObject> jsonObjects = JsonFactory.buildObject(objects);

            JsonObject[] returnList = jsonObjects.toArray(new JsonObject[jsonObjects.size()]);
            System.out.println("final return total: " + returnList.length);

            return Response.ok(returnList).build();
        } catch (JEVisException ex) {
            ex.printStackTrace();
            return Response.status(404).entity(ex.getMessage()).build();
        }

    }

    @DELETE
    @Path("/{id}")
    public Response deleteObject(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id) {

        JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());

        if (id == -999) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing id path parameter").build();
        }

        try {
            JEVisObject obj = ds.getObject(id);
            obj.delete();
            System.out.println("Object " + id + "delete");
            return Response.status(Response.Status.OK).build();
        } catch (JEVisException jex) {
            //TODO: check case...
            jex.printStackTrace();
            return Response.status(Response.Status.FORBIDDEN).entity(jex.getMessage()).build();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(npe).build();
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postObject(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            JsonObject object) {

        try {
            JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());

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

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    /**
     * Get the JEVisObject with the given id.
     *
     * @param context
     * @param httpHeaders
     * @param id jevis internal id of an JEVisObject
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getObject(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @DefaultValue("-99999") @PathParam("id") long id) {
        try {
            System.out.println("getObject: " + id);
//            JEVisDataSource ds = DSConnectionHandler.getInstance().getDataSource(httpHeaders.getRequestHeaders().getFirst(AuthFilter.HTTP_HEADER_USER));
            JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());

            if (id == -999) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing id path parameter").build();
            }

            JEVisObject obj = ds.getObject(id);
            if (obj == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            JsonObject jobj = JsonFactory.buildObject(obj);

//            for (JEVisRelationship rel : obj.getRelationships()) {
//                jobj.setRelationships(null);
//            }
            System.out.println("Obj: " + obj);
            return Response.ok(jobj).build();
        } catch (JEVisException jex) {
            //TODO: check case...
            jex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity(jex.getMessage()).build();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(npe).build();
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

    /**
     * Post an new JEVisObject from the given JSon Body
     *
     * @param httpHeaders
     * @param json
     * @return
     */
//    @POST
////    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response post(
//            @Context HttpHeaders httpHeaders,
//            JsonObject json) {
//        try {
//            System.out.println("json: " + json);
//            System.out.println("create new obj: " + json.getName() + " " + json.getJevisClass());
////            JEVisDataSource ds = Config.getDS("Sys Admin", "jevis");
//            JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(httpHeaders.getRequestHeaders().getFirst(AuthFilter.HTTP_HEADER_USER));
//
//            System.out.println("get Parent Object: " + json.getRelationships().get(0));
////            if (json.getRelationships().get(0).getType()) {
////
////            }
//
//            JEVisObject parentObj = ds.getObject(440l);//TODO replace with code
//            System.out.println("Found parent: " + parentObj);
//
//            System.out.println("Get JEVisClass: " + json.getJevisClass());
//            JEVisClass jevClass = ds.getJEVisClass(json.getJevisClass());
//            System.out.println("Found JEVisClass: " + jevClass);
//
//            System.out.println("Build new Object: " + json.getName() + " " + jevClass.getName());
//            JEVisObject newObj = parentObj.buildObject(json.getName(), jevClass);
//
//            System.out.println("Commit");
//            newObj.commit();
//            System.out.println("done");
//            return Response.ok(JsonFactory.buildObject(newObj)).build();
//        } catch (JEVisException ex) {
//            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
//        }
//
//    }
}
