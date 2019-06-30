/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Created by Mark on 12/21/2016.
 */
public class Utilities {
    private static final Logger logger = Logger.getLogger(Utilities.class);

    public static final String USER_TYPE_COACH= "coach";
    public static final String USER_TYPE_CLIENT= "client";
    public static final String INVITATION_STATUS_SENT = "sent";
    private final static char[] idchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    public static final String CLIENT_STATUS_PENDING = "pending";//waiting for a proposal, new clients start here
    public static final String CLIENT_STATUS_UNPAID = "unpaid";//proposal made but no payment yet
    public static final String CLIENT_STATUS_PAID = "paid";//paid but not active yet, service hasn't started
    //CLIENT_STATUS_TRAINING = "training";wtf is this for???
    public static final String CLIENT_STATUS_ACTIVE = "active";//paid and service has started

    public static String getRandomAlphaNumeric(int len) {
        char[] id = new char[len];
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0;  i < len;  i++) {
            id[i] = idchars[r.nextInt(idchars.length)];
        }
        return new String(id).toUpperCase();//TODO: this is not truly random but sufficient???
    }

    public static Map<String, Object> convertJsonToMap(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map;
            map = mapper.readValue(json, new TypeReference<Map<String, String>>(){});
            logger.debug(map);
            return map;
        } catch (JsonGenerationException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }
    }

    public static String ObjectToJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }
    }


    public static Object setData(Object object, HashMap<String, Object> fields) throws InvocationTargetException, IllegalAccessException {
        for(Map.Entry<String, Object> entry : fields.entrySet()) {
            BeanUtils.setProperty(object, entry.getKey(), entry.getValue());
        }
        return object;
    }

    public static String getUTC()
    {
/*
        Calendar c = Calendar.getInstance();
        int utcOffset = 0;//c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);
//        int utcOffset = c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);
        Long utcMilliseconds = c.getTimeInMillis() + utcOffset;
        return utcMilliseconds.toString();
*/

        Instant instant = Instant.now();
        return instant.toString();
    }

    public static String getUTCinISO()
    {
        return Instant.now().toString();
    }

    public static String getPrettyDate() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/YYYY HH:mm");
        Date date = new Date();
        return simpleDate.format(date);
    }
}
