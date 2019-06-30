package com.heycoach.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heycoach.model.*;
import com.stripe.Stripe;
import com.stripe.model.Event;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.heycoach.services.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;


@PropertySource("classpath:config.properties")
@EnableWebMvc
@RestController
//@Controller
@CrossOrigin()
public class MainController {
    private static final Logger logger = Logger.getLogger(MainController.class);

    @Autowired
    StripeServices stripeServices;

    @Autowired
    FirebaseClientServices dbServices;

    @Autowired UserServices userServices;

    @Autowired
    NotificationServices notificationServices;

    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired AnalyticServices analyticServices;

    public static final String version = "V1.0.52 9/30/17";

    @Autowired private Environment environment;

    String hc_environment = "???";

    public MainController() throws Exception {
        super();
        hc_environment = System.getProperty("HC_ENVIRONMENT");
        logger.info("******** HC_ENVIRONMENT: " + hc_environment +  " **********");
        ErrorService.environment = hc_environment;//set the rollbar static env
        Stripe.apiKey = EnvironmentServices.getStripeSecretKey(hc_environment);
    }

    @RequestMapping("/statusPage")
    public ModelAndView statusBody() {
        logger.debug("in MainController statusBody");
        String message = "<br><div style='text-align:center;'>"
                + "<h3>********** Welcome</h3>This message is coming from API MainController **********</div><br><br>";
        return new ModelAndView("status", "message", message);
    }

    @RequestMapping("/testDB")
    public @ResponseBody String testDB() {
        logger.debug("in MainController testDB");
        String text = firebaseClientServices.getParameter("1", "smsBody");
        if (text != null) {
            return "{\"status\": \"ok\", \"text:\": \"" + text + "\"}";
        } else {
            return "{\"status\": \"error\"}";

        }
    }

    @RequestMapping("/status")
    public @ResponseBody String status(HttpServletRequest request) {
        //response.addHeader("Access-Control-Allow-Origin", "*");
        //        logger.debug("in status of maincontroller");
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/YYYY HH:mm:ss.SSS");
        Date date = new Date();
        String prettyDate = simpleDate.format(date);
        String message = "<br><div style='text-align:center;'>"
                + "<h3>********** Welcome</h3>This message is coming from API MainController **********</div><br><br>";
        String host = request.getRemoteAddr();
        String ip =  request.getHeader("X-FORWARDED-FOR");
        String locale = request.getLocale().toString();
        String server = request.getServerName();
        //String hc_environment = environment.getProperty("HC_ENVIRONMENT");
        return "{\"status\": \"ok\", \"version\": \"" + version + "\""
                + ", \"timestamp\": \"" + prettyDate +"\" "
                + ", \"host\": \"" + host + "\""
                + ", \"fwd-ip\": \"" + ip + "\""
                + ", \"locale\": \"" + locale + "\""
                + ", \"server\": \"" + server + "\""
                + ", \"HC_ENVIRONMENT\": \"" + hc_environment + "\""
                + "}";
    }
//    let url = this.apiConf.apiURL + "/V1/event/" + this.globals.currentUser.uid + "/" + type + "/" + id;
    @RequestMapping("/V1/event/{eventType}/{senderUid}/{recipientUid}/{data}")
    public @ResponseBody String processEvent( @PathVariable("eventType") String eventType,
                                              @PathVariable("senderUid") String senderUid,
                                              @PathVariable("recipientUid") String recipientUid,
                                              @PathVariable("data") String data) {
        logger.debug("proessing event from user " + senderUid + " to user " + recipientUid + ", event type: " + eventType + ", data: " + data);
        //these will be event types from EventDispatcher.ts NOT notification types
        notificationServices.processEvent(senderUid, recipientUid, eventType, data);
        return "{\"status\": \"ok\"}";
    }

    @RequestMapping("/V1/snapshot")
    public @ResponseBody String getSnapshot() {
        logger.debug("getting snapshot... ");
        AnalyticsSnapshot analyticsSnapshot = analyticServices.gatherSnapshot();
        try {
            return analyticsSnapshot.toJson();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"status\": \"error\"}";
        }
//        return "{\"status\": \"ok\"}";
    }


