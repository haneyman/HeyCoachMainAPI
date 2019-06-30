/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */
package com.heycoach.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class MainControllerTests {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    @Autowired EnvironmentServices environmentServices;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//        environmentServices.setHc_environment("DEVELOPMENT");
        environmentServices.setHc_environment("QA");

    }

    @Test
    public void testGetStatus() throws Exception {
        this.mockMvc.perform(get("/status"))
                .andExpect(status().isOk());
                //.andExpect(forwardedUrl(MainController.PAGE_INDEX))
                //.andExpect(model().attribute("signupForm", any(SignupForm.class)));
    }

    @Test
    public void testProcessPayment() throws Exception {
        String paymentId = "-KcTYwQwjnT7C-_8S7IH";
        this.mockMvc.perform(get("/V1/payment/process/SA78oGhLD8dazX7TW9SCVCDxhc92/" + paymentId))
                .andExpect(status().isOk());
                //.andExpect(forwardedUrl(MainController.PAGE_INDEX))
                //.andExpect(model().attribute("signupForm", any(SignupForm.class)));
    }

    @Test
    public void testInvitation() throws Exception {
        String paymentId = "-KcTYwQwjnT7C-_8S7IH";
        this.mockMvc.perform(get("/V1/" + paymentId))
                .andExpect(status().isOk());
                //.andExpect(forwardedUrl(MainController.PAGE_INDEX))
                //.andExpect(model().attribute("signupForm", any(SignupForm.class)));
    }

    @Test
    public void getAppLogs() throws Exception {
        this.mockMvc.perform(get("/V1/applogs"))
                .andExpect(status().isOk());
                //.andExpect(forwardedUrl(MainController.PAGE_INDEX))
                //.andExpect(model().attribute("signupForm", any(SignupForm.class)));
    }



}