/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseAdminServices {
    private static final Logger logger = Logger.getLogger(FirebaseAdminServices.class);

    //    private String configJson = "firebase-adminsdk-dev.json";
    @Autowired EnvironmentServices environmentServices;

    @Value(value = "classpath:firebase-adminsdk-dev.json")
    private Resource configJson;


    public FirebaseApp initialize() {
        InputStream serviceAccount = null;
        try {
            serviceAccount = configJson.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }

//        try {
//            serviceAccount = new FileInputStream(configJson);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            RollbarService.reportError(e);
//        }

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl(environmentServices.getFirebaseURI())
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        logger.debug(app.getName());  // "[DEFAULT]"

        return app;
    }

    public boolean test(FirebaseApp app) {
        return false;
    }



}