    @RequestMapping("/V1/testEmail")
    public @ResponseBody String testEmail() {
        String invitationId = "-KuvbvoeXGhkb_eT0I5h";
        String coach = "g0bGQkFiNQQO5umIBQuj3vsox1m2";
        boolean result = notificationServices.processInvitation(coach, invitationId, false);
        if (result)
            return "{\"status\": \"email sent\"}";
        else
            return "{\"status\": \"problem\"}";
    }

    public @ResponseBody String testEmail(@PathVariable("invitationId") String invitationId, @PathVariable("coach") String coach) {
        boolean result = notificationServices.processInvitation(coach, invitationId, false);
        if (result)
            return "{\"status\": \"email sent\"}";
        else
            return "{\"status\": \"problem\"}";
    }

    @RequestMapping("/invitation/process/{coachId}/{invitationId}")
    public @ResponseBody String processInvitation(@PathVariable("coachId") String coachId, @PathVariable("invitationId") String invitationId) {
        logger.debug("proessing invitation for coach " + coachId + ", invitation " + invitationId);
        notificationServices.processInvitation(coachId, invitationId, false);
        return "{\"status\": \"ok\"}";
    }

    @RequestMapping("/V1/invitation/remind/{coachId}/{invitationId}")
    public @ResponseBody String processInvitationReminder(@PathVariable("coachId") String coachId, @PathVariable("invitationId") String invitationId) {
        logger.debug("proessing invitation reminder for coach " + coachId + ", invitation " + invitationId);
        notificationServices.processInvitation(coachId, invitationId, true);
        return "{\"status\": \"ok\"}";
    }

    @RequestMapping("/V1/payment/process/{userId}/{paymentId}/{messageId}")
    public @ResponseBody String processPaymentPrev(@PathVariable("userId") String userId,
                                               @PathVariable("messageId") String chatMessageId,
                                               @PathVariable("paymentId") String paymentId ) {
        //process payment - get user, payment, get proposal, get stripToken.id, create Stripe charge
        logger.debug("processpayment for user " + userId + "  payment id:" + paymentId + " message id:" + chatMessageId);
        ServiceResult result = stripeServices.processProposalPayment(userId, paymentId);
        if (result.getResult().equalsIgnoreCase("OK"))
            return "{\"status\": \"ok\"}";
        else {
            logger.debug(result.getMessage());
            return "{\"status\": \"error\", \"message\":\"" + result.getResponseToClient() + "\" }";
        }
    }

    @RequestMapping("/V1/payment/process/{userId}/{paymentId}")
    public @ResponseBody String processPaymentNew(@PathVariable("userId") String userId,
                                               @PathVariable("paymentId") String paymentId ) {
        //process payment - get user, payment, get proposal, get stripToken.id, create Stripe charge
        logger.debug("processpayment for user " + userId + "  payment id:" + paymentId);
        ServiceResult result = stripeServices.processProposalPayment(userId, paymentId);
        if (result.getResult().equalsIgnoreCase("OK"))
            return "{\"status\": \"ok\"}";
        else {
            logger.debug(result.getMessage());
            return "{\"status\": \"error\", \"message\":\"" + result.getResponseToClient() + "\" }";
        }
    }

    @RequestMapping("/getEmail/username/{userName:.*}")
    public @ResponseBody String getEmailForUserName(@PathVariable("userName") String userName) {
        logger.debug("in getEmailForUserName");
        String email = userServices.getEmailforUsername(userName);
        if (email != null) {
            return "{\"status\": \"ok\", \"email\":\"" + email + "\"}";
        } else {
            return "{\"status\": \"username not found\"}";
        }
    }

    @RequestMapping(value = "/V1/stripe/createAccount/{uid}", method = RequestMethod.POST,consumes="application/json",headers = "content-type=application/json")
    public @ResponseBody String createStripeAccount(HttpServletRequest request, @PathVariable("uid") String uid, @RequestBody StripeAccount stripeAccountData) {
        if (uid == null)
            return "{\"status\": \"error userid is null\"}";
        if (stripeAccountData == null)
            return "{\"status\": \"error account data is null\"}";
        stripeAccountData.setIpAddress(request.getRemoteAddr());
        ServiceResultStripe stripeResult = stripeServices.createManagedAccount(uid, stripeAccountData);
        if (stripeResult.getResult().equals("OK")) {
            return "{\"status\": \"ok\", " + stripeResult.getJsonString() + "}";
        } else {
            return "{\"status\": \"error creating stripe account \", \"message\": \"" + stripeResult.getMessage() + "\"}";
        }
    }

