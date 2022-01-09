package org.nh.pharmacy.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    private Logger logger = LoggerFactory.getLogger(BigDecimalDeserializer.class);

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (jsonParser.getCurrentValue() == null) {
            return null;
        }
        BigDecimal bigDecimal = new BigDecimal(jsonParser.getValueAsString()).setScale(6, BigDecimal.ROUND_HALF_UP);
        return bigDecimal;
    }

}
