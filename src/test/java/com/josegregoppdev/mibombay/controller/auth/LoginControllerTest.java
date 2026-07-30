package com.josegregoppdev.mibombay.controller.auth;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private final LoginController controller = new LoginController();

    @Test
    void login_sinParametros_retornaVistaLogin() {
        Model model = new ExtendedModelMap();
        String view = controller.login(null, null, null, model);
        assertEquals("login", view);
        assertFalse(model.containsAttribute("error"));
        assertFalse(model.containsAttribute("mensaje"));
    }

    @Test
    void login_conError_agregaAtributoError() {
        Model model = new ExtendedModelMap();
        String view = controller.login("true", null, null, model);
        assertEquals("login", view);
        assertTrue(model.containsAttribute("error"));
    }

    @Test
    void login_conLogout_agregaAtributoMensaje() {
        Model model = new ExtendedModelMap();
        String view = controller.login(null, "true", null, model);
        assertEquals("login", view);
        assertTrue(model.containsAttribute("mensaje"));
    }

    @Test
    void login_conExpired_agregaAtributoError() {
        Model model = new ExtendedModelMap();
        String view = controller.login(null, null, "true", model);
        assertEquals("login", view);
        assertTrue(model.containsAttribute("error"));
    }

    @Test
    void adminLogin_sinParametros_retornaVistaAdminLogin() {
        Model model = new ExtendedModelMap();
        String view = controller.adminLogin(null, null, model);
        assertEquals("admin/login", view);
    }

    @Test
    void adminLogin_conError_agregaAtributoError() {
        Model model = new ExtendedModelMap();
        String view = controller.adminLogin("true", null, model);
        assertEquals("admin/login", view);
        assertTrue(model.containsAttribute("error"));
    }
}
