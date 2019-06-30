/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heycoach.services.ErrorService;

/**
 * Used for input values when adding a stripe account
 */
public class StripeAccount {
    private String ipAddress;
    private String country;
    private Integer dobMonth;
    private Integer dobDay;
    private Integer dobYear;
    private String firstName;
    private String lastName;
    private String ssnLast4;
    private String addressLine1;
    private String addressCity;
    private String addressState;
    private String addressPostal;
    private String legalEntityType;
    private String businessName;
    private String taxId;
    private String email;
    private String id;

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }
    }


/*
    public void loadFromStripe(Account account) {
        this.country    = account.getCountry();
        this.dobMonth   = account.getLegalEntity().getDob().getMonth();
        this.dobDay     = account.getLegalEntity().getDob().getDay();
        this.dobYear    = account.getLegalEntity().getDob().getYear();
        this.firstName  = account.getLegalEntity().getFirstName();
        this.lastName   = account.getLegalEntity().getLastName();
        this.ssnLast4   = account.getLegalEntity().get
    }


    //merges changes in from given account
    public void mergeFrom(com.stripe.model.Account stripeAccount, StripeAccount fromAccount) {
        this.



        if (fromAccount.getAddressCity() != null)
            this.setAddressCity(fromAccount.getAddressCity());
        if (fromAccount.getAddressLine1() != null)
            this.setAddressLine1 (fromAccount.getAddressLine1());
        if (fromAccount.getAddressPostal() != null)
            this.setAddressPostal (fromAccount.getAddressPostal());
        if (fromAccount.getAddressState() != null)
            this.setAddressState (fromAccount.getAddressState());
        if (fromAccount.getBusinessName() != null)
            this.setBusinessName (fromAccount.getBusinessName());
        if (fromAccount.getCountry() != null)
            this.setCountry (fromAccount.getCountry());
        if (fromAccount.getIpAddress() != null)
            this.setIpAddress (fromAccount.getIpAddress());
        if (fromAccount.getDobMonth() != null)
            this.setDobMonth (fromAccount.getDobMonth());
        if (fromAccount.getDobDay() != null)
            this.setDobDay (fromAccount.getDobDay());
        if (fromAccount.getDobYear() != null)
            this.setDobYear (fromAccount.getDobYear());
        if (fromAccount.getFirstName() != null)
            this.setFirstName (fromAccount.getFirstName());
        if (fromAccount.getLastName() != null)
            this.setLastName (fromAccount.getLastName());
        if (fromAccount.getSsnLast4() != null)
            this.setSsnLast4 (fromAccount.getSsnLast4());
        if (fromAccount.getLegalEntityType() != null)
            this.setLegalEntityType (fromAccount.getLegalEntityType());
        if (fromAccount.getTaxId() != null)
            this.setTaxId (fromAccount.getTaxId());
        if (fromAccount.getEmail() != null)
            this.setEmail (fromAccount.getEmail());
    }
*/

    //**************************

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getDobMonth() {
        return dobMonth;
    }

    public void setDobMonth(Integer dobMonth) {
        this.dobMonth = dobMonth;
    }

    public Integer getDobDay() {
        return dobDay;
    }

    public void setDobDay(Integer dobDay) {
        this.dobDay = dobDay;
    }

    public Integer getDobYear() {
        return dobYear;
    }

    public void setDobYear(Integer dobYear) {
        this.dobYear = dobYear;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSsnLast4() {
        return ssnLast4;
    }

    public void setSsnLast4(String ssnLast4) {
        this.ssnLast4 = ssnLast4;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getAddressPostal() {
        return addressPostal;
    }

    public void setAddressPostal(String addressPostal) {
        this.addressPostal = addressPostal;
    }

    public String getLegalEntityType() {
        return legalEntityType;
    }

    public void setLegalEntityType(String legalEntityType) {
        this.legalEntityType = legalEntityType;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
