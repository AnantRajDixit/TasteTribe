package com.tastetribe.util;

import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Formats ingredient quantities for humans: whole numbers stay whole, common
 * fractions render as 1/4 / 1/3 / 1/2 / 2/3 / 3/4, everything else keeps a
 * reasonable number of decimals. Used by the serving scaler (backend business logic).
 */
@Component
public class QuantityFormatter {

    private static final Map<Double, String> FRACTIONS = Map.of(
            0.25, "1/4",
            1.0 / 3.0, "1/3",
            0.5, "1/2",
            2.0 / 3.0, "2/3",
            0.75, "3/4");

    public String format(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        int whole = (int) Math.floor(rounded + 1e-9);
        double frac = rounded - whole;

        // Pick the closest known fraction; accept it when we are within a tolerance.
        Map.Entry<Double, String> best = null;
        for (Map.Entry<Double, String> entry : FRACTIONS.entrySet()) {
            if (best == null || Math.abs(entry.getKey() - frac) < Math.abs(best.getKey() - frac)) {
                best = entry;
            }
        }
        if (best != null && Math.abs(best.getKey() - frac) <= 0.03) {
            if (whole > 0 && !best.getValue().isEmpty()) {
                return whole + " " + best.getValue();
            }
            return best.getValue().isEmpty() ? String.valueOf(whole) : best.getValue();
        }
        if (Math.abs(rounded - Math.rint(rounded)) < 0.01) {
            return String.valueOf((long) Math.rint(rounded));
        }
        return stripZeros(rounded);
    }

    private String stripZeros(double value) {
        String s = String.valueOf(value);
        if (s.endsWith("0")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}
