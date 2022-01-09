package org.nh.pharmacy.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigDecimal;

@Converter
public class BigDecimalConverter implements AttributeConverter<BigDecimal, Double> {

    private static Logger logger = LoggerFactory.getLogger(BigDecimalConverter.class);

    @Override
    public Double convertToDatabaseColumn(BigDecimal o) {
        if (o == null) {
            return null;
        }
        logger.debug("JPA to db "+ o.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue());
        return o.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public BigDecimal convertToEntityAttribute(Double o) {
        if (o == null) {
            return null;
        }
        logger.debug("JPA from db "+ new BigDecimal(o).setScale(6, BigDecimal.ROUND_HALF_UP));
        return new BigDecimal(o).setScale(6, BigDecimal.ROUND_HALF_UP);
    }
}
