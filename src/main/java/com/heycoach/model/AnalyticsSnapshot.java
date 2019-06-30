/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.Exclude;
import com.heycoach.services.Utilities;

import java.util.HashMap;
import java.util.LinkedHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsSnapshot {

    String date;
    HashMap<String, String> list;

    public AnalyticsSnapshot() {
        this.date = Utilities.getUTCinISO();
        this.list = new LinkedHashMap<String, String>();

    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(this);
        return jsonInString;
    }

    @Override
    public String toString() {
        return "AnalyticsSnapshot{" +
                "date='" + date + '\'' +
                ", list=" + list +
                '}';
    }

    //*****************************************************************



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public HashMap<String, String> getList() {
        return list;
    }

    public void setList(HashMap<String, String> list) {
        this.list = list;
    }
}
