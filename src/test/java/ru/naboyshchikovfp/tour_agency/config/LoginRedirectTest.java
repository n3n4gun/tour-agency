package ru.naboyshchikovfp.tour_agency.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

class LoginRedirectTest {
    @Test
    void adminGoesToManagementInsteadOfSavedClientPage() throws Exception {
        var request = new MockHttpServletRequest("GET", "/bookings");
        var response = new MockHttpServletResponse();
        var cache = new HttpSessionRequestCache();
        cache.saveRequest(request, response);
        var authentication = new UsernamePasswordAuthenticationToken("admin@example.com", "unused",
                AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        new SecurityConfig().authenticationSuccessHandler()
                .onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/admin/bookings");
        assertThat(cache.getRequest(request, response)).isNull();
    }

    @Test
    void clientReturnsToSavedBookingForm() throws Exception {
        var request = new MockHttpServletRequest("GET", "/bookings/new/5");
        var response = new MockHttpServletResponse();
        var cache = new HttpSessionRequestCache();
        cache.saveRequest(request, response);
        String savedUrl = cache.getRequest(request, response).getRedirectUrl();
        var authentication = new UsernamePasswordAuthenticationToken("client@example.com", "unused",
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        new SecurityConfig().authenticationSuccessHandler()
                .onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo(savedUrl);
    }

    @Test
    void clientWithoutSavedRequestGoesHome() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var authentication = new UsernamePasswordAuthenticationToken("client@example.com", "unused",
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        new SecurityConfig().authenticationSuccessHandler()
                .onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }
}
