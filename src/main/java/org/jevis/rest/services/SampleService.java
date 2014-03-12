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
import javax.ws.rs.core.MediaType;
import org.jevis.jeapi.JEVisAttribute;
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.JEVisObject;
import org.jevis.jeapi.JEVisSample;
import org.jevis.rest.Config;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/api/rest/objects/{id}/attributes/{attribute}/samples")
public class SampleService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonSample> getClass(
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("start") String start,
            @QueryParam("end") String end) throws JEVisException {

        Config config = new Config();
        JEVisObject obj = config.getDS("Sys Admin", "jevis").getObject(id);
        JEVisAttribute att = obj.getAttribute(attribute);

        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
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

    private List<JsonSample> getInBetweenl(JEVisAttribute att, DateTime start, DateTime end) throws JEVisException {
        List<JsonSample> samples = new LinkedList<JsonSample>();
        for (JEVisSample sample : att.getSamples(start, end)) {
            samples.add(JsonFactory.buildSample(sample));
        }

        return samples;
    }

    private List<JsonSample> getAll(JEVisAttribute att) throws JEVisException {
        List<JsonSample> samples = new LinkedList<JsonSample>();
        for (JEVisSample sample : att.getAllSamples()) {
            samples.add(JsonFactory.buildSample(sample));
        }

        return samples;
    }
}
