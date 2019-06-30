package com.heycoach.model;

import org.json.JSONObject;

/**
 * contains results of a call to a business service
 *
 */
public class ServiceResult {
    private String result;//TODO: ENUM this
    private String message;
    private String responseToClient;
    private JSONObject data;

    public ServiceResult(String result, String message, String responseToClient) {
        this.result = result;
        this.message = message;
        this.responseToClient = responseToClient;
    }

    public ServiceResult(String result, String message, String responseToClient, JSONObject data) {
        this.result = result;
        this.message = message;
        this.responseToClient = responseToClient;
        this.data = data;
    }

    //*****

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponseToClient() {
        return responseToClient;
    }

    public void setResponseToClient(String responseToClient) {
        this.responseToClient = responseToClient;
    }
}
