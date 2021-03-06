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

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This Class is used to represents an JEVisObject in JSON by the WebService
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@XmlRootElement(name = "Object")
public class JsonObject {

    private String name;
    private long id;
    private String jevisclass;
    private long parent;
    private List<JsonRelationship> relations;
    private List<JsonObject> objects;
    private List<JsonAttribute> attributes;

    public JsonObject() {
    }

    @XmlElement(name = "attributes")
    public List<JsonAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<JsonAttribute> attributes) {
        this.attributes = attributes;
    }

    @XmlElement(name = "objects")
    public List<JsonObject> getObjects() {
        return objects;
    }

    public void setObjects(List<JsonObject> objects) {
        this.objects = objects;
    }

    /**
     * Returns the name of this JEVisObject
     *
     * @return name of the JEVisObject
     */
    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Set the name of the JEVisObject
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the JEVisObject.
     *
     * @return the unique id of this JEVisObject
     */
    @XmlElement(name = "id")
    public long getId() {
        return id;
    }

    /**
     * Set the name of the JEVisObject. The ID will be give by the Database and
     * will be ignored in the most cases.
     *
     * @param id Unique identifier of this JEVisObject
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the JEVisClass of this JEVisObject.
     *
     * @return JEVisClass of this JEVisObject.
     */
    @XmlElement(name = "jevisclass")
    public String getJevisClass() {
        return jevisclass;
    }

    /**
     * Set the JEVisClass of this JEVisObject.
     *
     * @param jevisclass
     */
    public void setJevisClass(String jevisclass) {
        this.jevisclass = jevisclass;
    }

    /**
     * Returns an list of all JEVisRelationships
     *
     * @return list of all JEVisRelationships
     */
    public List<JsonRelationship> getRelationships() {
        return relations;
    }

    /**
     * Set the List of JEVisRelationships.
     *
     *
     * @param relations
     */
    public void setRelationships(List<JsonRelationship> relations) {
        this.relations = relations;
    }

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

}
