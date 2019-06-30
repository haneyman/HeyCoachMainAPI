package com.heycoach.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.heycoach.model.*;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class StripeServicesTest {
    private static final Logger logger = Logger.getLogger(StripeServicesTest.class);
    static {
        System.setProperty("HC_ENVIRONMENT", "DEVELOPMENT");
    }

    @Autowired StripeServices stripeServices;
    @Autowired EnvironmentServices environmentServices;
    @Autowired UserServices userServices;
    @Autowired FirebaseClientServices dbServices;

    @Before
    public void setup() {
//        environmentServices.setHc_environment("DEVELOPMENT");
        //environmentServices.setHc_environment("QA");
    }

    @Test
    public void testProposalPayment() {
        String user = "EMu49RNNNbQpTlbE6bihdMdXDHM2";
        String payment = "-Kv45ySTRAvbjJbkvSkS";
        stripeServices.processProposalPayment(user, payment);
    }

    @Test
    public void testAddLedger() {
        User client = getMockClient();
        //User coach = getMockCoach();
        User coach = userServices.getUserByUsername("coachtest2");
        try {
            stripeServices.addLedgerItem(coach, LedgerItem.CATEGORY_CLIENT_PAYMENT,
                    "client " + client.getFullName() + " paid " + 200 + " for services",
                    Utilities.getUTC(), " 4/6/2017, 7:55:47 PM", client.getUid(), LedgerItem.TYPE_CREDIT, 200.00F, 0F, 0F,
                    LedgerItem.SOURCE_TYPE_CLIENT, client.getUid(), LedgerItem.DESTINATION_TYPE_STRIPE_ACCT,
                    "CHARGEID", LedgerItem.REFERENCE_TYPE_PROPOSAL, "testproposalid");//id of charge???
/*
            stripeServices.addLedgerItem(coach, LedgerItem.CATEGORY_CLIENT_PAYMENT,
                    "client " + client.getFullName() + " paid " + 300 + " for services",
                    Utilities.getUTC(), " 4/6/2017, 7:55:47 PM", client.getUid(), LedgerItem.TYPE_CREDIT, new Long(300),
                    LedgerItem.SOURCE_TYPE_CLIENT, client.getUid(), LedgerItem.DESTINATION_TYPE_STRIPE_ACCT, "CHARGEID");//id of charge???
            stripeServices.addLedgerItem(coach, LedgerItem.CATEGORY_CLIENT_PAYMENT,
                    "client " + client.getFullName() + " paid " + 400 + " for services",
                    Utilities.getUTC(), " 4/6/2017, 7:55:47 PM", client.getUid(), LedgerItem.TYPE_CREDIT, new Long(400),
                    LedgerItem.SOURCE_TYPE_CLIENT, client.getUid(), LedgerItem.DESTINATION_TYPE_STRIPE_ACCT, "CHARGEID");//id of charge???

            stripeServices.addLedgerItem(coach, LedgerItem.CATEGORY_CLIENT_PAYMENT,
                    "client " + client.getFullName() + " paid " + 200 + " for services",
                    "2017-03-01T23:42:03.522Z", " 3/1/2017, 7:55:47 PM", client.getUid(), LedgerItem.TYPE_CREDIT, new Long(200),
                    LedgerItem.SOURCE_TYPE_CLIENT, client.getUid(), LedgerItem.DESTINATION_TYPE_STRIPE_ACCT, "CHARGEID");//id of charge???
            stripeServices.addLedgerItem(coach, LedgerItem.CATEGORY_CLIENT_PAYMENT,
                    "client " + client.getFullName() + " paid " + 300 + " for services",
                    "2017-03-01T23:42:03.522Z", " 3/1/2017, 7:55:47 PM", client.getUid(), LedgerItem.TYPE_CREDIT, new Long(300),
                    LedgerItem.SOURCE_TYPE_CLIENT, client.getUid(), LedgerItem.DESTINATION_TYPE_STRIPE_ACCT, "CHARGEID");//id of charge???
            stripeServices.addLedgerItem(coach, LedgerItem.CATEGORY_CLIENT_PAYMENT,
                    "client " + client.getFullName() + " paid " + 400 + " for services",
                    "2017-03-01T23:42:03.522Z", " 3/1/2017, 7:55:47 PM", client.getUid(), LedgerItem.TYPE_CREDIT, new Long(400),
                    LedgerItem.SOURCE_TYPE_CLIENT, client.getUid(), LedgerItem.DESTINATION_TYPE_STRIPE_ACCT, "CHARGEID");//id of charge???
*/
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        }

    }

    @Test public void testCreateExternalAccount() {
        String coachTest1 = "Px7MbzUng4YRRA9XMaiQCbZkPuy2";

        ServiceResultStripe result = stripeServices.createExternalAccount(getMockBankAccount(), coachTest1);
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));

        String json = Utilities.ObjectToJson(result.getStripeAccount());

        assertTrue(dbServices.updateUserStripe(coachTest1, json));

    }

    @Test public void testGetBalances() {
//        String coachTest = "Px7MbzUng4YRRA9XMaiQCbZkPuy2";//coachtest1
        String coachTest = "F2sjHURKbAZUhvZK4sHeYQuR87Y2";//coachtest3
        ServiceResultStripe result = stripeServices.getBalances(coachTest);
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));
        logger.debug("result" + result.getJson());
    }

    @Test public void testGetBank() {
        String coachTest1 = "Px7MbzUng4YRRA9XMaiQCbZkPuy2";
        ServiceResultStripe result = stripeServices.getBank(coachTest1);
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));
        logger.debug("json:" + result.getJsonString());
        String newJson = "{status:ok, " + result.getJsonString() + "}";
        logger.debug("newJson:" + newJson);

    }

    @Test public void testGetBankNone() {
        String coachTest1 = "FC5aEMLS7qP3viLm83Ng3S7mUh52";
        ServiceResultStripe result = stripeServices.getBank(coachTest1);
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));
        logger.debug("json:" + result.getJsonString());
        String newJson = "{status:ok, " + result.getJsonString() + "}";
        logger.debug("newJson:" + newJson);

    }

    @Test public void testPayout() {
        String coachTest1 = "Px7MbzUng4YRRA9XMaiQCbZkPuy2";
        ServiceResultStripe result = stripeServices.payout(coachTest1, 1.00F, "USD");
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));
        logger.debug("json:" + result.getJsonString());
        String newJson = "{status:ok,stripePayout: " + result.getJsonString() + "}";
        logger.debug("newJson:" + newJson);

    }
