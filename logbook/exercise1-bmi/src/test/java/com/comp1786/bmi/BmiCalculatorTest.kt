package com.comp1786.bmi

import org.junit.Assert.*
import org.junit.Test

class BmiCalculatorTest {
    @Test fun metricExample() { assertEquals(24.22145, BmiCalculator.calculate(70.0, 1.7), 0.00001) }
    @Test fun imperialUnitsMatchMetric() {
        assertEquals(70.0, BmiCalculator.kilograms(70.0 / 0.45359237, 1), 0.000001)
        assertEquals(70.0, BmiCalculator.kilograms(70.0 / 6.35029318, 2), 0.000001)
        assertEquals(1.7, BmiCalculator.metres(170.0, 0), 0.000001)
        assertEquals(1.7, BmiCalculator.metres(1.7, 1), 0.000001)
        assertEquals(1.7, BmiCalculator.metres(1.7 / 0.0254, 2), 0.000001)
        assertEquals(1.7, BmiCalculator.metres(1.7 / 0.3048, 3), 0.000001)
    }
    @Test fun categoryBoundaries() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.category(18.499))
        assertEquals(BmiCategory.HEALTHY, BmiCalculator.category(18.5))
        assertEquals(BmiCategory.HEALTHY, BmiCalculator.category(24.999))
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.category(25.0))
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.category(29.999))
        assertEquals(BmiCategory.OBESE, BmiCalculator.category(30.0))
    }
    @Test(expected = IllegalArgumentException::class) fun zeroHeightRejected() { BmiCalculator.calculate(70.0, 0.0) }
    @Test(expected = IllegalArgumentException::class) fun nonFiniteWeightRejected() { BmiCalculator.calculate(Double.NaN, 1.7) }
    @Test(expected = IllegalArgumentException::class) fun unreasonableWeightRejected() { BmiCalculator.calculate(501.0, 1.7) }
}
