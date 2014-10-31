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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.rest.AuthFilter;
import org.jevis.rest.JEVisConnectionCache;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonSample;
import org.joda.time.DateTime;

/**
 * This Class creates the Chart resource(page). This is just an example for
 * testing.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/api/rest/objects/{id}/attributes/{attribute}/chart")
public class ChartService {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getClass(
            @Context HttpHeaders httpHeaders,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("start") String start,
            @QueryParam("end") String end) throws JEVisException {

        System.out.println("GetChart");

        System.out.println("JEVis-ID: " + httpHeaders.getRequestHeaders().get(AuthFilter.HTTP_HEADER_USER));
        JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(httpHeaders.getRequestHeaders().getFirst(AuthFilter.HTTP_HEADER_USER));
//        JEVisDataSource ds = DSConnectionHandler.getInstance().getDataSource("Sys Admin");

        JEVisObject obj = ds.getObject(id);
//        JEVisObject obj = Config.getDS("Sys Admin", "jevis").getObject(id);
        JEVisAttribute att = obj.getAttribute(attribute);

//        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
//        DateTime startDate = null;
//        DateTime endDate = null;
//        if (start != null) {
//            startDate = fmt.parseDateTime(start);
//        }
//        if (end != null) {
//            endDate = fmt.parseDateTime(end);
//        }
//
//        if (start == null && end == null) {
//            return getAll(att);
//        } else {
//            return getInbetween(att, startDate, endDate);
//        }
        List<JEVisSample> samples = att.getAllSamples();
        return buildHTML(samples.subList(samples.size() - 31, samples.size() - 1), att);
    }

    private String buildHTML(List<JEVisSample> samples, JEVisAttribute attribute) {
        String title = "Chart Demo";
        String chartLib = "http://alpha.openjevis.org/js/Chart.js";

        Double maxValue = 0d;
        Double minValue = 1000000000d;

        for (JEVisSample samle : samples) {
            try {
                if (samle.getValueAsDouble() > maxValue) {
                    maxValue = samle.getValueAsDouble();

                }
                if (samle.getValueAsDouble() < minValue) {
                    minValue = samle.getValueAsDouble();
                }
            } catch (JEVisException ex) {
                Logger.getLogger(ChartService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Double stpes = (maxValue - minValue) / 10d;

        StringBuilder sb = new StringBuilder();

        sb.append("<!doctype html>");
        sb.append("<html><head>"
                + "<meta charset=\"UTF-8\">"
                + "<title>");
        sb.append(title);
        sb.append("</title>");
        sb.append("<script src=\"");
        sb.append(chartLib);
        sb.append("\"></script>");
        sb.append("</head>");

        sb.append("<body>\n"
                //                + "<h1>Chart Example v0.1</h1>\n"
                //                + "\n"
                + "<hr />\n"
                + "<p>Datapoint: <strong>" + attribute.getObject().getName() + "</strong></p>\n"
                + "\n"
                + "<p>Attribute: " + attribute.getName() + "</p>\n"
                + "\n"
                + "<p>First Sample: " + attribute.getTimestampFromFirstSample() + "</p>\n"
                + "\n"
                + "<p>Last Sample: " + attribute.getTimestampFromLastSample() + "</p>\n"
                + "\n"
                + "<hr />\n"
                + "<p>&nbsp;</p>"
                + "		<div style=\"width: 100% height=100%\">\n"
                + "			<canvas id=\"canvas\" height=\"450\" width=\"1024\"></canvas>\n"
                + "		</div>");

        sb.append("<script>\n");

        sb.append("var barChartData = {\n"
                + "		labels : " + getLabls(samples) + ",\n"
                + "		datasets : [\n"
                + "			{\n"
                + "				fillColor : \"rgba(220,220,220,0.5)\",\n"
                + "				strokeColor : \"#1a719c\",\n"
                + "				highlightFill: \"rgba(220,220,220,0.75)\",\n"
                + "				highlightStroke: \"rgba(220,220,220,1)\",\n"
                + "				data : " + getData(samples) + "\n"
                + "			}"
                + "		]\n"
                + "\n"
                + "	};"
        );

        sb.append("window.onload = function(){\n"
                + "		var ctx = document.getElementById(\"canvas\").getContext(\"2d\");\n"
                + "             ctx.canvas.width = window.innerWidth-40;\n"
                + "             ctx.canvas.height = window.innerHeight-260;\n"
                + "		window.myBar = new Chart(ctx).Line(barChartData, {\n"
                //                + "			responsive : true,\n"
                //                + "                             scaleOverride: true,\n"
                + "                             animationSteps: 80,\n"
                + "                             scaleBeginAtZero : false\n"
                //                + "                             scaleSteps: " + 7 + ",\n"
                //                + "                             scaleIntegersOnly: true,\n"
                //                + "                             scaleStepWidth :  " + 1 + ",\n"
                //                + "                             scaleStartValue: " + 20 + ",\n"
                + "		});\n"
                + "	};");

        sb.append("</script>\n"
                + "	</body>\n"
                + "</html>");

        return sb.toString();
    }

    private String getLabls(List<JEVisSample> samples) {
        StringBuilder sb = new StringBuilder();

        DateFormat dfmt = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        sb.append("[");

        for (JEVisSample sample : samples) {
            try {
                sb.append("\"");
                sb.append(dfmt.format(sample.getTimestamp().toDate()));
                sb.append("\",");
            } catch (JEVisException ex) {
                Logger.getLogger(ChartService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");

        return sb.toString();
    }

    private String getData(List<JEVisSample> samples) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (JEVisSample sample : samples) {
            try {
                sb.append(sample.getValue());
                sb.append(",");
            } catch (JEVisException ex) {
                Logger.getLogger(ChartService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");

        return sb.toString();

    }

    private List<JsonSample> getInbetween(JEVisAttribute att, DateTime start, DateTime end) throws JEVisException {
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
