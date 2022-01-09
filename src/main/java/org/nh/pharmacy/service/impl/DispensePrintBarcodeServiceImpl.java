package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.nh.pharmacy.domain.dto.BarcodeConfiguration;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.service.DispensePrintBarcodeService;
import org.nh.pharmacy.service.ElasticSearchQueryService;
import org.nh.pharmacy.service.FreemarkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service
public class DispensePrintBarcodeServiceImpl implements DispensePrintBarcodeService {

    private final Logger log = LoggerFactory.getLogger(StockSourceServiceImpl.class);

    private final ElasticSearchQueryService elasticSearchQueryService;

    private final FreemarkerService freemarkerService;

    public DispensePrintBarcodeServiceImpl(ElasticSearchQueryService elasticSearchQueryService, FreemarkerService freemarkerService){
        this.elasticSearchQueryService = elasticSearchQueryService;
        this.freemarkerService = freemarkerService;
    }

    @Override
    public List<Map<String, String>> findBarcodeFormats(List<DispenseDocumentLine> dispenseDocumentLines, Long unitId) {
        log.debug("Request to get Barcode configurations");
        List<Map<String, String>> barcodeFormat = new ArrayList<>();
        for (DispenseDocumentLine dispenseDocumentLine : dispenseDocumentLines) {
            barcodeFormat.add(getBarcodeFormat(dispenseDocumentLine,unitId));
        }
        return barcodeFormat;
    }

    private Map<String, String> getBarcodeFormat(DispenseDocumentLine dispenseDocumentLine, Long id) {
        log.debug("Request to getBarcodeFormat : {} ", id);
        Map<String, String> map = new HashedMap();
        String query = "unit.id:" + id;
        List<BarcodeConfiguration> barcodeConfigurationList = elasticSearchQueryService.queryElasticSearch(query, BarcodeConfiguration.class, "barcodeconfiguration");
        if (!barcodeConfigurationList.isEmpty()) {
            BarcodeConfiguration barcodeConfiguration = barcodeConfigurationList.iterator().next();
            String barcodeFormat = getFormat(dispenseDocumentLine, barcodeConfiguration.getFormat());
            if (barcodeConfiguration.getPrintNewLine()) {
                barcodeFormat = barcodeFormat.concat("\n\n");
            }
            map.put("format", barcodeFormat);
            map.put("printerName", barcodeConfiguration.getPrinterName());
            map.put("columnCount", barcodeConfiguration.getColumnCount().toString());
            map.put("printQuantity",dispenseDocumentLine.getPrintQuantity()!=null?dispenseDocumentLine.getPrintQuantity().toString() : "0");
        }
        return map;
    }

    private String getFormat(DispenseDocumentLine dispenseDocumentLine, String format) {
        log.debug("Request to getFormat");
        String source[] = {"{BARCODE}","{BRANDNAME}", "{BATCH_NO}", "{EXPIRY_DATE}", "{MRP}"};
        String target[] = {dispenseDocumentLine.getStockBarCode(),getBrandNameFromItemName(dispenseDocumentLine.getName()), dispenseDocumentLine.getBatchNumber(), dispenseDocumentLine.getExpiryDate().toString(), dispenseDocumentLine.getMrp().toString()};
        String barcodeformat = StringUtils.replaceEach(format, source, target);
        return barcodeformat;
    }

    @Override
    public List<String> findBarcodeFormatForLabelPrint(List<DispenseDocumentLine> dispenseDocumentLines) throws IOException {
        log.debug("Request to get Barcode configurations");
        List<String> barcodeFormatForLabel = new ArrayList<>();
        for (DispenseDocumentLine dispenseDocumentLine : dispenseDocumentLines) {
            barcodeFormatForLabel.add(getBarcodeFormatForLabelPrint(dispenseDocumentLine));
        }
        return barcodeFormatForLabel;
    }

    private String getBarcodeFormatForLabelPrint(DispenseDocumentLine dispenseDocumentLine) throws IOException {
        log.debug("Request to getBarcodeFormat for label print");
        Map<String, String> labelMap = new HashMap();
        String templateFilePath = "pharmacy-label.ftl";

        if (nonNull(dispenseDocumentLine.getName())) {
            String[] split = dispenseDocumentLine.getName().split("-");

            if (split.length >=4) {
                String itemName = split[3];
                if (itemName.length() > 28) {
                    labelMap.put("NAME", itemName.substring(0, 27));
                    labelMap.put("NAME2", itemName.substring(28));
                } else {
                    labelMap.put("NAME", itemName);
                    labelMap.put("NAME2", " ");
                }
            } else {
                labelMap.put("NAME", "-");
                labelMap.put("NAME2", " ");
            }
        } else {
            labelMap.put("NAME", "-");
            labelMap.put("NAME2", " ");
        }

        labelMap.put("STRENGTH", dispenseDocumentLine.getMedication().getDrugStrength());
        labelMap.put("FORM", nonNull(dispenseDocumentLine.getMedication().getDrugForm()) ? dispenseDocumentLine.getMedication().getDrugForm().getCode() : null);
        labelMap.put("BATCH", dispenseDocumentLine.getBatchNumber());
        labelMap.put("EXPIRY_DATE", dispenseDocumentLine.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        log.debug("Label Map: {}", labelMap);

        String formatData = freemarkerService.mergeTemplateIntoString(templateFilePath, labelMap);
        log.debug("PRN Format: {}", formatData);

        return formatData;
    }

    private String getBrandNameFromItemName(String itemName) {
        String[] splitedItemName = itemName.trim().split("-");
        return splitedItemName[3];
    }
}
