package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.pharmacy.domain.enumeration.FormatType;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serializable;

/**
 * A BarcodeConfiguration.
 */

@Document(indexName = "barcodeconfiguration")
@Setting(settingPath = "/es/settings.json")
public class BarcodeConfiguration implements Serializable {

    private Long id;
    private String code;
    private String printerName;
    private String format;
    private FormatType formatType;
    private Integer columnCount;
    private Boolean printNewLine;
    private OrganizationDTO unit;
    private HealthcareServiceCenterDTO hsc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public FormatType getFormatType() {
        return formatType;
    }

    public void setFormatType(FormatType formatType) {
        this.formatType = formatType;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public Boolean getPrintNewLine() {
        return printNewLine;
    }

    public void setPrintNewLine(Boolean printNewLine) {
        this.printNewLine = printNewLine;
    }

    public OrganizationDTO getUnit() {
        return unit;
    }

    public void setUnit(OrganizationDTO unit) {
        this.unit = unit;
    }

    public HealthcareServiceCenterDTO getHsc() {
        return hsc;
    }

    public void setHsc(HealthcareServiceCenterDTO hsc) {
        this.hsc = hsc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BarcodeConfiguration)) return false;

        BarcodeConfiguration that = (BarcodeConfiguration) o;

        if (!id.equals(that.id)) return false;
        if (!code.equals(that.code)) return false;
        if (!printerName.equals(that.printerName)) return false;
        if (!format.equals(that.format)) return false;
        if (formatType != that.formatType) return false;
        if (!columnCount.equals(that.columnCount)) return false;
        if (!printNewLine.equals(that.printNewLine)) return false;
        if (!unit.equals(that.unit)) return false;
        return hsc.equals(that.hsc);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + code.hashCode();
        result = 31 * result + printerName.hashCode();
        result = 31 * result + format.hashCode();
        result = 31 * result + formatType.hashCode();
        result = 31 * result + columnCount.hashCode();
        result = 31 * result + printNewLine.hashCode();
        result = 31 * result + unit.hashCode();
        result = 31 * result + hsc.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BarcodeConfiguration{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", printerName='" + printerName + '\'' +
            ", format='" + format + '\'' +
            ", formatType=" + formatType +
            ", columnCount=" + columnCount +
            ", printNewLine=" + printNewLine +
            ", unit=" + unit +
            ", hsc=" + hsc +
            '}';
    }
}
