package com.heycoach.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heycoach.model.*;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.*;
import com.stripe.model.Account;
import com.stripe.net.RequestOptions;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mark on 12/17/2016.
 *
 * Create a coach managed account
 * App creates a CC token - "payment"
 * API creates a customer with the "source" which is the above CC payment token
 * API creates charges referring to the customer id and source
 *
 *
 *
 */
@Service
public class StripeServices {
    private static final Logger logger = Logger.getLogger(StripeServices.class);

    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired UserServices userServices;
    @Autowired ChatServices chatServices;
    @Autowired EnvironmentServices environmentServices;

    private static final String STATEMENT_DESCRIPTOR = "Heycoach";

    public StripeServices() throws Exception {
        //doesn't work here Stripe.apiKey = environmentServices.getStripeSecretKey();//TODO: should be property
    }

    /**
     * Creates a managed account for the Coaches to receive money
     */
    public ServiceResultStripe createManagedAccount(String uid, StripeAccount stripeAccountData) {
        Map<String, Object> stripeAccountParameters = createAccountParameters(stripeAccountData, true);

        Account account = null;
        logger.debug("creating Stripe account...");
        String result = "";
        String message = "";
        String responseToClient = "";
        String jsonValue = "\"none\"";
        try {
            account = Account.create(stripeAccountParameters);
            logger.debug("created Stripe account");
            message = "Account created.";
            if (account != null) {
                ObjectMapper mapper = new ObjectMapper();
                jsonValue = mapper.writeValueAsString(account);
                result = "OK";
            } else {
                result = "ERROR";
            }
        } catch (Exception e) {
            logger.debug("exception creating Stripe managed account");
            e.printStackTrace();
            ErrorService.reportError(e);
            result = "ERROR";
            message = "Authentication exception: " + e.getMessage();
        }
        firebaseClientServices.updateUserStripeAccountId(uid, account.getId());
        ServiceResultStripe serviceResultStripe = new ServiceResultStripe(result, message, responseToClient,
                "\"account\": " + jsonValue);
        return serviceResultStripe;
        //TODO: What to do if error/exception?
    }

    /**
     * Updates a managed account for the Coaches to receive money
     */
    public ServiceResultStripe updateManagedAccount(String uid, StripeAccount stripeAccountData) {
        String result = "";
        String message = "";
        String responseToClient = "";
        String jsonValue = "\"none\"";
        Account curAccount = null;
        User user = userServices.getUserByUID(uid);
        if (user == null) {
            logger.debug("exception retrieving Stripe managed account, user not found:" + uid);
            result = "ERROR";
            message = "User not found " + uid;
        } else {
            String acctId = user.getAccount().getStripeAccountId();
            logger.debug("   updating stripe account id " + acctId + "...");
            //retrieve the account
            try {
                curAccount = Account.retrieve(acctId, null);
            } catch (Exception e) {
                logger.debug("exception retrieving Stripe managed account");
                e.printStackTrace();
                ErrorService.reportError(e);
                result = "ERROR";
                message = "Authentication exception: " + e.getMessage();
            }

            if (curAccount == null) {
                logger.debug("exception retrieving Stripe managed account, account not found for user " + uid + "  stripe:" + user.getAccount().getStripeAccountId());
                result = "ERROR";
                message = "Stripe account not found for user " + uid + " account " + user.getAccount().getStripeAccountId();
            } else {
                Map<String, Object> stripeAccountParameters = createAccountParameters(stripeAccountData, false);

                try {
                    Account updatedAccount = curAccount.update(stripeAccountParameters);
                    //account = Account.create(stripeAccountParameters);
                    logger.debug("updated Stripe account");
                    message = "Account updated.";
                    if (updatedAccount != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        jsonValue = mapper.writeValueAsString(updatedAccount);
                        result = "OK";
                    } else {
                        result = "ERROR";
                    }
                } catch (Exception e) {
                    logger.debug("exception updating Stripe managed account");
                    e.printStackTrace();
                    ErrorService.reportError(e);
                    result = "ERROR";
                    message = "Exceptionn updating account: " + e.getMessage();
                }
            }
        }
        //firebaseClientServices.updateUserStripeAccountId(uid, account.getId());
        ServiceResultStripe serviceResultStripe = new ServiceResultStripe(result, message, responseToClient,
                "\"account\": " + jsonValue);
        return serviceResultStripe;
        //TODO: What to do if error/exception?
    }

