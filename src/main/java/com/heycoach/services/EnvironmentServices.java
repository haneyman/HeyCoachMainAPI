package com.heycoach.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentServices {
    private static final Logger logger = Logger.getLogger(EnvironmentServices.class);
    public static final String ENV_DEVELOPMENT = "DEVELOPMENT";
    public static final String ENV_QA = "QA";
    public static final String ENV_PRODUCTION = "PRODUCTION";

    //Stripe keys, //TODO:move to properties file
    private static String STRIPE_TEST_SECRET_KEY = "";
//    private static String STRIPE_TEST_PUBLISHABLE_KEY = "";

    private static String STRIPE_PROD_SECRET_KEY = "";
//    private static String STRIPE_TEST_PUBLISHABLE_KEY = "";

    String hc_environment;

    public EnvironmentServices() {
        super();
        hc_environment = System.getProperty("HC_ENVIRONMENT");
        logger.info("EnvironmentServices constructor hc_environment:" + hc_environment);
    }

    public boolean isProduction() {
        return hc_environment.equalsIgnoreCase(ENV_PRODUCTION);
    }

    public boolean isQA() {
        return hc_environment.equalsIgnoreCase(ENV_QA);
    }

    public boolean isDevelopment() { return hc_environment.equalsIgnoreCase(ENV_DEVELOPMENT);
    }

    public static String getStripeSecretKey(String environment) throws Exception {
        if (environment.equalsIgnoreCase(ENV_DEVELOPMENT))
            return STRIPE_TEST_SECRET_KEY;
        else if (environment.equalsIgnoreCase(ENV_QA))
            return STRIPE_TEST_SECRET_KEY;
        else if (environment.equalsIgnoreCase(ENV_PRODUCTION))
            return STRIPE_PROD_SECRET_KEY;
        else
            throw new Exception("getStripeSecretKey got Unknown HC_ENVIRONMENT setting: '" + environment + "'");
    }

    public String getFirebaseJson() throws Exception {
        //logger.debug("getFirebaseJson:" + hc_environment);
        if (isDevelopment())
            return "/firebase-adminsdk-dev.json";
        else if (isQA())
            return "/firebase-service-acct-key-qa.json";
        else if (isProduction())
            return "/firebase-service-acct-key-prod.json";
        else
            throw new Exception("getFirebaseJson got Unknown HC_ENVIRONMENT setting: '" + hc_environment + "'");
    }

    public String getFirebaseURI()  {
        if (isDevelopment())//https://coachmonsterpoc.firebaseio.com
            return "https://coachmonsterpoc.firebaseio.com";
        else if (isQA())
            return "https://heycoach-qa.firebaseio.com";
        else if (isProduction())
            return "https://heycoach-prod.firebaseio.com";
        else
            return "";
    }

    public String getHc_environment() {
        return hc_environment;
    }

    public void setHc_environment(String hc_environment) {
        this.hc_environment = hc_environment;
    }
}
