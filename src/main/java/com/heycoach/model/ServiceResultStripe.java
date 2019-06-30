/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.model;

import com.stripe.model.Account;
import org.json.JSONObject;

public class ServiceResultStripe extends ServiceResult {

    Account stripeAccount;
    JSONObject json;
    String jsonString;

    public ServiceResultStripe(String result, String message, String responseToClient) {
        super(result, message, responseToClient);
    }

    public ServiceResultStripe(String result, String message, String responseToClient, Account stripeAccount) {
        super(result, message, responseToClient);
        this.stripeAccount = stripeAccount;
    }

    public ServiceResultStripe(String result, String message, String responseToClient, JSONObject jsonObject) {
        super(result, message, responseToClient);
        this.json = jsonObject;
    }

    public ServiceResultStripe(String result, String message, String responseToClient, String jsonString) {
        super(result, message, responseToClient);
        this.jsonString = jsonString;
    }

    //*******************************


    public Account getStripeAccount() {
        return stripeAccount;
    }

    public void setStripeAccount(Account stripeAccount) {
        this.stripeAccount = stripeAccount;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}
