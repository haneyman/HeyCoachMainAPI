package com.heycoach.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heycoach.services.ErrorService;
import com.heycoach.services.Utilities;

import java.io.IOException;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private String country;
    private String currency;
    private String email;
    private String lastName;
    private String firstName;
    private String lastActivity;
    private String lastActivityType;
    private String status;
    private String type;
    private String userId;
    private String uid;
    private Boolean reminderSent;
    private Account account;
    private String avatar;
    private String coachId;
    private String apnsToken;
    private String fcmToken;
    private String[] notifications;//actually an arraylist
//    private List<InvitationParent> invitations;
//    private ArrayList payments;
//    private List<Object> proposals;
//    private String proposals;
    //used for testing only at this juncture
    public User() {

    }

    public User(String country, String currency, String email, String lastName, String firstName, String lastActivity, String lastActivityType, String status, String type, String userId, String uid, Boolean reminderSent, Account account, String avatar, String coachId) {
        this.country = country;
        this.currency = currency;
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
        this.lastActivity = lastActivity;
        this.lastActivityType = lastActivityType;
        this.status = status;
        this.type = type;
        this.userId = userId;
        this.uid = uid;
        this.reminderSent = reminderSent;
        this.account = account;
        this.avatar = avatar;
        this.coachId = coachId;
    }

    public static User fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();

        //JSON from file to Object
        try {
            User user = mapper.readValue(json, User.class);
            return user;
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

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isCoach() {
        return this.type.equalsIgnoreCase(Utilities.USER_TYPE_COACH);
    }

    public boolean isClient()  {
        return this.type.equalsIgnoreCase(Utilities.USER_TYPE_CLIENT);
    }
    //getters setters


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCoachId() {
        return coachId;
    }

    public void setCoachId(String coachId) {
        this.coachId = coachId;
    }

    public String getLastActivityType() {
        return lastActivityType;
    }

    public void setLastActivityType(String lastActivityType) {
        this.lastActivityType = lastActivityType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public String getApnsToken() {
        return apnsToken;
    }

    public void setApnsToken(String apnsToken) {
        this.apnsToken = apnsToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String[] getNotifications() {
        return notifications;
    }

    public void setNotifications(String[] notifications) {
        this.notifications = notifications;
    }
}
