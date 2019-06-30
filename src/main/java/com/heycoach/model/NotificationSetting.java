/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heycoach.services.ErrorService;

import java.io.IOException;

public class NotificationSetting {
    private String isDefault;
    private String id;
    private String label;
    private String message;
    private String order;
    private String type;
    private String title;


    public static NotificationSetting fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();

        //JSON from file to Object
        try {
            NotificationSetting notificationSetting = mapper.readValue(json, NotificationSetting.class);
            return notificationSetting;
        } catch (IOException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }
    }



    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
