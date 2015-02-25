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
package org.jevis.rest;

import java.util.ArrayList;
import org.jevis.rest.json.JsonJEVisClass;
import org.jevis.rest.json.JsonRelationship;
import org.jevis.rest.json.JsonSample;
import org.jevis.rest.json.JsonObject;
import org.jevis.rest.json.JsonType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.rest.json.JsonAttribute;
import org.jevis.rest.json.JsonClassRelationship;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This Factory can convert JEAPI interfaces into an Json representaion
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JsonFactory {

    /**
     * Default date format for attribute dates
     */
    private static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    /**
     * default date fomrat for JEVIsSamples Timestamps
     */
    private static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();

    /**
     * Build an Json representation of an JEVisAttribute list
     *
     * @param atts
     * @return
     * @throws JEVisException
     */
    public static List<JsonAttribute> buildAttributes(List<JEVisAttribute> atts) throws JEVisException {
        List<JsonAttribute> jAtts = new ArrayList<JsonAttribute>();
        for (JEVisAttribute att : atts) {
            jAtts.add(buildAttribute(att));
        }

        return jAtts;
    }

    /**
     * Build an Json representation of an JEVisAttribute
     *
     * @param att
     * @return
     * @throws JEVisException
     */
    public static JsonAttribute buildAttribute(JEVisAttribute att) throws JEVisException {
        JsonAttribute jatt = new JsonAttribute();

        if (att.hasSample()) {
            jatt.setBegins(attDTF.print(att.getTimestampFromFirstSample()));
            jatt.setEnds(attDTF.print(att.getTimestampFromLastSample()));
            jatt.setSampleCount(att.getSampleCount());
            //TODO: check if its an binary type, String value will be not taht usefull 
            jatt.setLatestValue(att.getLatestSample().getValueAsString());

        }
        jatt.setPeriod(att.getInputSampleRate().toString());
        jatt.setType(att.getType().getName());
        if (!att.getDisplayUnit().toString().isEmpty()) {
            jatt.setUnit(att.getDisplayUnit().toString());
        }

        return jatt;
    }

    /**
     * Build an Json representaion of an JEVisRelationship list
     *
     * @param objs
     * @return
     * @throws JEVisException
     */
    public static List<JsonRelationship> buildRelationship(List<JEVisRelationship> objs) throws JEVisException {
        List<JsonRelationship> jRels = new ArrayList<JsonRelationship>();
        for (JEVisRelationship rel : objs) {
            JsonRelationship json = new JsonRelationship();
            json.setStart(rel.getStartObject().getID());
            json.setEnd(rel.getEndObject().getID());
            json.setType(rel.getType());
            jRels.add(json);
        }

        return jRels;
    }

    /**
     * Build an Json representaion of an JEVisClass
     *
     * @param objs
     * @return
     * @throws JEVisException
     */
    public static List<JsonClassRelationship> buildClassRelationship(List<JEVisClassRelationship> objs) throws JEVisException {
        List<JsonClassRelationship> jRels = new ArrayList<JsonClassRelationship>();
        for (JEVisClassRelationship rel : objs) {
            JsonClassRelationship json = new JsonClassRelationship();
            json.setStart(rel.getStart().getName());
            json.setEnd(rel.getEnd().getName());
            json.setType(rel.getType());
            jRels.add(json);
        }

        return jRels;
    }

    /**
     * Build an Json representaion of an JEVisObject List
     *
     * @param objs
     * @return
     * @throws JEVisException
     */
    public static List<JsonObject> buildObject(List<JEVisObject> objs) throws JEVisException {
        List<JsonObject> jObjects = new ArrayList<JsonObject>();
        for (JEVisObject obj : objs) {
            jObjects.add(buildObject(obj));

        }

        return jObjects;
    }

    /**
     * Build an Json representaion of an JEVisObject
     *
     * @param obj
     * @return
     * @throws JEVisException
     */
    public static JsonObject buildObject(JEVisObject obj) throws JEVisException {
        JsonObject json = new JsonObject();
        json.setName(obj.getName());
        json.setId(obj.getID());
        json.setJevisClass(obj.getJEVisClass().getName());
        json.setRelationships(JsonFactory.buildRelationship(obj.getRelationships()));

        return json;
    }

    /**
     * Build an Json representaion of an JEVisRelationship
     *
     * @param rel
     * @return
     * @throws JEVisException
     */
    public static JsonRelationship buildRelationship(JEVisRelationship rel) throws JEVisException {
        JsonRelationship json = new JsonRelationship();
        json.setStart(rel.getStartObject().getID());
        json.setEnd(rel.getEndObject().getID());
        json.setType(rel.getType());//or as String lile Link
        return json;
    }

    /**
     * Build an Json representaion of an JEVisClass list
     *
     * @param classes
     * @return
     * @throws JEVisException
     */
    public static List<JsonJEVisClass> buildJEVisClass(List<JEVisClass> classes) throws JEVisException {
        List<JsonJEVisClass> jclasses = new ArrayList<JsonJEVisClass>();

        for (JEVisClass jc : classes) {
            jclasses.add(buildJEVisClass(jc));
        }

        return jclasses;
    }

    /**
     * Builds an Json reprensentation of JEVisClass
     *
     * @param jclass
     * @return
     * @throws JEVisException
     */
    public static JsonJEVisClass buildJEVisClass(JEVisClass jclass) throws JEVisException {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(jclass.getName());

        //Raltionship do this allready
//        if (jclass.getInheritance() != null) {
//            json.setInheritance(jclass.getInheritance().getName());
//        }
        json.setUnique(jclass.isUnique());
        json.setDescription(jclass.getDescription());

        json.setRelationships(buildClassRelationship(jclass.getRelationships()));

        return json;
    }

    /**
     * Build an Json representaion of an JEVIsSample
     *
     * @param sample
     * @return
     * @throws JEVisException
     */
    public static JsonSample buildSample(JEVisSample sample) throws JEVisException {
        JsonSample json = new JsonSample();

//        DateTimeFormatter fmt = ISODateTimeFormat.basicDateTimeNoMillis();
        json.setTs(sampleDTF.print(sample.getTimestamp()));
        json.setValue(sample.getValue().toString());
        if (!sample.getNote().isEmpty()) {
            json.setNote(sample.getNote());
        }

        return json;
    }

    /**
     * Build an list of json representaions of JEVisTypes
     *
     * @param types
     * @return
     */
    public static List<JsonType> buildTypes(List<JEVisType> types) {
        List<JsonType> jtypes = new ArrayList<JsonType>();

        for (JEVisType type : types) {
            try {
                jtypes.add(buildType(type));
            } catch (JEVisException ex) {
                Logger.getLogger(JsonFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jtypes;
    }

    /**
     * Build an JSon representaion of an JEVisType
     *
     * @param type
     * @return
     * @throws JEVisException
     */
    public static JsonType buildType(JEVisType type) throws JEVisException {
        JsonType json = new JsonType();
        json.setDescription(type.getDescription());
        json.setGUIDisplayType(type.getGUIDisplayType());
        json.setPrimitiveType(type.getPrimitiveType());
        json.setName(type.getName());
        json.setValidity("" + type.getValidity());
        return json;
    }
}
