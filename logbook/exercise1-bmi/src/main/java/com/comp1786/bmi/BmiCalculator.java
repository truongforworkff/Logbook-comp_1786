package com.comp1786.bmi;
/** Pure Java arithmetic, independent of the Android interface. */
public final class BmiCalculator {
    private BmiCalculator() {}
    public static double kilograms(double value, int unit) {
        switch (unit) {
            case 0: return value;
            case 1: return value * 0.45359237;
            case 2: return value * 6.35029318;
            default: throw new IllegalArgumentException("Unknown weight unit");
        }
    }
    public static double metres(double value, int unit) {
        switch (unit) {
            case 0: return value / 100.0;
            case 1: return value;
            case 2: return value * 0.0254;
            case 3: return value * 0.3048;
            default: throw new IllegalArgumentException("Unknown height unit");
        }
    }
    public static double calculate(double kg, double metres) {
        if (!Double.isFinite(kg) || kg < 10 || kg > 500 ||
                !Double.isFinite(metres) || metres < 0.5 || metres > 2.5) {
            throw new IllegalArgumentException("Weight or height outside reasonable range");
        }
        return kg / (metres * metres);
    }
    public static BmiCategory category(double bmi) {
        if (bmi < 18.5) return BmiCategory.UNDERWEIGHT;
        if (bmi < 25) return BmiCategory.HEALTHY;
        if (bmi < 30) return BmiCategory.OVERWEIGHT;
        return BmiCategory.OBESE;
    }
}