    @RequestMapping(value = "/V1/stripe/updateAccount/{uid}", method = RequestMethod.POST,consumes="application/json",headers = "content-type=application/json")
    public @ResponseBody String updateStripeAccount(HttpServletRequest request, @PathVariable("uid") String uid, @RequestBody StripeAccount stripeAccountData) {
        if (uid == null)
            return "{\"status\": \"error userid is null\"}";
        if (stripeAccountData == null)
            return "{\"status\": \"error account data is null\"}";
        stripeAccountData.setIpAddress(request.getRemoteAddr());
        ServiceResultStripe stripeResult = stripeServices.updateManagedAccount(uid, stripeAccountData);
        if (stripeResult.getResult().equals("OK")) {
            return "{\"status\": \"ok\", " + stripeResult.getJsonString() + "}";
        } else {
            return "{\"status\": \"error creating stripe account \", \"message\": \"" + stripeResult.getMessage() + "\"}";
        }
    }

    @RequestMapping(value = "/V1/stripe/getBalances/{uid}", method = RequestMethod.GET)
    public @ResponseBody String getStripeBalances(@PathVariable("uid") String uid) {
        if (uid == null)
            return "{\"status\": \"error userid is null\"}";

        ServiceResultStripe stripeResult = stripeServices.getBalances(uid);
        if (stripeResult.getResult().equals("OK") ) {
            return stripeResult.getJson().put("status", "OK").toString();
        } else {
            return "{\"status\": \"error getting stripe balances \", \"message\": \"" + stripeResult.getMessage() + "\"}";
        }
    }


    @RequestMapping(value = "/V1/stripe/getBank/{uid}", method = RequestMethod.GET)
    public @ResponseBody String getStripeBank(@PathVariable("uid") String uid) {
        if (uid == null)
            return "{\"status\": \"error userid is null\"}";

        ServiceResultStripe stripeResult = stripeServices.getBank(uid);
        if (stripeResult.getResult().equals("OK") ) {
            String newJson = "{\"status\":\"ok\", " + stripeResult.getJsonString() + "}";
            return newJson;
        } else {
            return "{\"status\": \"error getting stripe bank \", \"message\": \"" + stripeResult.getMessage() + "\"}";
        }
    }

    @RequestMapping(value = "/V1/stripe/payout/{uid}/{currency}/{amount}", method = RequestMethod.GET)
    public @ResponseBody String payout(@PathVariable("uid")         String uid,
                                       @PathVariable("currency")    String currency,
                                       @PathVariable("amount")      String amount) {
        if (uid == null)
            return "{\"status\": \"error userid is null\"}";
        Float amountF = new Float(amount);
        ServiceResultStripe stripeResult = stripeServices.payout(uid, amountF, currency);
        if (stripeResult.getResult().equals("OK") ) {
            String newJson = "{\"status\":\"ok\", " + stripeResult.getJsonString() + "}";
            return newJson;
        } else {
            return "{\"status\": \"error during payout \", \"message\": \"" + stripeResult.getMessage() + "\"}";
        }
    }

    @RequestMapping(value = "/V1/stripe/createExternalAccount/{uid}", method = RequestMethod.POST,consumes="application/json",headers = "content-type=application/json")
    public @ResponseBody String createStripeExternalAccount(@PathVariable("uid") String uid, @RequestBody StripeExternalAccount bankAccount) {
        if (uid == null)
            return "{\"status\": \"error accountToken is null\"}";
        ServiceResultStripe stripeResult = stripeServices.createExternalAccount(bankAccount,uid);
        if (stripeResult.getResult().equals("OK")) {
            return "{\"status\": \"ok\"}";
        } else {
            return "{\"status\": \"error updating db\"}";
        }
/*  use this if you want the stripe data
        if (stripeResult.getResult().equals("OK") && stripeResult.getStripeAccount() != null) {
            String json = Utilities.ObjectToJson(stripeResult.getStripeAccount());
            if (dbServices.updateUserStripe(uid, json)) {
                return "{\"status\": \"ok\"}";
            } else {
                return "{\"status\": \"error updating db\"}";
            }
        } else {
            return "{\"status\": \"error creating stripe external account \", \"message\": \"" + stripeResult.getMessage() + "\"}";
        }
*/
    }

