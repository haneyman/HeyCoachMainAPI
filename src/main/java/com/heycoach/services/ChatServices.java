package com.heycoach.services;

import com.heycoach.controller.MainController;
import com.heycoach.model.ChatMessage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ChatServices {
    private static final Logger logger = Logger.getLogger(ChatServices.class);
    @Autowired FirebaseClientServices firebaseClientServices;

    public boolean markChatProposalPaid(String coachId, String clientId, String chatMessageId) {
        return firebaseClientServices.updateChatMessageText(coachId, clientId, chatMessageId, "paid");
    }

    public ChatMessage getChatMessage(String coachId, String clientId, String chatMessageId) {
        return firebaseClientServices.getChatMessage(coachId, clientId, chatMessageId);
    }
}
