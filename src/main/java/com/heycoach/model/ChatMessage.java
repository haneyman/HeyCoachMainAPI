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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {
    public static final String CHAT_TYPE_STANDARD = "standard";
    public static final String CHAT_TYPE_PROPOSAL = "proposal";
    public static final String CHAT_TYPE_SYSTEM = "system";
    public static final String CHAT_TYPE_IMAGE = "image";

    private String id;
    private String text;
    private String link;
    private String senderId;
    private String recipientId;
    private String coachId;
    private String clientId;
    private String type;
//    private String object;   if you add this back in it gets a jackson error, see NotificationServiceTests.testNotifyAboutChatToCoachProposalException
    private String read;
    private String sent;

    public ChatMessage() {};//for Jackson

    public ChatMessage(String id, String text, String link, String senderId, String recipientId, String coachId, String clientId, String type, String object, String read, String sent) {
        this.id = id;
        this.text = text;
        this.link = link;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.coachId = coachId;
        this.clientId = clientId;
        this.type = type;
//        this.object = object;
        this.read = read;
        this.sent = sent;
    }

    public static ChatMessage fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();

        //JSON from file to Object
        try {
            ChatMessage chatMessage = mapper.readValue(json, ChatMessage.class);
            return chatMessage;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getCoachId() {
        return coachId;
    }

    public void setCoachId(String coachId) {
        this.coachId = coachId;
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

/*
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }
*/

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }
}