    //https://dashboard.stripe.com/account/webhooks
    @RequestMapping("/stripe/webhook")
    @ResponseBody
    public String stripeWebHook(Event stripeEvent) {
        logger.debug("MainController.stripeWebHook has been called!!");
        stripeServices.processWebHook(stripeEvent);
        return "";
    }

/*    //https://dashboard.stripe.com/account/webhooks
    @RequestMapping("/getUserByUID/{userId}")
    @ResponseBody
    public String getUserByUID(@PathVariable String userId) {
        logger.debug("MainController.getUserByUID " + userId);
        //dbServices.getUserByUID(userId);
        return "return from getUserByUID";
    }*/

    @RequestMapping("/V1/users/list")
    @ResponseBody
    public String getUsers() {
        logger.debug("MainController.getUsers");
        ServiceResult result = userServices.getUsers();
        if (result.getResult().equalsIgnoreCase("OK"))
            return result.getResponseToClient();
        else {
            logger.debug(result.getMessage());
            return "{\"status\": \"error\", \"message\":\"" + result.getResponseToClient() + "\" }";
        }
    }

    @RequestMapping("/V1/users/delete/{uid}")
    @ResponseBody
    public String deleteUser(@PathVariable String userId) {
        logger.debug("MainController.deleteUser");
        ServiceResult result = userServices.deleteUser(userId);
        if (result.getResult().equalsIgnoreCase("OK"))
            return result.getResponseToClient();
        else {
            logger.debug(result.getMessage());
            return "{\"status\": \"error\", \"message\":\"" + result.getResponseToClient() + "\" }";
        }
    }

    @RequestMapping("/V1/applog/list")
    @ResponseBody
    public String getAppLogs() {
        logger.debug("MainController.getAppLogs");
        String results = firebaseClientServices.getNodeList("application-log");
        logger.debug("Size of applog: " + results.length());
        return results;
    }

    @RequestMapping("/V1/applog/clear")
    @ResponseBody
    public String clearAppLog() {
        logger.debug("MainController.clearAppLog");
        String results = firebaseClientServices.clearApplicationLog();
        logger.debug("Size of applog: " + results.length());
        return results;
    }

    @RequestMapping(value = "errors", method = RequestMethod.GET)
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {

        ModelAndView errorPage = new ModelAndView("errors");
        String errorMsg = "";
        int httpErrorCode = getErrorCode(httpRequest);

        switch (httpErrorCode) {
            case 400: {
                errorMsg = "Http Error Code: 400. Bad Request";
                break;
            }
            case 401: {
                errorMsg = "Http Error Code: 401. Unauthorized";
                break;
            }
            case 404: {
                errorMsg = "Http Error Code: 404. Resource not found";
                break;
            }
            case 500: {
                errorMsg = "Http Error Code: 500. Internal Server Error";
                break;
            }
        }
        errorPage.addObject("errorMsg", errorMsg);
        return errorPage;
    }

    private int getErrorCode(HttpServletRequest httpRequest) {
        return (Integer) httpRequest
                .getAttribute("javax.servlet.error.status_code");
    }



/*
    @RequestMapping("/login")
    public ModelAndView login() {
        logger.debug("testFirebase...");
        String message = "<br><div style='text-align:center;'>"
                + "<h3>********** Hello World</h3>This message is coming from API MainController **********</div><br><br>";
        return new ModelAndView("login", "message", message);
    }

    @RequestMapping("/chat")
    public ModelAndView chat() {
        logger.debug("chat...");
        return new ModelAndView("chat");
    }
*/

}
