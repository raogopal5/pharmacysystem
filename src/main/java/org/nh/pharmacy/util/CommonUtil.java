package org.nh.pharmacy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CommonUtil.class);

    /**
     * Get the documentNumber based on processId
     * @param processId
     * @return
     */
    public static String getDocumentNumber(String processId){
        LOG.debug("getDocumentNumber method starts : processId : {}", processId);
        String documentNumber = null;
        if(processId == null)
            return null;
        switch (processId){
            case "dispense_document_process":
                documentNumber = "document_number";
                break;
        }
        return documentNumber;
    }
}
