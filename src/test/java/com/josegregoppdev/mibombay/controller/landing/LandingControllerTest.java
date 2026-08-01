package com.josegregoppdev.mibombay.controller.landing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LandingControllerTest {

    private final LandingController controller = new LandingController();

    @Test
    void landing_returnsLandingView() {
        assertEquals("landing", controller.landing());
    }
}
