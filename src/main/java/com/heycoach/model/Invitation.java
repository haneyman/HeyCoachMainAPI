/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heycoach.services.ErrorService;

import java.io.IOException;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Invitation  implements Serializable {
    private String accessCode;
    private String clientId;
    private String createdDate;
    private String id;
    private String message;
    private String receiver;
    private String reminderSent;
    private String sent;
    private String status;

    public Invitation() {

    }

    public Invitation(String accessCode, String clientId, String createdDate, String id, String message, String receiver, String reminderSent, String sent, String status) {
        this.accessCode = accessCode;
        this.clientId = clientId;
        this.createdDate = createdDate;
        this.id = id;
        this.message = message;
        this.receiver = receiver;
        this.reminderSent = reminderSent;
        this.sent = sent;
        this.status = status;
    }

    public static Invitation fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Invitation invitation = mapper.readValue(json, Invitation.class);
            return invitation;
        } catch (IOException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(this);
        return jsonInString;
    }


    //*****************************************


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(String reminderSent) {
        this.reminderSent = reminderSent;
    }
}