    /**
     * Takes in an Account object and loads up a Map that Stripe API requires
     * used by both update and create situations
     * only adds parameters for those that have a value (to support updates)
     * @param account
     * @return
     */
    public Map<String, Object> createAccountParameters(StripeAccount account, boolean isCreate) {
        Map<String, Object> accountParams = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(account.getCountry()))
            accountParams.put("country", account.getCountry());
        if (!StringUtils.isEmpty(account.getEmail()))
            accountParams.put("email", account.getEmail());
        if (isCreate) {
            accountParams.put("type", "custom");
            accountParams.put("payout_schedule[interval]", "manual");
            accountParams.put("product_description", "Heycoach client");
            accountParams.put("support_email", "support@heycoach.me");
        }
        //accountParams.put("timezone", "America/Los_Angeles");  stripe no likey

        if (isCreate) {
            Map<String, Object> tosParams = new HashMap<String, Object>();
            tosParams.put("date", new Date().getTime() / 1000);
            tosParams.put("ip", account.getIpAddress());
            accountParams.put("tos_acceptance", tosParams);
        }

        Map<String, Object> accountParamsDob = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(account.getDobDay()))
            accountParamsDob.put("day", account.getDobDay());
        if (!StringUtils.isEmpty(account.getDobMonth()))
            accountParamsDob.put("month", account.getDobMonth());
        if (!StringUtils.isEmpty(account.getDobYear()))
            accountParamsDob.put("year", account.getDobYear());

        Map<String, Object> accountParamsLegalEntity = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(account.getFirstName()))
            accountParamsLegalEntity.put("first_name", account.getFirstName());
        if (!StringUtils.isEmpty(account.getLastName()))
            accountParamsLegalEntity.put("last_name", account.getLastName());
        if (!StringUtils.isEmpty(account.getLegalEntityType()))
            accountParamsLegalEntity.put("type", account.getLegalEntityType());//individual
        if (accountParamsDob.size() > 0)
            accountParamsLegalEntity.put("dob", accountParamsDob);
        if (account.getSsnLast4() != null && account.getSsnLast4().length() > 0)
            accountParamsLegalEntity.put("ssn_last_4", account.getSsnLast4());
        if (account.getTaxId() != null && account.getTaxId().length() > 0)
            accountParamsLegalEntity.put("business_tax_id", account.getTaxId());
        if (account.getBusinessName() != null && account.getBusinessName().length() > 0)
            accountParamsLegalEntity.put("business_name", account.getBusinessName());

        Map<String, Object> accountParamsLegalEntityAddress = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(account.getAddressLine1()))
            accountParamsLegalEntityAddress.put("line1", account.getAddressLine1());
        if (!StringUtils.isEmpty(account.getAddressCity()))
            accountParamsLegalEntityAddress.put("city", account.getAddressCity());
        if (!StringUtils.isEmpty(account.getAddressState()))
            accountParamsLegalEntityAddress.put("state", account.getAddressState());
        if (!StringUtils.isEmpty(account.getAddressPostal()))
            accountParamsLegalEntityAddress.put("postal_code", account.getAddressPostal());
        if (accountParamsLegalEntityAddress.size() > 0)
            accountParamsLegalEntity.put("address", accountParamsLegalEntityAddress);

        if (accountParamsLegalEntity.size() > 0)
            accountParams.put("legal_entity", accountParamsLegalEntity);
        if (account.getBusinessName() != null && account.getBusinessName().length() > 0)
            accountParams.put("business_name", account.getBusinessName());

        return accountParams;
    }

    public ServiceResultStripe createExternalAccount(StripeExternalAccount bankAccount, String userId) {
        logger.debug("Creating Stripe External account for user" + userId );
        Account account = getManagedAccountByUID(userId);
        Map<String, Object> externalAccountParameters = createExternalAccountParameters(bankAccount);
        String result = "";
        String message = "";
        String responseToClient = "";
        String jsonValue = "\"none\"";
        if (account == null) {
            message = "createExternalAccount could not retrieve Stripe account for user " + userId;
            result = "ERROR";
            logger.debug(message);
            responseToClient = "There was an issue creating this bank account.  Contact support or try again.";
        } else {
            try {
                ExternalAccount externalAccount = account.getExternalAccounts().create(externalAccountParameters);
                firebaseClientServices.updateUserStripeExternalAccountId(userId, externalAccount.getId());
                result = "OK";
                //account = getManagedAccountByUID(userId);
            } catch (Exception e) {
                logger.debug("exception creating Stripe external account");
                e.printStackTrace();
                ErrorService.reportError(e);
                result = "ERROR";
                message = "API Exception:" + e.getClass() + " " + e.getMessage();
                responseToClient = "There was an issue creating this bank account.  Contact support or try again.";
            }
        }
        ServiceResultStripe serviceResultStripe = new ServiceResultStripe(result, message, responseToClient, account);
        return serviceResultStripe;
    }

    public ServiceResultStripe payout(String userId, Float amount, String currency) {
        logger.debug("Creating Stripe payout for user" + userId );
        User user = userServices.getUserByUID(userId);
        Account account = getManagedAccountByUser(user);

        Map<String, Object> payoutParams = new HashMap<String, Object>();
        Integer amountI = Math.round(amount * 100);
        payoutParams.put("amount", amountI);//amount is in pennies
        payoutParams.put("currency", currency);
        payoutParams.put("statement_descriptor", "Heycoach Earnings");
        RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(account.getId()).build();

        String result = "";
        String message = "";
        String responseToClient = "";
        String jsonValue = "\"none\"";
        try {
            logger.debug("Creating a Stripe payout for user " + userId + " amount:" + amountI + " currency:" + currency);
            Payout payout = Payout.create(payoutParams, requestOptions);
            //Balance balance = Balance.retrieve(RequestOptions.builder().setStripeAccount(account.getId()).build());

            if (payout != null && payout.getFailureCode() == null) {
                result = "OK";
                //finally add a ledger item
                try {
                    addLedgerItem(user, LedgerItem.CATEGORY_PAYOUT,
                            "caoch " + user.getFullName() + " created payout of $" + amount,
                            Utilities.getUTC(), Utilities.getPrettyDate(), user.getUid(), LedgerItem.TYPE_DEBIT, amount, 0F, 0F,
                            LedgerItem.SOURCE_TYPE_STRIPE_ACCOUNT, payout.getSourceType(),
                            LedgerItem.DESTINATION_TYPE_BANK_ACCT, payout.getDestination(),
                            LedgerItem.REFERENCE_TYPE_PAYOUT, payout.getId());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    ErrorService.reportError(e);
                }
            } else { //there was an issue with the payout
                result = "ERROR";
            }
            ObjectMapper mapper = new ObjectMapper();
            jsonValue = mapper.writeValueAsString(payout);
        } catch (Exception e) {
            logger.debug("exception creating payout");
            e.printStackTrace();
            ErrorService.reportError(e);
            result = "ERROR";
            message = "API Exception:" + e.getClass() + " " + e.getMessage();
            responseToClient = "There was an issue creating this transfer.  Contact support or try again.";
        }
        ServiceResultStripe serviceResultStripe = new ServiceResultStripe(result, message, responseToClient,
                "\"stripePayout\": " + jsonValue);
        return serviceResultStripe;
    }

    public ServiceResultStripe getBank(String uid) {
        logger.debug("Getting Stripe External account for user" + uid );
        String result = "";
        String message = "";
        String responseToClient = "";
        //JSONObject json = new JSONObject();
        Account account = getManagedAccountByUID(uid);//TODO: this could be optimized by passing in stripe acct id
        String jsonValue = "\"none\"";
        if (account == null) {
            result = "OK";
        } else {
            BankAccount defaultBankAccount = null;
            try {
                ExternalAccountCollection externalAccounts = account.getExternalAccounts();
                for (ExternalAccount externalAccount : externalAccounts.getData()) {
                    BankAccount bankAccount = (BankAccount) externalAccount;
                    if (bankAccount.getDefaultForCurrency()) {
                        defaultBankAccount = bankAccount;
                        break;
                    }
                }
                if (defaultBankAccount != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    jsonValue = mapper.writeValueAsString(defaultBankAccount);
                }
                result = "OK";
            } catch (Exception e) {
                logger.debug("exception getting the bank");
                e.printStackTrace();
                ErrorService.reportError(e);
                result = "ERROR";
                message = "API Exception:" + e.getClass() + " " + e.getMessage();
                responseToClient = "There was an issue retrieving the bank.  Contact support or try again.";
            }
        }
        ServiceResultStripe serviceResultStripe = new ServiceResultStripe(result, message, responseToClient,
                "\"stripeBank\": " + jsonValue);
        return serviceResultStripe;
    }

    public ServiceResultStripe getBalances(String uid) {
//        logger.debug("Getting Stripe balances for user" + uid );

        Account account = getManagedAccountByUID(uid);//TODO: this could be optimized by passing in stripe acct id
        String result = "";
        String message = "";
        String responseToClient = "";
        JSONObject json = new JSONObject();
        if (account == null) {
            json.put("available", 0);
            json.put("pending", 0);
            result = "OK";
        } else {
            try {
                Balance balance = Balance.retrieve(RequestOptions.builder().setStripeAccount(account.getId()).build());
                Long available = 0L;
                Long pending = 0L;
                for (Money money : balance.getAvailable()) {
                    available += money.getAmount();
                }
                for (Money money : balance.getPending()) {
                    pending += money.getAmount();
                }
                json.put("available", available/100);
                json.put("pending", pending/100);
                result = "OK";
            } catch (Exception e) {
                logger.debug("exception creating Stripe external account");
                e.printStackTrace();
                ErrorService.reportError(e);
                result = "ERROR";
                message = "API Exception:" + e.getClass() + " " + e.getMessage();
                responseToClient = "There was an issue retrieving the balance.  Contact support or try again.";
            }
        }
        ServiceResultStripe serviceResultStripe = new ServiceResultStripe(result, message, responseToClient, json);
        return serviceResultStripe;
    }


