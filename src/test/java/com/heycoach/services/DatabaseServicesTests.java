/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.heycoach.model.User;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Mark on 12/18/2016.
 */
public class DatabaseServicesTests extends TestCase {
    private static final Logger logger = Logger.getLogger(DatabaseServicesTests.class);

    public static final String FIREBASE_USER_UID = "qJGDBKpKMeb2FwihUsAnnDQnttk1";//Client3 LastName3 client3@yahoo.com
    public static final String FIREBASE_USER_EMAIL = "client3@yahoo.com";
    //not working    @Autowired
//    private DatabaseServices dbServices;
    private FirebaseClientServices dbServices;
    @Autowired
    EnvironmentServices environmentServices;

    @Before
    public void setup() {
//        environmentServices.setHc_environment("DEVELOPMENT");
        environmentServices.setHc_environment("QA");
    }


/*    @Test
    public void testConnectToDN() throws FileNotFoundException {
        dbServices = new FirebaseServices();//constructor calls initialization, logs in
        //dbServices.connectToDB();
        assertTrue(dbServices != null);
        assertTrue(dbServices.isConnected());
    }*/

    @Test
    public void testGetFirebaseToken() throws IOException {
        dbServices = new FirebaseClientServices();//constructor calls initialization, logs in
        dbServices.connectToDB();
        String token = dbServices.getToken();
        logger.debug("Token from test:" + token);
        assertTrue(dbServices != null);
        assertTrue(token != null);
        assertTrue(token.length() > 100);

//        assertTrue(dbServices.isConnected());
    }

    @Test
    public void testGetFirebaseUser()  {
        dbServices = new FirebaseClientServices();//constructor calls initialization, logs in
        //String token = dbServices.getToken();
        User user = dbServices.getUserByUID(FIREBASE_USER_UID);
        assertTrue(user != null);
        assertTrue(user.getEmail().equalsIgnoreCase(FIREBASE_USER_EMAIL));
    }

    @Test
    public void testUpdateStripeFirebaseUser()  {
        dbServices = new FirebaseClientServices();//constructor calls initialization, logs in
        dbServices.updateUserStripe(FIREBASE_USER_UID, stripeAddAccountResult);
        //String token = dbServices.getToken();
    }

/*
    @Test
    public void testGetUser() throws FileNotFoundException {
        dbServices = new FirebaseServices();//constructor calls initialization, logs in
        //dbServices.connectToDB();
        assertTrue(dbServices != null);
        assertTrue(dbServices.isConnected());

        dbServices.getUserByUID("client1@aol.com");
//        User user = dbServices.getUserByUID("client1@aol.com");
//        assertTrue(user.getFirstName() == "Client");
    }
*/



}


