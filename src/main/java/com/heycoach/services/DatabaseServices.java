/*
 * Copyright (c) 2016. HeyCoach & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.heycoach.model.Invitation;
import com.heycoach.model.User;

import javax.management.Notification;
import java.io.FileNotFoundException;

/**
 * Created by Mark on 12/18/2016.
 */
public interface DatabaseServices {
    public boolean connectToDB() throws FileNotFoundException;
    public User getUserByUID(String userId) ;
    public User getUserByUsername(String userName) ;
    public Invitation getInvitiation(String coachId, String invitationId) ;
    public boolean isConnected();


}
