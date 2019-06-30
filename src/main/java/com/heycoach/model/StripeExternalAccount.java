/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.model;

/**
 * Used for the controller input json in the post
 */
public class StripeExternalAccount {
    private String accountHolderName;
    private String accountHolderType;
    private String country;
    private String currency;
    private Boolean defaultForCurrency;
    private String accountNumber;
    private String routingNumber;
    private String metadata;


    /*********/
    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountHolderType() {
        return accountHolderType;
    }

    public void setAccountHolderType(String accountHolderType) {
        this.accountHolderType = accountHolderType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getDefaultForCurrency() {
        return defaultForCurrency;
    }

    public void setDefaultForCurrency(Boolean defaultForCurrency) {
        this.defaultForCurrency = defaultForCurrency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "StripeExternalAccount{" +
                "accountHolderName='" + accountHolderName + '\'' +
                ", accountHolderType='" + accountHolderType + '\'' +
                ", country='" + country + '\'' +
                ", currency='" + currency + '\'' +
                ", defaultForCurrency=" + defaultForCurrency +
                ", accountNumber='" + accountNumber + '\'' +
                ", routingNumber='" + routingNumber + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
