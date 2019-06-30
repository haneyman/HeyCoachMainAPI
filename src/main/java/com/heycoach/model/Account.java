/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.firebase.database.Exclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
//    com.stripe.model.Account stripe;
    String stripeAccountId;

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }
    /*  Jackson coudldn't parse externalAccount field accountHolderName, couldn't annotate that with @JsonIgnore
    public com.stripe.model.Account getStripe() {
        return stripe;
    }

    public void setStripe(com.stripe.model.Account stripe) {
        this.stripe = stripe;
    }
*/
}
