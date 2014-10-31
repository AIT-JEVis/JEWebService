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

import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.rest.JEVisConnectionCache;
import org.jevis.rest.JsonFactory;
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
    public List<JsonSample> getSampples(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end) throws JEVisException {

        JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());
//        JEVisDataSource ds = DSConnectionHandler.getInstance().getDataSource(httpHeaders.getRequestHeaders().getFirst(AuthFilter.HTTP_HEADER_USER));
        JEVisObject obj = ds.getObject(id);
        JEVisAttribute att = obj.getAttribute(attribute);

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

        if (start == null && end == null) {
            return getAll(att);
        } else {
            return getInBetweenl(att, startDate, endDate);
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

        return samples;
    }
}
