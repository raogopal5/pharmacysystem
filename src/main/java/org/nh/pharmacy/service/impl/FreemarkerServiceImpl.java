package org.nh.pharmacy.service.impl;

import freemarker.template.Configuration;
import org.nh.pharmacy.service.FreemarkerService;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service("freemarkerService")
@Transactional
public class FreemarkerServiceImpl implements FreemarkerService {

    private final Logger log = LoggerFactory.getLogger(FreemarkerServiceImpl.class);

    private final Configuration freemarkerConfiguration;

    public FreemarkerServiceImpl(Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    /**
     * @param templateReference
     * @param model
     * @return
     * @throws IOException
     * @throws CustomParameterizedException
     */
    @Override
    public String mergeTemplateIntoString(final String templateReference, final Object model) throws IOException, CustomParameterizedException {
        log.info("Template Name to process:- " + templateReference);
        List<ErrorMessage> errorMessages = new ArrayList<>();
        if (null == templateReference || templateReference.isEmpty()) {
            throw new CustomParameterizedException("The given template is null, empty or blank...");
        }

        if (!templateReference.substring(templateReference.lastIndexOf(".") + 1).equalsIgnoreCase("ftl")) {
            throw new CustomParameterizedException("Expected a Freemarker template file with extension ftl...");
        }

        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                freemarkerConfiguration.getTemplate(templateReference, Charset.forName("UTF-8").name()), model);
        } catch (freemarker.template.TemplateNotFoundException e) {
            log.error("Template not found", e);
            throw new IOException(e.getMessage());
        } catch (freemarker.template.TemplateException e) {
            log.error("Failed to merge Template", e);
            throw new IOException(e.getMessage());
        }

    }

}
