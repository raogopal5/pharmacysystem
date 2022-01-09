package org.nh.pharmacy.util;

import org.nh.common.util.BigDecimalUtil;

import java.math.BigDecimal;

import static org.nh.common.util.BigDecimalUtil.*;

/**
 * Created by Nirbhay on 8/16/17.
 */
public class CalculateTaxUtil {

    public static BigDecimal calculateTax(BigDecimal amount, Float percentage){
        BigDecimal basicAmount = divide(amount, getBigDecimal((percentage * 0.01f) + 1));
        BigDecimal taxAmount = subtract(amount , basicAmount);
        return taxAmount;
    }

    public static BigDecimal reverseCalculateTaxAmount(BigDecimal amount, Float percentage, Float totalPercentage) {
        return divide(amount.multiply(getBigDecimal(percentage)), (getBigDecimal(totalPercentage +100f)));
        //return multiply(amount, percentage) / (totalPercentage + 100);
    }

    public static BigDecimal reverseCalculateGross(BigDecimal amount, Float percentage){
        return divide(amount, getBigDecimal((percentage * 0.01f) + 1));
        //return amount / ((percentage * 0.01f) + 1);
    }

    public static BigDecimal splitAmountAgainstPercent(BigDecimal amount, Float percentage){
        /*if(percentage == 0) return 0f;
        return amount / ((percentage * 0.01f) + 1);*/
        if(percentage == 0) return BigDecimalUtil.ZERO;
        return divide(amount, getBigDecimal((percentage * 0.01f) + 1));
    }

    public static BigDecimal splitPercentAmount(BigDecimal amount, Float percentage){
        if(percentage == 0) return BigDecimalUtil.ZERO;
        BigDecimal basicAmount = divide(amount, getBigDecimal((percentage * 0.01f) + 1));
        //BigDecimal basicAmount = amount / ((percentage * 0.01f) + 1);
        return amount.subtract(basicAmount);
    }

    public static BigDecimal calculatePercentAmount(BigDecimal amount, Float percentage){
        if(percentage == 0) return BigDecimalUtil.ZERO;
        //return multiply(amount, (percentage /100));
        return amount.multiply(getBigDecimal(percentage /100));
    }

    public static BigDecimal splitTaxAmount(BigDecimal amount, Float percentage, Float totalPercentage) {
        if (percentage == 0) return BigDecimalUtil.ZERO;
        //return multiply(amount, (percentage /100));
        return amount.multiply(getBigDecimal(percentage / totalPercentage));
    }

    /*public static float calculateTax(float amount, float percentage){
        float basicAmount = divide(amount, ((percentage * 0.01f) + 1));
        float taxAmount = subtract(amount , basicAmount);
        return taxAmount;
    }

    public static float reverseCalculateTaxAmount(float amount, float percentage, float totalPercentage) {
        return multiply(amount, percentage) / (totalPercentage + 100);
    }

    public static float reverseCalculateGross(float amount, float percentage){
        return amount / ((percentage * 0.01f) + 1);
    }

    public static float splitAmountAgainstPercent(float amount, float percentage){
        if(percentage == 0) return 0f;
        return amount / ((percentage * 0.01f) + 1);
    }

    public static float splitPercentAmount(float amount, float percentage){
        if(percentage == 0) return 0f;
        float basicAmount = amount / ((percentage * 0.01f) + 1);
        return amount - basicAmount;
    }

    public static float calculatePercentAmount(float amount, float percentage){
        if(percentage == 0) return 0f;
        return multiply(amount, (percentage /100));
    }
*/
    /*public static Float roundOff(Float amount, int decimalPlace){
        if(amount == null){
            return amount;
        }
        return round3(amount, decimalPlace);
    }

    public static float round1(Float amount, int decimalPlace) {
        Double powVal =  Math.pow(10d, decimalPlace);
        amount = amount * powVal.floatValue();
        int roundedValue = Math.round(amount);
        return  roundedValue/ powVal.floatValue();
    }*/

    public static float round2(float number, int scale) {
        int pow = 1;
        for (int i = 1; i <= scale; i++)
            pow *= 10;
        float tmp = number * pow;
        float result = (tmp - (int) tmp);
        if (result > 0) {
            return ( (float) ( (int) (result >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
        } else {
            return ( (float) ( (int) (result <= -0.5f ? tmp - 1 : tmp) ) ) / pow;
        }
    }

   /* public static float round3(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).floatValue();
    }*/

   /*public static void main(String[] args) {
        float givenPer = 10f;
        float userPer = 0f;
        Float[] lines = new Float[]{105f};
        Float totalAmount = 0f;
        float grossAmount =0f ;
        for (Float line: lines) {
            float amountAfterDiscount = line - multiply(line, 5*0.01f);
            amountAfterDiscount = multiply(amountAfterDiscount, 6*0.01f);
            System.out.println(amountAfterDiscount);
            System.out.println(roundOff(amountAfterDiscount,2));
            /*grossAmount += line;
            totalAmount += round2(givenPer*line*0.01f, 3);
            totalAmount += round2(userPer*line*0.01f, 3);
            System.out.println("Before__________");
            System.out.println(totalAmount);
            //totalAmount = totalAmount;
            System.out.println("After__________");
            System.out.println(totalAmount);*//*
        }
        *//*System.out.println(round2(totalAmount, 0));
        System.out.println(round2(totalAmount*100f/grossAmount, 2));
        System.out.println(round2(82f-81.55f, 2));*//*

    }*/

    public static BigDecimal calculateAmountToReturn(Float returnQuantity, Float issueQuantity, BigDecimal amount){
        return divide(multiply(amount, returnQuantity),getBigDecimal(issueQuantity));
    }

   /* public static float multiply(float a, float b) {
        return multiply(a, b, 6);
    }

    public static float multiply(float a, float b, int scale) {
        return BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b)).setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static float divide(float a, float b) {
        //return BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), BigDecimal.ROUND_HALF_UP).floatValue();
        return a/b;
    }

    public static float divide(float a, float b, int scale) {
        //return BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), BigDecimal.ROUND_HALF_UP).floatValue();
        return roundOff(a/b, scale);
    }

    public static float subtract(float a, float b) {
        return BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)).setScale(6, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static float subtract(float a, float b, int scale) {
        return BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)).setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static float returnZeroIfNegative(float a) {
        return a < 0 ? 0f : a;
    }

    public static float add(float a, float b) {
        return add(a, b, 6);
    }

    public static float add(float a, float b, int scale) {
        return BigDecimal.valueOf(a).add(BigDecimal.valueOf(b)).setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }*/
}
