package com.heycoach.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.heycoach.model.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * RESTful interaction with Firebase
 * Waaaaaay better than that damn event-based crap
 */
@Service
public class FirebaseClientServices implements DatabaseServices {
    private static final Logger logger = Logger.getLogger(FirebaseClientServices.class);
    @Autowired EnvironmentServices environmentServices;

    private boolean isConnected;
    private String configJson;
//    private String configJson = "/firebase-adminsdk-dev.json";
    private String token = null;//the current token
    private String error = "";
    private ResponseEntity<String> response;//the last response
    private boolean hasAcknowlegedURL = false;//whether a system output message has been sent with firebase URL - just want to see once

    private void getEnv() {
        try {
            configJson = environmentServices.getFirebaseJson();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            logger.debug("CANNOT CONNECT TO DB!!!!!    Could not connect to DB using json from EnvironmentServices");
            configJson = "CANNOT CONNECT TO DB";
        }
    }
    /**
     *
     * Obtains token from Firebase, no actual persistent connection
     * @return
     */
    public boolean connectToDB()  {
        getEnv();
        //TODO: could be called multiple times so check the token for age and if young skip refresh

        //this json file is generated and downloaded to the firebase admin sdk service account at
        //  https://console.firebase.google.com/project/heycoach-prod/settings/serviceaccounts/adminsdk

        InputStream inputStream = getClass().getResourceAsStream(configJson);
        if (inputStream == null) {
            logger.debug("Firebase config not found: " + configJson);
        }


        GoogleCredential googleCred;
        GoogleCredential scoped;
        try {
            googleCred = GoogleCredential.fromStream(inputStream);
            scoped = googleCred.createScoped(
                    Arrays.asList(
                            "https://www.googleapis.com/auth/firebase.database",
                            "https://www.googleapis.com/auth/userinfo.email"
                    )
            );
            scoped.refreshToken();
        } catch (IOException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return false;
        }
        this.token = scoped.getAccessToken();
//        logger.debug("Firebase token acquired successfully. " + this.getToken().substring(1, 50) + "...");
        return true;
    }

    public String getCondition() {
        return "1";
    }

    public String getParameter(String condition, String parameterName) {
        logger.debug("getting parameter " + parameterName + " for condition " + condition);
        String uri = environmentServices.getFirebaseURI() + "/remoteConfig/parameters/" + parameterName + "/" + condition + ".json";
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            String result = response.getBody();
            if (!result.equalsIgnoreCase("null") && result.length() > 0)
                return response.getBody();
            else {
                logger.debug("    ERROR!! parameter not found: " + parameterName + "   condition: " + condition);
                return null;
            }
        } else {
            logger.debug("    ERROR!! db issue obtaining parameter!!!");
            return null;
        }
    }

    public Invitation getInvitiation(String coachId, String invitationId) {
        logger.debug("get invitiation getting coach: " + coachId + "  inv " + invitationId);
        String uri = environmentServices.getFirebaseURI() + "/users/" + coachId + "/invitations/" + invitationId + ".json";
        Invitation invitation = null;
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
             invitation = Invitation.fromJson(response.getBody());
             invitation.setId(invitationId);//convenience hack
        }
        logger.debug("invitation found: " );
        return invitation;
    }

    public boolean updateInvitation(String coachId, String invitationId, Invitation invitation) {
        logger.debug("updating invitation...");
        String uri = environmentServices.getFirebaseURI() + "/users/" + coachId + "/invitations/" + invitationId + ".json";
        try {
            if (callFirebaseREST(uri, HttpMethod.PUT, invitation.toJson())) {
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return false;
        }
        logger.debug("firebase updated invitation " );
        return true;
    }

    public User getUserByUID(String uid) { //this is the UID not the userId
        //logger.debug("FirebaseClientServices.getUserByUID getting user: " + uid);
        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + ".json";
        User user = null;
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            user = User.fromJson(response.getBody());
            if (user.getUid() == null)
                user.setUid(uid);
        }
        if (user != null) {
            //logger.debug("    found user: " + user.getFirstName() + " " + user.getLastName() + "  " + user.getEmail());
        } else
            logger.debug("ERROR: getUserByUID did not find user uid: " + uid );

        return user;
    }

    public JSONObject getUserByIdJSON(String userId) {
        logger.debug("getting user json: " + userId);
        String uri = environmentServices.getFirebaseURI() + "/users/" + userId + ".json";
//        User user = null;
        JSONObject json = null;
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            json = new JSONObject(response.getBody());
//            logger.debug(json);
            return json;
        }
        return null;
//        logger.debug("rest found user: " + user.getFirstName() + " " + user.getLastName() + "  " + user.getEmail());
//        return user;
    }

    public User getUserByUsername(String username) {
        logger.debug("getting user by username: " + username);
        String uri = environmentServices.getFirebaseURI() + "/users.json?orderBy=\"userId\"&equalTo=\""+username+"\"";
//        String uri = environmentServices.getFirebaseURI() + "/users.json?orderBy=userId&equalTo="+username;
        User user = null;
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            String newBody = response.getBody().substring(response.getBody().indexOf(":") + 1);
            user = User.fromJson(newBody);
        }
        if (user != null)
            logger.debug("found user: " + user.getFirstName() + " " + user.getLastName() + "  " + user.getEmail());
        else
            logger.debug("did not find user.");
        return user;
    }