/*
    public ServiceResultStripe updateActiveExternalAccount(BankAccount inBankAccount, String userId) {
        String result = "";
        String message = "";
        String responseToClient = "";

        Account managedAccount = getManagedAccountByUID(userId);
        Map<String, Object> bankAccountParams = new HashMap<String, Object>();
        bankAccountParams.put("limit", 30);
        bankAccountParams.put("object", "bank_account");
        ExternalAccountCollection externalAccounts = null;
        try {
            externalAccounts = managedAccount.getExternalAccounts().all(bankAccountParams);
            for (ExternalAccount externalAccount : externalAccounts.getData()) {
                BankAccount bankAccount = (BankAccount) externalAccount;
                if (bankAccount.getDefaultForCurrency()) { //if this is the default account, update it

                }
            }
        } catch (Exception e) {
            logger.debug("exception getting list of Stripe external accounts");
            e.printStackTrace();
            RollbarService.reportError(e);
            result = "ERROR";
            message = "API Exception:" + e.getClass() + " " + e.getMessage();
            responseToClient = "There was an issue creating this bank account.  Contact support or try again.";
        }


        ServiceResultStripe serviceResultStripe = new ServiceResultStripe(result, message, responseToClient, null);
        return serviceResultStripe;
    }
*/

    public Map<String, Object> createExternalAccountParameters(StripeExternalAccount accountData) {
        Map<String, Object> accountParams = new HashMap<String, Object>();
        Map<String, Object> externalAccountParams = new HashMap<String, Object>();
        externalAccountParams.put("object", "bank_account");
        externalAccountParams.put("account_holder_name", accountData.getAccountHolderName());
        externalAccountParams.put("account_holder_type", accountData.getAccountHolderType());//"individual" or "company"
//        externalAccountParams.put("bank_name", accountData.getBankName());
        externalAccountParams.put("country", accountData.getCountry());
        externalAccountParams.put("currency", accountData.getCurrency());
        externalAccountParams.put("default_for_currency", accountData.getDefaultForCurrency());//default if 1st acct
        externalAccountParams.put("routing_number", accountData.getRoutingNumber());
        externalAccountParams.put("account_number", accountData.getAccountNumber());
        if (accountData.getMetadata() != null && accountData.getMetadata().length() > 0)
            externalAccountParams.put("metadata", accountData.getMetadata());


        accountParams.put("external_account", externalAccountParams);

        return accountParams;
    }

    /**
     * Finds the Stripe managed account based on firebase user's uid
     * @param uid
     */
    public Account getManagedAccountByUID(String uid) {
        User user = userServices.getUserByUID(uid);
        if (user != null)
            return getManagedAccountByUser(user);
        else {
            logger.debug("ERROR: could not fine managed account for user " + uid );
            return null;
        }
    }

    public Account getManagedAccountByUser(User user) {
        Account account = null;
        if (user.getAccount() != null) {
            try {
                account = Account.retrieve(user.getAccount().getStripeAccountId(), null);
                //            account = Account.retrieve(user.getAccount().getStripe().getId());
            } catch (AuthenticationException e) {
                e.printStackTrace();
                ErrorService.reportError(e);
            } catch (InvalidRequestException e) {
                e.printStackTrace();
                ErrorService.reportError(e);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                ErrorService.reportError(e);
            } catch (CardException e) {
                e.printStackTrace();
                ErrorService.reportError(e);
            } catch (APIException e) {
                e.printStackTrace();
                ErrorService.reportError(e);
            }
            return account;
        } else {
            return null;//it's ok to not have a managed account
        }
    }


    /**
     * Creates a customer with token of credit card info, should be called after collecting CC info
     *
     * https://stripe.com/docs/api/java#create_customer
     *
     * @param name
     * @param coach
     * @param creditCardToken
     * @return
     */
    public Customer createCustomer(String name, String coach, String creditCardToken) {
        Customer customer = null;
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", "Customer " + name + " for " + coach);
        customerParams.put("source", creditCardToken);

        try {
            Customer.create(customerParams);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (InvalidRequestException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (APIConnectionException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (CardException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (APIException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        }
        return customer;
    }

    /**
     * Creates a charge, which takes the payment token and the customer and move the money,
     * since we are specifying a customer we must specify a source of the customer and the
     *
     * https://stripe.com/docs/api/java#create_charge
     *
     * @param amount - client's fee in cents
     * @param fee - the fee heycoach is charging in cents
     * @param coach the coach receiving payment, should be their user/account/stripe/id
     * @param client the one paying
     * @param currency - usd or TODO: need to handle others?
     * @param description - description of charge
     * @param source - payment token id
     * @return
     */
    public Charge createCharge(int amount, int fee, User coach, User client, String currency, String description,
                               String source, String proposalId, String paymentId, String proposalNumber) {
        logger.debug("Creating Stripe charge for " + amount + " for coach " + coach.getUid() + "   client " + client.getUid() );
        String statementDescriptor = STATEMENT_DESCRIPTOR + " - Proposal" ;
        String destination = coach.getAccount().getStripeAccountId();
//        String destination = coach.getAccount().getStripe().getId();
        Charge charge = null;
        Map<String, Object> chargeParams = new HashMap<String, Object>();
        chargeParams.put("amount", amount);
        chargeParams.put("currency", currency);
        chargeParams.put("source", source);
        chargeParams.put("destination", destination);

        chargeParams.put("application_fee", fee);
        chargeParams.put("description", description);
//        chargeParams.put("customer", customerId);
        chargeParams.put("statement_descriptor", statementDescriptor);

        Map<String, String> initialMetadata = new HashMap<String, String>();
        initialMetadata.put("proposal_number", proposalNumber);
        initialMetadata.put("proposal_id", proposalId);
        initialMetadata.put("Coach_id", coach.getUid());
        initialMetadata.put("Coach_userid", coach.getUserId());
        initialMetadata.put("Coach_name", coach.getFullName());
        initialMetadata.put("Client_id", client.getUid());
        initialMetadata.put("Client_userid", client.getUserId());
        initialMetadata.put("Client_name", client.getFullName());
        chargeParams.put("metadata", initialMetadata);

        try {
            charge = Charge.create(chargeParams);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (InvalidRequestException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (APIConnectionException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (CardException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        } catch (APIException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        }
        return charge;
    }

    public ServiceResult processProposalPayment(String userId, String paymentId) {
//    public ServiceResult processProposalPayment(String userId, String paymentId, String chatMessageId) {
        logger.debug("processProposalPayment processing proposal payment for user:" + userId + " payment id:" + paymentId);
        //get the user in json
        JSONObject user = firebaseClientServices.getUserByIdJSON(userId);
        //find the payment
        JSONObject proposal = null;
        Charge charge;
        String proposalId, proposalNumber = "";
        User coach, client;
        BigDecimal serviceFee;
        BigDecimal processingFee;
        BigDecimal discount;
        BigDecimal chargeAmount;
        JSONObject card ;
        try {
            JSONObject payments = (JSONObject) user.get("payments");
            JSONObject payment = (JSONObject) payments.get(paymentId);
            JSONObject stripePayment = (JSONObject) payment.get("stripePayment");
            if (stripePayment == null)
                return new ServiceResult("ERROR", "stripePayment not found.", "problem processing payment. " +
                        " vendor payment not found for user id " + userId + " payment id:" + paymentId);
            //Stripe V3, the data structure is different, it has a token node with an id
            //JSONObject stripePaymentToken = (JSONObject) stripePayment.get("token");
            //String paymentTokenId = (String) stripePaymentToken.get("id");
            //Stripe V2:
            String paymentTokenId = (String) stripePayment.get("id");
            card = (JSONObject) stripePayment.get("card");

            //create the Stripe charge
            proposal = (JSONObject) payment.get("proposal");
            String coachId = (String) proposal.get("senderId");
            String clientId = (String) proposal.get("recipientId");
            proposalId = (String) proposal.get("id");
            proposalNumber = (String) proposal.get("proposalNumber");
            coach = firebaseClientServices.getUserByUID(coachId);
            client = firebaseClientServices.getUserByUID(clientId);
            //get description of services
            //BigDecimal fee =            proposal.getBigDecimal("fee");
            serviceFee   = proposal.getBigDecimal("serviceFee");
            processingFee   = proposal.getBigDecimal("processingFee");
            discount     = proposal.getBigDecimal("discount");

            //2 amounts are specified to stripe - fee and amount - so the fee is what Heycoach gets which includes our discounted fee plus the stripe processing fee - we pay that
            BigDecimal adjustedFee  = serviceFee.subtract(discount).add(processingFee);//changed to include processingFee AND our fee
            int adjustedFeeCents    = adjustedFee.multiply(new BigDecimal(100)).intValue();

            chargeAmount = proposal.getBigDecimal("chargeAmount");
            int chargeAmountCents   = chargeAmount.multiply(new BigDecimal(100)).intValue();

            Integer qty = proposal.getInt("qty");
            String time = proposal.getString("time");
            JSONArray services = proposal.getJSONArray("services");
            String servicesAsString = "";
            String service;
            for (int i = 0; i < services.length(); i++) {
                service = services.getString(i);
                if (servicesAsString.length() > 0)
                    servicesAsString += ", " + service;
                else
                    servicesAsString += service;
            }
            String description = "For " + qty + " " + time + " of " + servicesAsString;

            //create the charge in stripe
            charge = createCharge(chargeAmountCents, adjustedFeeCents, coach, client, "usd", description, paymentTokenId,
                    proposalId, paymentId, proposalNumber);
        } catch (JSONException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return new ServiceResult("ERROR",
                    "processProposalPayment exception: " + userId + " payment id:" + paymentId +
                            "   exception: " + e.getMessage(),
                    "problem processing payment. ");

        }

        if (charge == null ) //TODO: add additional integrity checks here
            return new ServiceResult("ERROR", "updateUserStripeCharge failed.", "problem processing payment. " +
                    " could not update user with stripe charge " + userId + " payment id:" + paymentId + " charge:" + charge + "  Charge was null.");

        //update user with the stripe charge
        String jsonCharge = Utilities.ObjectToJson(charge);
        jsonCharge = jsonCharge.replaceAll("null", "\"\"");
        if (!firebaseClientServices.updateUserStripeCharge(userId, paymentId, jsonCharge))
            return new ServiceResult("ERROR", "updateUserStripeCharge failed.", "problem processing payment. " +
                    " could not update user with stripe charge " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);

        //update user paymentMethods - to be able to reuse the card
        //done in client now
        //        if (!firebaseClientServices.updateUserPaymentMethod(userId, card))
        //            ErrorService.reportError("StripeServices.updateUserPaymentMethod failed for user " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);

        //update the user with a service
        if (!firebaseClientServices.updateUserService(userId, paymentId, proposal))
            return new ServiceResult("ERROR", "updateUserService failed.", "problem processing payment. " +
                    " user service not updated " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);

        //update user status
        if (!firebaseClientServices.updateUserStatus(userId, "paid"))
            return new ServiceResult("ERROR", "updateUserService failed.", "problem processing payment. " +
                    " user service not updated " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);

        //TODO: consolidate these 4 updates into a single update dude
        //update proposal status as paid
        if (!firebaseClientServices.updateProposalField(coach.getUid(), proposalId, "status",  "paid"))
            return new ServiceResult("ERROR", "updateProposalField failed.", "problem processing payment. " +
                    " user service not updated " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);
        if (!firebaseClientServices.updateProposalField(coach.getUid(), proposalId, "paymentId",  paymentId))
            return new ServiceResult("ERROR", "updateProposalField failed.", "problem processing payment. " +
                    " user service not updated " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);
        if (!firebaseClientServices.updateProposalField(coach.getUid(), proposalId, "chargeId",  charge.getId()))
            return new ServiceResult("ERROR", "updateProposalField failed.", "problem processing payment. " +
                    " user service not updated " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);
        if (!firebaseClientServices.updateProposalField(coach.getUid(), proposalId, "paid",  Utilities.getUTC()))
            return new ServiceResult("ERROR", "updateProposalField failed.", "problem processing payment. " +
                    " user service not updated " + userId + " payment id:" + paymentId + " jsonCharge:" + jsonCharge);

        //update the chat message to indicate paid
/*  removed 7/23/17 don't think this is necessary
        try {
            chatServices.markChatProposalPaid(coach.getUid(), client.getUid(), chatMessageId);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        }
*/

        //finally add a ledger item
        try {
            Float amount = chargeAmount.floatValue();
            Float heycoachfee = serviceFee.subtract(discount).floatValue();
            Float stripeFee = processingFee.floatValue();
            addLedgerItem(coach, LedgerItem.CATEGORY_CLIENT_PAYMENT,
                    "client " + client.getFullName() + " paid " + amount + " for services",
                    Utilities.getUTC(), Utilities.getPrettyDate(), client.getUid(), LedgerItem.TYPE_CREDIT,
                    amount, heycoachfee, stripeFee,
                    LedgerItem.SOURCE_TYPE_CLIENT, client.getUid(), LedgerItem.DESTINATION_TYPE_STRIPE_ACCT, charge.getId(),
                    LedgerItem.REFERENCE_TYPE_PROPOSAL, proposalId);//id of charge???
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
        }

        return new ServiceResult("OK" , "", "");
    }

    //parameters copied from constructor for ease in testing
    public boolean addLedgerItem(User user, String category, String description, String datestamp, String datestampPretty, String clientId,
                            String type, Float amount, Float heycoachFee, Float processingFee, String sourceType, String source, String destinationType,
                            String destination, String proposalType, String proposalId) throws JsonProcessingException {
        LedgerItem ledgerItem = new LedgerItem(category, description, datestamp, datestampPretty, clientId, type, amount, heycoachFee, processingFee,
                sourceType, source, destinationType, destination, proposalType, proposalId);
        logger.debug("addLedgerItem adding ledger for user:" + user.getUid() + " for " +  amount + "  category:" + category);
        return firebaseClientServices.createLedgerItem(user, ledgerItem);
    }


        public void acceptServicesAgreement() {
/*
        Account account = Account.retrieve({CONNECTED_STRIPE_ACCOUNT_ID}, (RequestOptions) null);

        Map<String, Object> tosAcceptanceParams = new HashMap<String, Object>();
        tosAcceptanceParams.put("date", (long) System.currentTimeMillis() / 1000L);
        tosAcceptanceParams.put("ip", request.getRemoteAddr()); // Assumes you're not using a proxy

        Map<String, Object> accountParams = new HashMap<String, Object>();
        accountParams.put("tos_acceptance", tosAcceptanceParams);

        account.update(accountParams);
*/
    }


    public void processWebHook(Event stripeEvent) {
        logger.debug("webhook event received " + stripeEvent);
    }
}
