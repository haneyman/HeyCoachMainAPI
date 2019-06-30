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
public class LedgerItem implements Serializable {
    private String category;//paymentFromClient, ???
    private String description;//payment for proposal #111223
    private String datestamp;//when it occurred in UTC
    private String datestampPretty;//pretty when it occurred
    private String clientId;
    private String type;//credit or debit
    private Float amount;//total amount
    private Float heycoachFee;//fee for heycoach
    private Float processingFee;//fee for stripe
    private String sourceType;//client
    private String source;//client id
    private String destinationType;//stripeAccount
    private String destination;//[stripe account id]
    private String referenceId;//id of what they are paying for
    private String referenceType;//what they are paying for, e.g. proposal

    public final static String CATEGORY_CLIENT_PAYMENT = "payment:client";
    public final static String CATEGORY_PAYOUT = "payout";
    public final static String TYPE_DEBIT = "debit";
    public final static String TYPE_CREDIT = "credit";
    public final static String SOURCE_TYPE_CLIENT = "client";
    public final static String SOURCE_TYPE_COACH = "coach";
    public final static String SOURCE_TYPE_STRIPE_ACCOUNT = "account:stripe";
    public final static String DESTINATION_TYPE_STRIPE_ACCT = "account:stripe";
    public final static String DESTINATION_TYPE_BANK_ACCT = "account:bank";
    public final static String REFERENCE_TYPE_PROPOSAL = "proposal";
    public final static String REFERENCE_TYPE_PAYOUT = "payout";

    public LedgerItem(String category, String description, String datestamp, String datestampPretty, String clientId,
                      String type, Float amount, Float heycoachFee, Float processingFee, String sourceType, String source,
                      String destinationType, String destination, String referenceType, String referenceId) {
        this.category = category;
        this.description = description;
        this.datestamp = datestamp;
        this.datestampPretty = datestampPretty;
        this.clientId = clientId;
        this.type = type;
        this.amount = amount;
        this.sourceType = sourceType;
        this.source = source;
        this.destinationType = destinationType;
        this.destination = destination;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.heycoachFee = heycoachFee;
        this.processingFee = processingFee;
    }

    public static LedgerItem fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();

        //JSON from file to Object
        try {
            LedgerItem ledgerItem = mapper.readValue(json, LedgerItem.class);
            return ledgerItem;
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


    // getters, setters

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatestamp() {
        return datestamp;
    }

    public void setDatestamp(String datestamp) {
        this.datestamp = datestamp;
    }

    public String getDatestampPretty() {
        return datestampPretty;
    }

    public void setDatestampPretty(String datestampPretty) {
        this.datestampPretty = datestampPretty;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Float getHeycoachFee() {
        return heycoachFee;
    }

    public void setHeycoachFee(Float heycoachFee) {
        this.heycoachFee = heycoachFee;
    }

    public Float getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(Float processingFee) {
        this.processingFee = processingFee;
    }
}

