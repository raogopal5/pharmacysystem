package org.nh.pharmacy.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.nh.common.dto.UOMDTO;

import java.io.IOException;

/**
 * Created by Nirbhay on 10/16/17.
 */
public class UOMDeserializer extends JsonDeserializer<UOMDTO> {

    /**
     * It converts String to json type
     *
     * @param jsonParser
     * @param deserializationContext
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public UOMDTO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        UOMDTO uom = null;
        if (node == null) {
            return uom;
        }
        String uomValue = node.asText();
        if (!uomValue.isEmpty()) {
            uom = new UOMDTO();
            uom.setCode(uomValue);
            uom.setName(uomValue);
        }
        if (node.path("code") != null && !node.path("code").asText().isEmpty()) {
            uom = new UOMDTO();
            uom.setId(node.path("id").asLong());
            uom.setCode(node.path("code").asText());
            uom.setName(node.path("name").asText());
        }
        return uom;
    }
}
