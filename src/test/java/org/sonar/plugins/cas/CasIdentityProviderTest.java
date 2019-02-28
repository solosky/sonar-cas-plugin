package org.sonar.plugins.cas;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.authentication.BaseIdentityProvider;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.sonar.plugins.cas.AuthTestData.getJwtToken;
import static org.sonar.plugins.cas.util.CookieUtil.JWT_SESSION_COOKIE;

public class CasIdentityProviderTest {

    @Test
    public void loginIntegrationTest() throws TicketValidationException, IOException {
        // given
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", "cas.url.com")
                .withAttribute("sonar.cas.sonarServerUrl", "sonar.url.com")
                .withAttribute("sonar.cas.sessionStorePath", "/tmp")
                .withAttribute("sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds", "100")
                .withAttribute("sonar.cas.protocol", "cas3")
                .withAttribute("sonar.cas.rolesAttributes", "groups")
                .withAttribute("sonar.cas.eMailAttribute", "mail")
                .withAttribute("sonar.cas.fullNameAttribute", "displayName");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://sonar.url.com/somePageWhichIsNotLogin"));
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("ticket")).thenReturn("ST-1-123456789");
        when(request.getParameter("service")).thenReturn("serviceUrl");
        when(request.getContextPath()).thenReturn("http://sonar.url.com");

        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getHeaders("Set-Cookie")).thenReturn(Collections.singletonList(JWT_SESSION_COOKIE + "=" + getJwtToken()));

        BaseIdentityProvider.Context context = mock(BaseIdentityProvider.Context.class);
        when(context.getRequest()).thenReturn(request);
        when(context.getResponse()).thenReturn(response);

        Map<String, Object> casAttributes = new HashMap<>();
        casAttributes.put("mail", "ab@cd.ef");
        casAttributes.put("groups", "admin");
        casAttributes.put("displayName", "Mr. T");
        AttributePrincipal principal = mock(AttributePrincipal.class);
        when(principal.getName()).thenReturn("CAS");
        when(principal.getAttributes()).thenReturn(casAttributes);
        Assertion assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(principal);
        TicketValidator validator = mock(TicketValidator.class);
        when(validator.validate(any(), any())).thenReturn(assertion);
        TicketValidatorFactory validatorFactory = mock(TicketValidatorFactory.class);
        when(validatorFactory.create()).thenReturn(validator);

        CasAttributeSettings attributeSettings = new CasAttributeSettings(config);

        CasSessionStore sessionStore = mock(CasSessionStore.class);
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);

        CasIdentityProvider sut = new CasIdentityProvider(config, attributeSettings, sessionStoreFactory, validatorFactory);

        // when
        sut.init(context);

        // then
        verify(context).authenticate(any());
        String expectedRedirUrl = "http://sonar.url.com";
        verify(response).sendRedirect(expectedRedirUrl);
    }

    @Test
    public void init_switchToHandleLogout() throws IOException {
        // given
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", "cas.url.com")
                .withAttribute("sonar.cas.sonarServerUrl", "sonar.url.com")
                .withAttribute("sonar.cas.sessionStorePath", "/tmp")
                .withAttribute("sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds", "100")
                .withAttribute("sonar.cas.protocol", "cas3")
                .withAttribute("sonar.cas.rolesAttributes", "groups")
                .withAttribute("sonar.cas.eMailAttribute", "mail")
                .withAttribute("sonar.cas.fullNameAttribute", "displayName");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://sonar.url.com/somePageWhichIsNotLogin"));
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("logoutRequest")).thenReturn(AuthTestData.getLogoutTicket());
        when(request.getContextPath()).thenReturn("http://sonar.url.com");

        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getHeaders("Set-Cookie")).thenReturn(Collections.singletonList(JWT_SESSION_COOKIE + "=" + getJwtToken()));

        BaseIdentityProvider.Context context = mock(BaseIdentityProvider.Context.class);
        when(context.getRequest()).thenReturn(request);
        when(context.getResponse()).thenReturn(response);

        CasSessionStore sessionStore = mock(CasSessionStore.class);
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);

        CasIdentityProvider sut = new CasIdentityProvider(config, null, sessionStoreFactory, null);

        // when
        sut.init(context);

        // then
        String expectedRedirUrl = "sonar.url.com/sessions/init/cas";
        verify(response).sendRedirect(expectedRedirUrl);
    }

}