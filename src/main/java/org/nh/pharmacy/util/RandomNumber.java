package org.nh.pharmacy.util;

import java.util.Random;

/**
 * Created by vagrant on 3/9/17.
 */
public class RandomNumber {


    public static Long getRandomNumber()
    {
        long LOWER_RANGE = 0; //assign lower range value
        long UPPER_RANGE = 1000000; //assign upper range value
        Random random = new Random();
        return LOWER_RANGE + (long)(random.nextDouble()*(UPPER_RANGE - LOWER_RANGE));
    }
}
