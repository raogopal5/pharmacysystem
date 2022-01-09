package org.nh.pharmacy.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeZoneFilter extends GenericFilterBean {

    private Logger logger = LoggerFactory.getLogger("audit.request");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String localTimeData = request.getHeader("X-Localtime");
            if (localTimeData != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
                ZonedDateTime dateTime = ZonedDateTime.parse(localTimeData, formatter);

                if (Math.abs(ChronoUnit.MINUTES.between(ZonedDateTime.now(), dateTime.withZoneSameInstant(ZonedDateTime.now().getZone()))) > 5) {
                    throw new UnauthorizedClientException("Client and Server TimeZones are not same");
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            logger.error("Client and Server TimeZones are not same " + ((HttpServletRequest) servletRequest).getRequestURI());
        }
    }

}
