package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.StockSource;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface DispensePrintBarcodeService {

    List<Map<String, String>> findBarcodeFormats(List<DispenseDocumentLine> dispenseDocumentLines, Long unitId);

    List<String> findBarcodeFormatForLabelPrint(List<DispenseDocumentLine> dispenseDocumentLines) throws IOException;
}
