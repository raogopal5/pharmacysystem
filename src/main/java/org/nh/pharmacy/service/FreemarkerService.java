package org.nh.pharmacy.service;

import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;

import java.io.IOException;

/**
 * Sanjitv
 */
public interface FreemarkerService {

    /**
     * @param templateReference
     * @param model
     * @return
     * @throws IOException
     * @throws CustomParameterizedException
     */
    String mergeTemplateIntoString(final String templateReference, final Object model) throws IOException, CustomParameterizedException;
}
