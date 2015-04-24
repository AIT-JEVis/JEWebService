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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/api/rest/")
public class HomeService {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get() {
        String html = "<html>\n"
                + "    <body>\n"
                + "        <h2>JEVis WebService</h2>\n"
                + "        <br>\n"
                + "        <p>CheatSheet:</p>\n"
                + "        <ul>\n"
                + "            <li><a href=\"./rest/objects/1\" target=\"_blank\">Get an JEVisObject</a> </li>\n"
                + "            <li><a href=\"./rest/objects?class=Organization\" target=\"_blank\">Get an JEVisObjects by JEVisClass</a> </li>\n"
                + "            <li><a href=\"./rest/objects/35/attributes\" target=\"_blank\">Get all JEVisAttributes for an object</a>  </li>\n"
                + "            <li><a href=\"./rest/objects/35/attributes/Raw Data\" target=\"_blank\">Get an singel Attribute</a>  </li>\n"
                + "            <li><a href=\"./rest/objects/35/attributes/Raw Data/samples\" target=\"_blank\">GEt all JEVisSampels of an JEVisAttribute</a>  </li>\n"
                + "            <li><a href=\"./rest/objects/35/attributes/Raw Data/samples?start=2014-01-30T17:26:33.000%2B01:00&end=2014-01-30T17:26:53.000%2B01:00\" target=\"_blank\">Get all Samples in an time range</a>  </li>\n"
                + "            <li><a href=\"./rest/classes/\" target=\"_blank\">Get all JEVisClasses</a>  </li>\n"
                + "            <li><a href=\"./rest/classes/Data\" target=\"_blank\">Get an JEVisClass</a>  </li>\n"
                + "            <li><a href=\"./rest/objects/368/attributes/Raw Data/chart\" target=\"_blank\">Example Line Chart</a>  </li>\n"
                + "            <li><a href=\"./rest/version\" target=\"_blank\">Version of the WebService</a>  </li>\n"
                + "        </ul> \n"
                + "\n"
                + "<br>"
                + "<br>"
                + "<br>"
                + "  <p><a href=\" https://addons.mozilla.org/de/firefox/addon/jsonovich/?src=search\" target=\"_blank\">Tip, FireFox addon installieren damit Json besser aussieht!!</a> </li>\n"
                + "    </body>\n"
                + "</html>";

        return Response.ok(html).build();

    }
}
