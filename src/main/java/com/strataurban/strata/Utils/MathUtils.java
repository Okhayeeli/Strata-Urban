package com.strataurban.strata.Utils;


public abstract class MathUtils {

    public static Double findRatingAverage(int rating, Double supplierRating, int numberOfRatings) {
        if (numberOfRatings == 0) {
            // If there are no ratings yet, return the new rating as the average
            return (double) rating;
        } else {
            // Calculate the new average
            return ((supplierRating * numberOfRatings) + rating) / (numberOfRatings + 1);
        }
    }

}
