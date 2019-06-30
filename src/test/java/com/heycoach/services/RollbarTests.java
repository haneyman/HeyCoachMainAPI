/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */
package com.heycoach.services;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class RollbarTests {
    private static final Logger logger = Logger.getLogger(RollbarTests.class);

    @Test
    public void testRollbar() throws Exception {
        try {
            throw new Exception("this is a test exception from api STATIC");
        } catch (Exception e) {
            ErrorService.reportError(e);
            e.printStackTrace();
            ErrorService.reportError(e);
        }
    }
}