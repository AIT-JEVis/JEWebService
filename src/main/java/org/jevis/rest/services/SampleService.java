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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.rest.Config;
import org.jevis.rest.JsonFactory;
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
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest
    ) throws JEVisException {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(SampleService.class.getName()).log(Level.INFO, "GET Sample for Object: " + id + " Attribute: " + attribute);

            ds = Config.getJEVisDS(httpHeaders);

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
                JsonSample[] returnList = list.toArray(new JsonSample[list.size()]);

                return Response.ok(returnList).build();
//            return getAll(att);
            } else {
                List<JsonSample> list = getInBetween(att, startDate, endDate);
                JsonSample[] returnList = list.toArray(new JsonSample[list.size()]);

                return Response.ok(returnList).build();
//            return getInBetweenl(att, startDate, endDate);
            }

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @GET
    @Path("/Files")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSampleFiles(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            //@QueryParam("from") String start, //TODO: reimplement
            //@QueryParam("until") String end,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest
    ) throws JEVisException {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(SampleService.class.getName()).log(Level.INFO, "GET File: " + id + " Attribute: " + attribute);

            ds = Config.getJEVisDS(httpHeaders);

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

                ResponseBuilder response = Response.ok(arr);
                response.header("Content-Disposition",
                        "attachment; filename=\"" + file.getFilename() + "\"");
                return response.build();

            } else {
                //TODO: implement zipping of multiple files
                return Response.status(Status.SERVICE_UNAVAILABLE)
                        .entity("TODO: implement zipping of multiple files").build();
            }
        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @POST
    @Path("/Files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postSampleFiles(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws JEVisException {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(SampleService.class.getName()).log(Level.INFO, "GET Files: " + id + " Attribute: " + attribute);

            ds = Config.getJEVisDS(httpHeaders);

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

            // Read file content from InputStream into byte-array
            byte[] fileContent;
            try {
                fileContent = IOUtils.toByteArray(uploadedInputStream);
            } catch (IOException ex) {
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("IOException while from InputStream"
                                + "IOException: " + ex.getMessage()).build();
            }
            if (fileContent.length < 1) {
                // something went wrong, or empty file
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("Error while reading input-file. Got empty file").build();
            }

            // create a new sample containing a file
            JEVisFile jFile = new JEVisFileImp();
            jFile.setFilename(filename);
            jFile.setBytes(fileContent);
            JEVisSample sample = att.buildSample(null, jFile);
            sample.commit();

            return Response.status(Status.CREATED)
                    .entity("Received file: " + filename + " with length: " + fileContent.length).build();
        } catch (JEVisException jex) {
            System.out.println("Error while fetching attribute: ");
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

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
        return samples;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postSamples(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            List<JsonSample> samples) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(SampleService.class.getName()).log(Level.INFO, "POST Samples for Object: " + id + " Attribute: " + attribute);

            ds = Config.getJEVisDS(httpHeaders);

//            for (JsonSample sample : samples) {
//                System.out.println("sample: " + sample.getTs() + " value: " + sample.getValue() + " note: " + sample.getNote());
//            }
            JEVisObject object = ds.getObject(id);
            JEVisAttribute att = object.getAttribute(attribute);

            List<JEVisSample> newSamples = toJEVisSample(att, samples);
            att.addSamples(newSamples);

            if (newSamples.size() > 0) {
                return Response.status(Status.CREATED).build();
            } else {
                return Response.status(Status.NOT_MODIFIED).build();
            }

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @DELETE
    public Response deleteSamples(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(SampleService.class.getName()).log(Level.INFO, "DELETE Sample for Object: " + id + " Attribute: " + attribute);

            ds = Config.getJEVisDS(httpHeaders);

            JEVisObject object = ds.getObject(id);
            JEVisAttribute att = object.getAttribute(attribute);

            DateTime startDate = null;
            DateTime endDate = null;
            if (onlyLatest) {
                // delete the latest sample
                if (!att.hasSample()) {
                    return Response.status(Status.NOT_FOUND)
                            .entity("Attribute has no samples").build();
                }

                JEVisSample latestSample = att.getLatestSample();
                DateTime timestamp = latestSample.getTimestamp();
                startDate = timestamp;
                endDate = timestamp;
            } else {
                // define the timerange to delete samples from
                if (start != null) {
                    startDate = fmt.parseDateTime(start);
                }
                if (end != null) {
                    endDate = fmt.parseDateTime(end);
                }
            }

            if (startDate == null && endDate == null) {
                att.deleteAllSample();
            } else {
                att.deleteSamplesBetween(startDate, endDate);
            }

            return Response.status(Status.OK).build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
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
                ex.printStackTrace();
            }

        }
        return newSamples;

    }

}
