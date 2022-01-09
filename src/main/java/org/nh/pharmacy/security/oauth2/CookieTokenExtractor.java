package org.nh.pharmacy.security.oauth2;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Extracts the access token from a cookie.
 * Falls back to a <code>BearerTokenExtractor</code> extracting information from the Authorization header, if no
 * cookie was found.
 */
public class CookieTokenExtractor extends BearerTokenExtractor {

    public static final String ACCESS_TOKEN_COOKIE = OAuth2AccessToken.ACCESS_TOKEN;

    /**
     * Extract the JWT access token from the request, if present.
     * If not, then it falls back to the {@link BearerTokenExtractor} behaviour.
     *
     * @param request the request containing the cookies.
     * @return the extracted JWT token; or null.
     */
    @Override
    protected String extractToken(HttpServletRequest request) {
        String result;
        Cookie accessTokenCookie = getAccessTokenCookie(request);
        if (accessTokenCookie != null) {
            result = accessTokenCookie.getValue();
        } else {
            result = super.extractToken(request);
        }
        return result;
    }

    public static Cookie getAccessTokenCookie(HttpServletRequest request) {
        return getCookie(request, ACCESS_TOKEN_COOKIE);
    }

    private static Cookie getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    String value = cookie.getValue();
                    if (StringUtils.hasText(value)) {
                        return cookie;
                    }
                }
            }
        }
        return null;
    }

}
