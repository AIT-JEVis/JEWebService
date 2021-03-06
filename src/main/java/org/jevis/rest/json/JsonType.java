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
 * This Class represents an JEVisType as Json
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "JEVisType")
public class JsonType {

    private String name;
    private int primitiveType;
    private String GUIDisplayType;
    private int GUIPosition;
    private String validity;
    private String description;

    public JsonType() {
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "premitiveType")
    public int getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(int primitiveType) {
        this.primitiveType = primitiveType;
    }

    @XmlElement(name = "guiDisplayType")
    public String getGUIDisplayType() {
        return GUIDisplayType;
    }

    public void setGUIDisplayType(String GUIDisplayType) {
        this.GUIDisplayType = GUIDisplayType;
    }

    @XmlElement(name = "guiPosition")
    public int getGUIPosition() {
        return GUIPosition;
    }

    public void setGUIPosition(int GUIPosition) {
        this.GUIPosition = GUIPosition;
    }

    @XmlElement(name = "validity")
    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
