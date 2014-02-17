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

import java.util.LinkedList;
import java.util.List;
import org.jevis.jeapi.JEVisAttribute;
import org.jevis.jeapi.JEVisClass;
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.JEVisObject;
import org.jevis.jeapi.JEVisRelationship;
import org.jevis.jeapi.JEVisSample;
import org.jevis.jeapi.JEVisType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JsonFactory {

    public static JsonObject buildObject(JEVisObject obj) throws JEVisException {
        JsonObject json = new JsonObject();
        json.setName(obj.getName());
        json.setId(obj.getID());
        json.setJevisClass(obj.getJEVisClass().getName());





        json.setParent(22l);
//        List<JsonRelationship> rels = new LinkedList<JsonRelationship>();
//        for (JEVisRelationship rel : obj.getRelationships()) {
//            rels.add(JsonFactory.buildRelationship(rel));
//        }
//        json.setRelations(rels);
        return json;
    }

    public static JsonRelationship buildRelationship(JEVisRelationship rel) throws JEVisException {
        JsonRelationship json = new JsonRelationship();
        json.setStart(rel.getStartObject().getID());
        json.setEnd(rel.getEndObject().getID());
        json.setType(rel.getType());//or as String lile Link
        return json;
    }

    public static JsonJEVisClass buildJEVisClass(JEVisClass jclass) throws JEVisException {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(jclass.getName());
        if (jclass.getInheritance() != null) {
            json.setInheritance(jclass.getInheritance().getName());
        } else {
            json.setInheritance("null");
        }

        json.setUnique(jclass.isUnique());
        json.setDescription(jclass.getDescription());
        return json;
    }

    public static JsonAttribute buildAttribute(JEVisAttribute att) throws JEVisException {
        JsonAttribute json = new JsonAttribute();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

        json.setName(att.getName());
        if (att.hasSample()) {
            json.setFirstTS(fmt.print(att.getTimestampFromFirstSample()));
            json.setLastTS(fmt.print(att.getTimestampFromLastSample()));
            json.setLastvalue(att.getLatestSample().getValueAsString());
        }
        json.setSamplecount(att.getSampleCount());
        json.setPeriod("P15m");
        json.setObject(att.getObject().getID());


        return json;

    }

    public static JsonSample buildSample(JEVisSample sample) throws JEVisException {
        JsonSample json = new JsonSample();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        json.setTs(fmt.print(sample.getTimestamp()));
        json.setValue(sample.getValue().toString());
        json.setNote(sample.getNote());
        return json;
    }

    public static JsonType buildType(JEVisType type) throws JEVisException {
        JsonType json = new JsonType();
        json.setDescription(type.getDescription());
        json.setGUIDisplayType(type.getGUIDisplayType());
        json.setPrimitiveType(type.getPrimitiveType());
        json.setName(type.getName());
        json.setValidity(type.getValidity());
        return json;
    }
}
