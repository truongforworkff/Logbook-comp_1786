package com.comp1786.bmi

enum class BmiCategory { UNDERWEIGHT, HEALTHY, OVERWEIGHT, OBESE }

/** Conversion is kept outside the Activity so the arithmetic can be tested. */
object BmiCalculator {
    fun kilograms(value: Double, unit: Int): Double = when (unit) {
        0 -> value
        1 -> value * 0.45359237
        2 -> value * 6.35029318
        else -> error("Unknown weight unit")
    }
    fun metres(value: Double, unit: Int): Double = when (unit) {
        0 -> value / 100.0
        1 -> value
        2 -> value * 0.0254
        3 -> value * 0.3048
        else -> error("Unknown height unit")
    }
    fun calculate(kg: Double, metres: Double): Double {
        require(kg.isFinite() && kg in 10.0..500.0)
        require(metres.isFinite() && metres in 0.5..2.5)
        return kg / (metres * metres)
    }
    fun category(bmi: Double): BmiCategory = when {
        bmi < 18.5 -> BmiCategory.UNDERWEIGHT
        bmi < 25.0 -> BmiCategory.HEALTHY
        bmi < 30.0 -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }
}
