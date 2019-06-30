/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heycoach.model.ServiceResult;
import com.heycoach.model.User;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserServices {
    private static final Logger logger = Logger.getLogger(UserServices.class);
    private static final String STATEMENT_DESCRIPTOR = "Heycoach";
    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired EnvironmentServices environmentServices;

    public String getEmailforUsername(String username) {
        User user = firebaseClientServices.getUserByUsername(username);
        if (user != null) {
            return user.getEmail();
        } else
            return null;
    }


    public ServiceResult getUsers() {
        String users = firebaseClientServices.getNodeList("users");
        return new ServiceResult("OK" , "", users);
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<User>();
        logger.debug("Obtaining ALL users...");
        String results = firebaseClientServices.getNodeList("users");

        JSONObject nodes = new JSONObject(results);
        HashMap list = (HashMap<String, Object>) nodes.toMap();//converts it to a map of each entry being fbid, hashmap of data
        long i = 0;
        Iterator<Map.Entry<String, Map>> it = list.entrySet().iterator();//got through each node
        while (it.hasNext()) {
            Map.Entry<String, Map> fbNode = it.next();
            HashMap<String,Object> data = (HashMap<String, Object>) fbNode.getValue();//each node is a key and map of actual properties
            User user = new User();
            try {
                Utilities.setData(user, data);//use apache commons beanutils to hydrate the object based on the hasmap
            } catch (Exception e) {
                logger.debug("got error serializing but continuing: " + e.getMessage());
//                e.printStackTrace();
//                ErrorService.reportError(e);
            }
            users.add(user);
            //i += pair.getKey() + pair.getValue();
        }
        return users;
    }


    public User getUserByUID(String uid) {
        return firebaseClientServices.getUserByUID(uid);
    }

    public User getUserByUsername(String uid) {
        return firebaseClientServices.getUserByUsername(uid);
    }



    public ServiceResult deleteUser(String uid) {
        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + ".json";
        logger.debug("deleting user " + uid );

        if (firebaseClientServices.callFirebaseREST(uri, HttpMethod.DELETE, "")) {
            return new ServiceResult("OK" , "", "");
        } else
            return new ServiceResult("ERROR" , "", "");
    }

    public ServiceResult addUser(User user) throws JsonProcessingException {
        String uri = environmentServices.getFirebaseURI() + "/users.json";
        logger.debug("adding user " + user.getFullName());
        JSONObject result = null;
        result =firebaseClientServices.callFirebaseRESTreturnJSON(uri, HttpMethod.POST, user.toJson());
        String uid = result.getString("name");
        firebaseClientServices.updateUserField(uid, "uid", uid);
        if (result != null) {
//            uid = result.getString("name");
            return new ServiceResult("OK" , "", "", result);
        } else {
            return new ServiceResult("ERROR", "", "");
        }
    }
}
