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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.sql.JEVisFileSQL;
import org.jevis.api.sql.JEVisSampleSQL;
import org.jevis.rest.JEVisConnectionCache;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonAttribute;
import org.jevis.rest.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * this Class handles all the JEVisSample related requests
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
     * @param onlyLatest
     * @return
     * @throws JEVisException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSampples(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest
    ) throws JEVisException {

        System.out.println("getSampples: " + id + "att: " + attribute);
        JEVisDataSource ds = JEVisConnectionCache.getInstance()
                .getDataSource(context.getUserPrincipal().getName());

        JEVisObject obj = ds.getObject(id);
        if (obj == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Object is not accessable").build();
        }

        JEVisAttribute att = obj.getAttribute(attribute);
        if (att == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Attribute is not accessable").build();
        }

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

        if (onlyLatest == true) {
            // TODO: what to do if there are no samples?
            if (!att.hasSample()) {
                return null;
            }
            JEVisSample sample = att.getLatestSample();
            return Response.ok(JsonFactory.buildSample(sample)).build();
        }
        if (start == null && end == null) {
            // TODO: if there are no samples the call returns as body "null"
            List<JsonSample> list = getAll(att);
            System.out.println("List<JsonSample> list: " + list.size());
            JsonSample[] returnList = list.toArray(new JsonSample[list.size()]);

            System.out.println("returnList: " + returnList.length);
            System.out.println("666");
            return Response.ok(returnList).build();
//            return getAll(att);
        } else {
            List<JsonSample> list = getInBetween(att, startDate, endDate);
            JsonSample[] returnList = list.toArray(new JsonSample[list.size()]);

            return Response.ok(returnList).build();
//            return getInBetweenl(att, startDate, endDate);
        }
    }

    @GET
    @Path("/Files")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSampleFiles(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            //@QueryParam("from") String start, //TODO: reimplement
            //@QueryParam("until") String end,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest
    ) throws JEVisException {

        JEVisDataSource ds = JEVisConnectionCache.getInstance()
                .getDataSource(context.getUserPrincipal().getName());

        JEVisObject obj = ds.getObject(id);
        if (obj == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Object is not accessable").build();
        }

        JEVisAttribute att = obj.getAttribute(attribute);
        if (att == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Attribute is not accessable").build();
        }
        if (!att.hasSample()) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Attribute has no samples").build();
        }
        if (att.getType().getPrimitiveType() != JEVisConstants.PrimitiveType.FILE) {
            //TODO: only implemented for files
            return Response.status(Status.SERVICE_UNAVAILABLE)
                    .entity("TODO: only implemented for files").build();
        }

        if (onlyLatest) {
            JEVisFile file = att.getLatestSample().getValueAsFile();
            byte[] arr = file.getBytes();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                bos.write(arr);
            } catch (IOException ex) {
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("IOException while writing file content to buffer"
                                + "IOException: " + ex.getMessage()).build();
            }

            ResponseBuilder response = Response.ok(arr);
            response.header("Content-Disposition",
                    "attachment; filename=\"" + file.getFilename() + "\"");
            return response.build();

        } else {
            //TODO: implement zipping of multiple files
            return Response.status(Status.SERVICE_UNAVAILABLE)
                    .entity("TODO: implement zipping of multiple files").build();
        }
    }

    @POST
    @Path("/Files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postSampleFiles(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws JEVisException {

        JEVisDataSource ds = JEVisConnectionCache.getInstance()
                .getDataSource(context.getUserPrincipal().getName());

        JEVisObject obj = ds.getObject(id);
        if (obj == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Object is not accessable").build();
        }
        JEVisAttribute att = obj.getAttribute(attribute);
        if (att == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Attribute is not accessable").build();
        }
        if (!att.hasSample()) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Attribute has no samples").build();
        }
        if (att.getType().getPrimitiveType() != JEVisConstants.PrimitiveType.FILE) {
            //TODO: only implemented for files
            return Response.status(Status.SERVICE_UNAVAILABLE)
                    .entity("TODO: only implemented for files").build();
        }

        // Get the Filename from the header
        String filename = fileDetail.getFileName();
        if (filename == null || filename.isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Empty filename").build();
        }

        // Read file content into buffer
        //TODO: determine how large files are allowed to be
        final int bufferSize = 1024 * 1024;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        try {
            bytesRead = uploadedInputStream.read(buffer);
        } catch (IOException ex) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("IOException while from InputStream"
                            + "IOException: " + ex.getMessage()).build();
        }
        if (bytesRead < 1) {
            // something went wrong, or empty file
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while reading input-file").build();
        }
        if (bytesRead >= bufferSize) {
            // Uploaded file was bigger than the buffer
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Uploaded file bigger than buffer-size").build();
        }
        byte[] fileContent = new byte[bytesRead];
        System.arraycopy(buffer, 0, fileContent, 0, bytesRead);

        System.out.println("Received file: " + filename + " with length: " + bytesRead);

        // create a new sample containing a file
        JEVisSample sample = att.buildSample(null, fileContent);
        JEVisFile jfile = sample.getValueAsFile();
        jfile.setFilename(filename);
        sample.commit();

        return Response.status(Status.CREATED).build();

        // Explanation Content type: http://stackoverflow.com/questions/20508788/do-i-need-content-type-application-octet-stream-for-file-download
        // Tutorial file-upload: http://www.mkyong.com/webservices/jax-rs/file-upload-example-in-jersey/
    }

    /**
     * Get all Samples between the given time-range
     *
     * @param att
     * @param start
     * @param end
     * @return
     * @throws JEVisException
     */
    private List<JsonSample> getInBetween(JEVisAttribute att, DateTime start, DateTime end) throws JEVisException {
        List<JsonSample> samples = new LinkedList<JsonSample>();
        for (JEVisSample sample : att.getSamples(start, end)) {
            samples.add(JsonFactory.buildSample(sample));
        }
        System.out.println("getInBetween: " + samples.size());
        return samples;
    }

    /**
     * Get all Samples for a JEVisAttribute
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
            System.out.println("DS is connected? " + ds.isConnectionAlive());
            System.out.println("getObject: " + id);
            JEVisObject object = ds.getObject(id);
            System.out.println("done");
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
                System.out.println("JEVisException while converting JsonSamples: "
                        + ex.getMessage());
            }

        }
        return newSamples;

    }

}
