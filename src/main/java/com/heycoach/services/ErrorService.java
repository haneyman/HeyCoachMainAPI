/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.rollbar.payload.Payload;
import com.rollbar.sender.PayloadSender;
import com.rollbar.sender.RollbarResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ErrorService {
    private static final Logger logger = Logger.getLogger(ErrorService.class);
    public static String environment;//this needs to be set!!

    public static final String SERVER_POST_ACCESS_TOKEN = "8f7c6b174a1f427790bf0feb04d05fb0";
    public static boolean isDisabled = false;

    public static void reportError(String error) {
        logger.error(error);

        Payload p = Payload.fromMessage(SERVER_POST_ACCESS_TOKEN, environment, error, null);
        PayloadSender payloadSender = new PayloadSender();
        try {
            RollbarResponse response = payloadSender.send(p);
            logger.debug("Rollbar payload sent, response:" + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Rollbar reportError(string) exception sending payload:" + e.getMessage());
        }

    }

    public static void reportError(Throwable t) {
        if (isDisabled) {
            logger.debug("Rollbar is disabled.  NOT reporting this error/exception.");
            return;
        }

        if (environment == null) {
            logger.debug("Defaulting environment to \"development\"");
            environment = "development";
        }
        Payload p = Payload.fromError(SERVER_POST_ACCESS_TOKEN, environment, t, null);
        PayloadSender payloadSender = new PayloadSender();
        try {
            // Here you can filter or transform the payload as needed before sending it
            RollbarResponse response = payloadSender.send(p);
            logger.debug("Rollbar payload sent, response:" + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Rollbar  reportError(throwable) exception sending payload:" + e.getMessage());
        }
    }
}
