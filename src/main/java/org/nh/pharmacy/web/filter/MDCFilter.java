package org.nh.pharmacy.web.filter;

import org.nh.pharmacy.security.SecurityUtils;
import org.nh.security.dto.Preferences;
import org.nh.security.util.UserPreferencesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


public class MDCFilter extends GenericFilterBean {

    private Logger logger = LoggerFactory.getLogger("audit.request");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
            String unitCode = "-";
            if (preferences != null && preferences.getHospital() != null) {
                unitCode = preferences.getHospital().getCode();
            }
            String mdcData = String.format("%s %s %s", Optional.ofNullable(request.getHeader("X-FORWARED-FOR")).orElse(request.getRemoteHost()), SecurityUtils.getCurrentUserLogin().orElse("-"),
                unitCode);
            MDC.put("a-req-info", mdcData);
            logger.info("{}",request.getRequestURI());
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }
    }

}
