/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.google.firebase.FirebaseApp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class FirebaseAdminServicesTests {

    @Autowired
    private FirebaseAdminServices firebaseAdminServices;
    @Autowired EnvironmentServices environmentServices;

    @Before
    public void setup() {
//        environmentServices.setHc_environment("DEVELOPMENT");
        environmentServices.setHc_environment("QA");
    }

    @Test
    public void testAddLedger() {
        FirebaseApp result = firebaseAdminServices.initialize();
        assertTrue(result != null);

    }




}