/*  couldn't get this damn code to work, see NotificationServices.getNotificationSetting

    public NotificationSetting getNotificationSetting(String settingId) {
        logger.debug("getting notification setting: " + settingId);
//        String uri = environmentServices.getFirebaseURI() + "/notifications.json?orderBy=\"order\"&equalTo=\""+"2"+"\"";
//        String uri = environmentServices.getFirebaseURI() + "/notifications.json?orderByChild=\"id\"&equalTo=\""+settingId+"\"";
        String uri = environmentServices.getFirebaseURI() + "/notifications.json?orderBy=\"type\"";//?orderBy=\"id\"";//&limitToFirst=1";
        NotificationSetting notificationSetting = null;
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            String newBody = response.getBody().substring(response.getBody().indexOf(":") + 1);
            notificationSetting = NotificationSetting.fromJson(newBody);
        }
        if (notificationSetting != null)
            logger.debug("found notification setting, message: " + notificationSetting.getMessage());
        else
            logger.debug("did not find notificationSetting " + settingId);
        return notificationSetting;
    }
*/

    public boolean updateChatMessageText(String coachId, String clientId, String messageId, String text) {
//    public boolean updateChatMessage(String coachId, String clientId, String messageId, String type, String text) {
        logger.debug("updating chat message... ");
        String uri = environmentServices.getFirebaseURI() + "/chat/" + coachId + "/" + clientId
                + "/messages/" + messageId + "/text.json";

        try {
            if (callFirebaseREST(uri, HttpMethod.PUT, "\"" + text + "\"")) {
            //this gets a 400, why??? if (callFirebaseREST(uri, HttpMethod.PUT, "{'type':'" + type + "','text':'" + text + "'}")) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return false;
        }
        logger.debug("firebase updated chat message " );
        return true;
    }

    public ChatMessage getChatMessage(String coachId, String clientId, String messageId) {
        logger.debug("getting chat message... ");
        String uri = environmentServices.getFirebaseURI() + "/chat/" + coachId + "/" + clientId
                + "/messages/" + messageId + ".json";
        try {
            if (callFirebaseREST(uri, HttpMethod.GET, null)) {
//                String newBody = response.getBody().substring(response.getBody().indexOf(":") + 1);
                ChatMessage chat = ChatMessage.fromJson(response.getBody());
                return chat;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }
        return null;
    }

/*
    public String getUsers() {
        logger.debug("getting users");
        String uri = environmentServices.getFirebaseURI() + "/users.json";//?orderBy=\"lastName\"";
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            return response.getBody();
        } else
            return null;
    }
*/

    public String getNodeList(String node) {
        String uri = environmentServices.getFirebaseURI() + "/" + node + ".json";
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            return response.getBody();
        } else
            return null;
    }

    public String clearApplicationLog() {
        logger.debug("clearing the applog");
        String uri = environmentServices.getFirebaseURI() + "/application-log/20170223.json";
        if (callFirebaseREST(uri, HttpMethod.DELETE, null)) {
            return response.getBody();
        } else
            return null;
    }

    public boolean updateUserStripe(String uid, String json) {
        logger.debug("updating user with stripe info...");

        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + "/account/stripe.json";
        return updateUserChild(uid, uri, json);
    }

    public boolean updateUserStripeAccountId(String uid, String accountId) {
        logger.debug("updating user with stripe account id...");

        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + "/account/stripeAccountId.json";
        return callFirebaseREST(uri, HttpMethod.PUT,"\"" + accountId + "\"" );
//        return updateUserChild(uid, uri, accountId);
    }

    public boolean updateUserStripeExternalAccountId(String uid, String accountId) {
        logger.debug("updating user with stripe external account id...");

        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + "/account/stripeExternalAccountId.json";
        return callFirebaseREST(uri, HttpMethod.PUT,"\"" + accountId + "\"" );
//        return updateUserChild(uid, uri, accountId);
    }

    public boolean updateUserStripeCharge(String uid, String paymentId, String chargeJson) {
        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + "/payments/" + paymentId + "/stripeCharge.json";
        logger.debug("firebase updated user with stripe charge: " + chargeJson);
        return updateUserChild(uid, uri, chargeJson);
    }

    //for now this only supports a single card
    public boolean updateUserPaymentMethod(String uid, JSONObject card) {
        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + "/paymentMethods/1/" + "/card.json";
        logger.debug("firebase updated user with stripe card: " + card);
        return updateUserChild(uid, uri, card.toString());
    }

    //update the user service with qty, time, paymentId, and status
    public boolean updateUserService(String uid, String paymentId, JSONObject proposalJson) {
        String uri = environmentServices.getFirebaseURI() + "/users/" + uid + "/service.json";
        JSONObject service = new JSONObject();
        service.put("payment", paymentId);
        service.put("qty", proposalJson.getInt("qty"));
        service.put("time", proposalJson.getString("time"));
        service.put("startTime", "");
        service.put("endtime", "");
//        service.put("status", "paid");
        logger.debug("firebase updated user with paid service: " + service);
        return updateUserChild(uid, uri, service.toString());
    }

    public Proposal getProposal(String coachId, String proposalId) {
        logger.debug("get proposal getting coach: " + coachId + "  proposal " + proposalId);
        String uri = environmentServices.getFirebaseURI() + "/users/" + coachId + "/proposals/" + proposalId + ".json";
        Proposal proposal = null;
        if (callFirebaseREST(uri, HttpMethod.GET, null)) {
            proposal = Proposal.fromJson(response.getBody());
            if (proposal != null)
                proposal.setId(proposalId);//convenience hack
        }
        return proposal;
    }

    public boolean updateProposalField(String coachUid, String proposalId, String fieldName, String value) {
        String uri = environmentServices.getFirebaseURI() + "/users/" + coachUid + "/proposals/" + proposalId + "/"
                + fieldName + ".json";
        logger.debug("updating user's proposal field " + coachUid + "   field:" + fieldName + "  value:" + value);
        if (callFirebaseREST(uri, HttpMethod.PUT, "\"" + value + "\"")) {
        } else
            return false;
        return true;
    }

    private boolean updateUserChild(String userId, String uri, String jsonData) {
        //User user = null;
        //user = getUserByUID(userId);
        //if (user != null) {
            if (callFirebaseREST(uri, HttpMethod.PUT, jsonData)) {
                //user = User.fromJson(response.getBody());
            }
        //} else {
        //    error = "User not found";
        //    return false;
        //}
        return true;
    }

    public boolean updateUserStatus(String userId, String status) {
        return updateUserField(userId, "status", status);
    }

    public boolean updateUserField(String userId, String fieldName, String value) {
//        User user;
//        JSONObject json = new JSONObject();
//        json.put(fieldName, value);
        String uri = environmentServices.getFirebaseURI() + "/users/" + userId + "/" + fieldName + ".json";
        logger.debug("updating user field " + userId + "   field:" + fieldName + "  value:" + value);
        logger.debug("uri:" + uri);
            if (callFirebaseREST(uri, HttpMethod.PUT, "\"" + value + "\"")) {
                //user = User.fromJson(response.getBody());
            } else
                return false;
        logger.debug("User updated field " + fieldName + " to " + value);
        return true;
    }

    public boolean createInviteCrossIndex(Invitation invitation, User coach) {
        String key = invitation.getAccessCode();
        String value = "/users/" + coach.getUid() + "/invitations/" + invitation.getId();
        //String uri = environmentServices.getFirebaseURI() + "/invitations/" + invitation.getRandomAlphaNumeric() ;// + ".json";
        String uri = environmentServices.getFirebaseURI() + "/invitations.json";
        JSONObject json = new JSONObject();
        json.put(key, value);

        if (callFirebaseREST(uri, HttpMethod.PATCH, json.toString())) {
            //user = User.fromJson(response.getBody());
        } else
            return false;
        return true;
    }

    public boolean createLedgerItem(User user, LedgerItem ledgerItem) throws JsonProcessingException {
        String uri = environmentServices.getFirebaseURI() + "/users/" + user.getUid() + "/ledger.json";

        if (callFirebaseREST(uri, HttpMethod.POST, ledgerItem.toJson())) {
            //user = User.fromJson(response.getBody());
        } else
            return false;
        return true;
    }

    public boolean callFirebaseREST(String uri, HttpMethod httpMethod, String body) {
        //logger.debug("calling firebase uri:" + uri + " method:" + httpMethod);
        connectToDB();

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + this.token);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);

        try {
            this.response = restTemplate.exchange(uri, httpMethod, entity, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        }

        if (this.response.getStatusCode() == HttpStatus.OK) {
            if (hasAcknowlegedURL) {
                logger.debug("   successful firebase call " + response.getStatusCode() + "  uri: " + uri);
                hasAcknowlegedURL = true;
            }
            return true;
        } else {
            logger.debug("   Bad return code calling firebase code: " + response.getStatusCode() + " uri: " + uri);
            return false;
        }
    }

    //basically copy of above with result parsed into json
    public JSONObject callFirebaseRESTreturnJSON(String uri, HttpMethod httpMethod, String body) {
        logger.debug("calling firebase uri:" + uri + " method:" + httpMethod);
        connectToDB();

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + this.token);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        JSONObject result = null;

        try {
            this.response = restTemplate.exchange(uri, httpMethod, entity, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        }

        if (this.response.getStatusCode() == HttpStatus.OK) {
            logger.debug("successful firebase call " + response.getStatusCode());
            result = new JSONObject(response.getBody());
            return result;
        } else {
            logger.debug("Bad return code calling firebase code: " + response.getStatusCode());
            return null;
        }
    }


    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