/*
    @Test public void testUpdateExternalAccount() {
        String coachTest1 = "Px7MbzUng4YRRA9XMaiQCbZkPuy2";

        ServiceResultStripe result = stripeServices.updateActiveExternalAccount(getMockBankAccount(), coachTest1);
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));
    }
*/


    public StripeExternalAccount getMockBankAccount() {
        StripeExternalAccount bankAccount = new StripeExternalAccount();
        // https://stripe.com/docs/testing#managed-accounts
        bankAccount.setAccountNumber("000123456789");//stripe provided test account
        bankAccount.setAccountHolderType("individual");
        bankAccount.setCountry("US");
        bankAccount.setCurrency("usd");
        bankAccount.setRoutingNumber("110000000");
        bankAccount.setDefaultForCurrency(true);
        return bankAccount;
    }

    public User getMockClient() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Client");
        user.setUid("SJfZswTkc4hGuAR2fmS447iwg2i2");
        return user;
    }

    public User getMockCoach() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Coach");
        user.setUid("fW3g3XzGjIckam9yx0pYOagLWnu1addclose");
        return user;
    }

    //https://stripe.com/docs/connect/testing-verification
    @Test
    public void testCreateManagedAccount() {
        //stripeServices = new StripeServices();
        StripeAccount stripeAccount = new StripeAccount();
        stripeAccount.setCountry("US");
        stripeAccount.setIpAddress("73.92.238.206");
        stripeAccount.setDobDay(23);
        stripeAccount.setDobMonth(5);
        stripeAccount.setDobYear(1993);
        stripeAccount.setFirstName("Client3");
        stripeAccount.setLastName("LastName3");
        stripeAccount.setLegalEntityType("individual");
        stripeAccount.setSsnLast4("1234");
        stripeAccount.setAddressLine1("111 Main St");
        stripeAccount.setAddressCity("San Francisco");
        stripeAccount.setAddressState("CA");
        stripeAccount.setAddressPostal("94101");
        String coachId = "test_api_" + Utilities.getRandomAlphaNumeric(8);
        ServiceResultStripe result = stripeServices.createManagedAccount(coachId, stripeAccount);
        logger.debug(result.getJsonString());
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));

    }

    @Test
    public void testUpdateManagedAccount() {
        //stripeServices = new StripeServices();
        StripeAccount stripeAccount = new StripeAccount();
        stripeAccount.setCountry("US");
        stripeAccount.setDobDay(25);
        stripeAccount.setDobMonth(5);
        stripeAccount.setDobYear(1963);
        stripeAccount.setFirstName("Client3New");
        //stripeAccount.setLastName("LastName3");
        //stripeAccount.setLegalEntityType("individual");
        stripeAccount.setSsnLast4("4567");
        //stripeAccount.setAddressLine1("222 Main St");
        //stripeAccount.setAddressCity("San Francisco");
        //stripeAccount.setAddressState("CA");
        //stripeAccount.setAddressPostal("94101");
        String coachId = "test_api_1OWMLCDY";
        ServiceResultStripe result = stripeServices.updateManagedAccount(coachId, stripeAccount);
        logger.debug(result.getJsonString());
        assertTrue(!result.getResult().equalsIgnoreCase("ERROR"));

    }

/*
    @Test
    public void updateManagedAccountBankAccount() {
        stripeServices = new StripeServices();
        Map<String, Object> accountParams = new HashMap<String, Object>();
        Map<String, Object> externalAccountParams = new HashMap<String, Object>();
        externalAccountParams.put("object", "bank_account");
        externalAccountParams.put("country", "US");
        externalAccountParams.put("currency", "usd");
        externalAccountParams.put("routing_number", "110000000");
        externalAccountParams.put("account_number", "000123456789");
        accountParams.put("external_account", externalAccountParams);


        ServiceResultStripe serviceResultStripe = stripeServices.createManagedAccount(accountParams);
        Account account = serviceResultStripe.getStripeAccount();
        assertTrue(account != null);

    }
*/



}
