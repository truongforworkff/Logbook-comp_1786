package com.comp1786.bmi;
import org.junit.Test;
import static org.junit.Assert.*;
public class BmiCalculatorTest {
    @Test public void metricExample() { assertEquals(24.22145, BmiCalculator.calculate(70, 1.7), 0.00001); }
    @Test public void imperialUnitsMatchMetric() {
        assertEquals(70, BmiCalculator.kilograms(70 / 0.45359237, 1), 0.000001);
        assertEquals(70, BmiCalculator.kilograms(70 / 6.35029318, 2), 0.000001);
        assertEquals(1.7, BmiCalculator.metres(170, 0), 0.000001);
        assertEquals(1.7, BmiCalculator.metres(1.7, 1), 0.000001);
        assertEquals(1.7, BmiCalculator.metres(1.7 / 0.0254, 2), 0.000001);
        assertEquals(1.7, BmiCalculator.metres(1.7 / 0.3048, 3), 0.000001);
    }
    @Test public void categoryBoundaries() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.category(18.499));
        assertEquals(BmiCategory.HEALTHY, BmiCalculator.category(18.5));
        assertEquals(BmiCategory.HEALTHY, BmiCalculator.category(24.999));
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.category(25));
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.category(29.999));
        assertEquals(BmiCategory.OBESE, BmiCalculator.category(30));
    }
    @Test(expected = IllegalArgumentException.class) public void zeroHeightRejected() { BmiCalculator.calculate(70, 0); }
    @Test(expected = IllegalArgumentException.class) public void nonFiniteWeightRejected() { BmiCalculator.calculate(Double.NaN, 1.7); }
    @Test(expected = IllegalArgumentException.class) public void unreasonableWeightRejected() { BmiCalculator.calculate(501, 1.7); }
}
