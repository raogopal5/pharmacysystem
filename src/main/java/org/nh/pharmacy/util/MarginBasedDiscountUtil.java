package org.nh.pharmacy.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.nh.pharmacy.domain.DiscountSlab;
import org.springframework.scripting.support.StandardScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;

import java.util.List;
import java.util.Map;

/**
 * Created by Nirbhay on 1/25/18.
 */
public class MarginBasedDiscountUtil {

    private static StandardScriptEvaluator standardScriptEvaluator = new StandardScriptEvaluator();

    public static Float evaluateMarginBasedDiscountFormula(String formula, Map<String, Object> argumentBindings) {
        if (null == formula) {
            throw new RuntimeException("Margin Based Discount formula not configured.");
        }
        standardScriptEvaluator.setLanguage("JavaScript");
        Object evaluate = standardScriptEvaluator.evaluate(new StaticScriptSource(formula), argumentBindings);
        return Float.valueOf(evaluate.toString());
    }

    public static Float getDiscountPercentFromSlab(String json, float value) {
        try {
            List<DiscountSlab> mapList = new ObjectMapper().readValue(json,
                TypeFactory.defaultInstance().constructCollectionType(List.class, DiscountSlab.class));

            for (DiscountSlab discountSlab : mapList) {
                float min = discountSlab.getMin();
                float max = discountSlab.getMax();
                if (min <= value && value <= max) {
                    return discountSlab.getPercentage();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Exception occured, while evaluating discount slab.");
        }
        return 0f;
    }

/*    public static void main(String[] args) {
        //(Sale Rate - Avg. Purchase Cost) x 100/Sale Rate
        String formula = "((saleRate - avgPurchaseCost) * 100) / saleRate";
        Map argumentBindings = new HashMap<>();
        argumentBindings.put("saleRate", 10.0f);
        argumentBindings.put("avgPurchaseCost", 6.0f);
        System.out.println("formula::" + formula + "\t argumentBindings::" + argumentBindings);
        Float result = evaluateMarginBasedDiscountFormula(formula, argumentBindings);
        System.out.println("Result ::" + result);

        String json = "[{\"min\":0,\"max\":10,\"percentage\":0},{\"min\":11,\"max\":50,\"percentage\":5},{\"min\":51,\"max\":100,\"percentage\":10}]";

        System.out.println("Discount::"+getDiscountPercentFromSlab(json, 12.0f));
    }*/
}
