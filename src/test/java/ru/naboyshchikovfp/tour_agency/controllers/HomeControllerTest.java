package ru.naboyshchikovfp.tour_agency.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.ui.ExtendedModelMap;

class HomeControllerTest {
    @Test
    void adminGetsManagementNavigation() {
        var model = new ExtendedModelMap();
        var principal = new UsernamePasswordAuthenticationToken("admin@example.com", "unused",
                AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        assertThat(new HomeController().showHomePage(model, principal)).isEqualTo("index");
        assertThat(model).containsEntry("isAdmin", true).containsEntry("isUser", false);
        assertThat(model).containsEntry("currentUserEmail", "admin@example.com");
    }

    @Test
    void clientGetsOwnBookingsNavigation() {
        var model = new ExtendedModelMap();
        var principal = new UsernamePasswordAuthenticationToken("client@example.com", "unused",
                AuthorityUtils.createAuthorityList("ROLE_USER"));
        new HomeController().showHomePage(model, principal);
        assertThat(model).containsEntry("isAdmin", false).containsEntry("isUser", true);
    }

    @Test
    void guestDoesNotGetPrivateNavigation() {
        var model = new ExtendedModelMap();
        new HomeController().showHomePage(model, null);
        assertThat(model).containsEntry("currentUserEmail", null);
        assertThat(model).containsEntry("isAdmin", false).containsEntry("isUser", false);
    }
}
