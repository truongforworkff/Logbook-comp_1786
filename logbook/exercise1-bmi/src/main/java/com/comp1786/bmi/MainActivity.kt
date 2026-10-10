package com.comp1786.bmi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.comp1786.bmi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var lastBmi: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        if (savedInstanceState?.containsKey("bmi") == true) {
            lastBmi = savedInstanceState.getDouble("bmi")
            showResult(lastBmi!!)
        }
        binding.calculate.setOnClickListener { calculate() }
    }

    private fun calculate() {
        binding.weightInput.error = null
        binding.heightInput.error = null
        // Accept decimal commas as well as decimal points, but never NaN/Infinity.
        val weight = binding.weightInput.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val height = binding.heightInput.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val kg = weight?.let { BmiCalculator.kilograms(it, binding.weightUnit.selectedItemPosition) }
        val metres = height?.let { BmiCalculator.metres(it, binding.heightUnit.selectedItemPosition) }
        if (kg == null || !kg.isFinite()) binding.weightInput.error = getString(R.string.invalid_number)
        else if (kg !in 10.0..500.0) binding.weightInput.error = getString(R.string.weight_range)
        if (metres == null || !metres.isFinite()) binding.heightInput.error = getString(R.string.invalid_number)
        else if (metres !in 0.5..2.5) binding.heightInput.error = getString(R.string.height_range)
        if (binding.weightInput.error != null || binding.heightInput.error != null) {
            lastBmi = null
            binding.result.text = getString(R.string.empty_result)
            binding.result.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.result.setTextColor(binding.heightInput.currentTextColor)
            if (binding.weightInput.error != null) binding.weightInput.requestFocus() else binding.heightInput.requestFocus()
            return
        }
        lastBmi = BmiCalculator.calculate(kg!!, metres!!)
        showResult(lastBmi!!)
    }

    private fun showResult(bmi: Double) {
        val (label, color) = when (BmiCalculator.category(bmi)) {
            BmiCategory.UNDERWEIGHT -> R.string.underweight to R.color.underweight
            BmiCategory.HEALTHY -> R.string.healthy to R.color.healthy
            BmiCategory.OVERWEIGHT -> R.string.overweight to R.color.overweight
            BmiCategory.OBESE -> R.string.obese to R.color.obese
        }
        binding.result.text = getString(R.string.result, bmi, getString(label))
        binding.result.setBackgroundColor(ContextCompat.getColor(this, color))
        binding.result.setTextColor(ContextCompat.getColor(this, R.color.result_text))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        lastBmi?.let { outState.putDouble("bmi", it) }
        super.onSaveInstanceState(outState)
    }
}
