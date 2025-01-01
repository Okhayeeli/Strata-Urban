package com.strataurban.strata.Security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

// OAuth2AuthenticationFailureHandler.java
@Component
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
//    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
//                                        AuthenticationException exception) throws IOException, java.io.IOException {
//        log.error("OAuth2 authentication failed", exception);
//
//        String targetUrl = UriComponentsBuilder.fromUriString("/login")
//                .queryParam("error", exception.getLocalizedMessage())
//                .build().toUriString();
//
//        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
//        getRedirectStrategy().sendRedirect(request, response, targetUrl);
//    }
}