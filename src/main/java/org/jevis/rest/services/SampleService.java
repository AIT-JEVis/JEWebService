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
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.rest.JEVisConnectionCache;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonAttribute;
import org.jevis.rest.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * his Class handels all the JEVisSample related requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/objects/{id}/attributes/{attribute}/samples")
public class SampleService {

    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss").withZoneUTC();

    /**
     * Get the samples from an object/Attribute
     *
     * @param context
     * @param httpHeaders
     * @param id
     * @param attribute
     * @param start
     * @param end
     * @return
     * @throws JEVisException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
//    public List<JsonSample> getSampples(
    public Response getSampples(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end) throws JEVisException {

        System.out.println("getSampples: " + id + "att: " + attribute);
        JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());

        JEVisObject obj = ds.getObject(id);
        if (obj == null) {
            return Response.status(Status.NOT_FOUND).entity("Object is not accessable").build();
        }

        JEVisAttribute att = obj.getAttribute(attribute);

        System.out.println("1");
        //yyyyMMdd'T'HHmmssZ
//        DateTimeFormatter fmt = ISODateTimeFormat.basicOrdinalDateTimeNoMillis();
        DateTime startDate = null;
        DateTime endDate = null;
        if (start != null) {
            startDate = fmt.parseDateTime(start);
        }
        if (end != null) {
            endDate = fmt.parseDateTime(end);
        }
        System.out.println("2");

        if (start == null && end == null) {
            List<JsonSample> list = getAll(att);
            System.out.println("List<JsonSample> list: " + list.size());
            JsonSample[] returnList = list.toArray(new JsonSample[list.size()]);

            System.out.println("returnList: " + returnList.length);
            System.out.println("666");
            return Response.ok(returnList).build();
//            return getAll(att);
        } else {
            List<JsonSample> list = getInBetweenl(att, startDate, endDate);
            JsonSample[] returnList = list.toArray(new JsonSample[list.size()]);

            return Response.ok(returnList).build();
//            return getInBetweenl(att, startDate, endDate);
        }
    }

    /**
     * Get all Samples between the given timerange
     *
     * @param att
     * @param start
     * @param end
     * @return
     * @throws JEVisException
     */
    private List<JsonSample> getInBetweenl(JEVisAttribute att, DateTime start, DateTime end) throws JEVisException {
        List<JsonSample> samples = new LinkedList<JsonSample>();
        for (JEVisSample sample : att.getSamples(start, end)) {
            samples.add(JsonFactory.buildSample(sample));
        }
        System.out.println("getInBetween: " + samples.size());
        return samples;
    }

    /**
     * Get all Samples for an JEVisAttribute
     *
     * @param att
     * @return
     * @throws JEVisException
     */
    private List<JsonSample> getAll(JEVisAttribute att) throws JEVisException {
        List<JsonSample> samples = new LinkedList<JsonSample>();
        for (JEVisSample sample : att.getAllSamples()) {
            samples.add(JsonFactory.buildSample(sample));
        }
        System.out.println("getAll: " + samples.size());
        return samples;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postSamples(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            List<JsonSample> samples) {

        System.out.println("postSamples");

        System.out.println("Sample: ");
        for (JsonSample sample : samples) {
            System.out.println("sample: " + sample.getTs() + " value: " + sample.getValue() + " note: " + sample.getNote());
        }

        JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());
        try {
            JEVisObject object = ds.getObject(id);
            JEVisAttribute att = object.getAttribute(attribute);

            List<JEVisSample> newSamples = toJEVisSample(att, samples);
            System.out.println("Build new samples: " + newSamples.size());
            att.addSamples(newSamples);

            if (newSamples.size() > 0) {
                return Response.status(Status.CREATED).build();
            } else {
                return Response.status(Status.NOT_MODIFIED).build();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Status.NOT_FOUND).entity(ex.getMessage()).build();

        }

    }

    @DELETE
    public Response deleteSamples(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end) {

        JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());
        try {
            JEVisObject object = ds.getObject(id);
            JEVisAttribute att = object.getAttribute(attribute);

            DateTime startDate = null;
            DateTime endDate = null;
            if (start != null) {
                startDate = fmt.parseDateTime(start);
            }
            if (end != null) {
                endDate = fmt.parseDateTime(end);
            }

            if (startDate == null && endDate == null) {
                att.deleteAllSample();
            } else {
                att.deleteSamplesBetween(startDate, endDate);
            }

            return Response.status(Status.OK).build();

        } catch (JEVisException ex) {
            ex.printStackTrace();
            return Response.status(Status.NOT_FOUND).entity(ex.getMessage()).build();

        }
    }

    public List<JEVisSample> toJEVisSample(JEVisAttribute att, List<JsonSample> jsonSamples) {

        List<JEVisSample> newSamples = new ArrayList<JEVisSample>();
        for (JsonSample sample : jsonSamples) {
            try {
                Object value = null;
                if (att.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE) {
                    value = Double.parseDouble(sample.getValue());
                } else if (att.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                    value = Long.parseLong(sample.getValue());
                } else {//(att.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.STRING)
                    value = sample.getValue();
                }

                DateTime ts = JsonFactory.sampleDTF.parseDateTime(sample.getTs());

                if (sample.getNote() != null && !sample.getNote().isEmpty()) {
                    newSamples.add(att.buildSample(ts, value, sample.getNote()));
                } else {
                    newSamples.add(att.buildSample(ts, value));
                }

            } catch (JEVisException ex) {

            }

        }
        return newSamples;

    }

}
