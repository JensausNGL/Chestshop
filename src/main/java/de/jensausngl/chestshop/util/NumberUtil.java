package de.jensausngl.chestshop.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtil {

    private static final DecimalFormat FORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Locale.GERMANY);

    static {
        FORMAT.applyPattern("#,##0.00");
    }

    public static int numbersAfterDecimal(final double value) {
        final String string = Double.toString(value);
        final int index = string.indexOf(".");

        if (index < 0) {
            return 0;
        }

        return string.length() - index - 1;
    }

    public static double getDouble(final String value) {
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException ignore) {
            return -1;
        }
    }

    public static int getInt(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ignore) {
            return -1;
        }
    }

    public static String format(final double value) {
        return FORMAT.format(value);
    }

}
