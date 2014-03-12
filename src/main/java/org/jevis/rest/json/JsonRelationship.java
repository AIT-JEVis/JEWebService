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
package org.jevis.rest.json;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement
public class JsonRelationship {

    private long start;
    private long end;
    private int type;
    private static String pathObj = "/api/rest/objects/";

    public JsonRelationship() {
    }

    @XmlElement(name = "start")
    public String getStart() {
//        return start;
        return pathObj + start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    @XmlElement(name = "end")
    public String getEnd() {
        return pathObj + end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @XmlElement(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
