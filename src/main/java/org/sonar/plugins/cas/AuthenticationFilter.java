package org.sonar.plugins.cas;

import org.sonar.api.web.ServletFilter;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The {@link AuthenticationFilter} always redirects to the CAS Server.
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class AuthenticationFilter extends ServletFilter {

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/sessions/new");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to init
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        String loginRedirectUrl = getCasLoginUrl() + "?service=" + getSonarServiceUrl();
        ((HttpServletResponse) response).sendRedirect(loginRedirectUrl);
    }

    private String getCasLoginUrl() {
        return SonarCasProperties.CAS_SERVER_LOGIN_URL.getStringProperty();
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.getStringProperty();
        return sonarUrl + "/sessions/init/cas";
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}
