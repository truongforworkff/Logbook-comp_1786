package com.comp1786.bmi;
import android.os.Bundle;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.comp1786.bmi.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Double lastBmi;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        if (state != null && state.containsKey("bmi")) {
            lastBmi = state.getDouble("bmi");
            showResult(lastBmi);
        }
        binding.calculate.setOnClickListener(view -> calculate());
    }
    private Double number(String text) {
        try { return Double.parseDouble(text.trim().replace(',', '.')); }
        catch (NumberFormatException error) { return null; }
    }
    private void calculate() {
        binding.weightInput.setError(null);
        binding.heightInput.setError(null);
        Double weight = number(binding.weightInput.getText().toString());
        Double height = number(binding.heightInput.getText().toString());
        Double kg = weight == null ? null : BmiCalculator.kilograms(weight, binding.weightUnit.getSelectedItemPosition());
        Double metres = height == null ? null : BmiCalculator.metres(height, binding.heightUnit.getSelectedItemPosition());
        if (kg == null || !Double.isFinite(kg)) binding.weightInput.setError(getString(R.string.invalid_number));
        else if (kg < 10 || kg > 500) binding.weightInput.setError(getString(R.string.weight_range));
        if (metres == null || !Double.isFinite(metres)) binding.heightInput.setError(getString(R.string.invalid_number));
        else if (metres < 0.5 || metres > 2.5) binding.heightInput.setError(getString(R.string.height_range));
        if (binding.weightInput.getError() != null || binding.heightInput.getError() != null) {
            lastBmi = null;
            binding.result.setText(R.string.empty_result);
            binding.result.setBackgroundColor(Color.TRANSPARENT);
            binding.result.setTextColor(binding.heightInput.getCurrentTextColor());
            if (binding.weightInput.getError() != null) binding.weightInput.requestFocus();
            else binding.heightInput.requestFocus();
            return;
        }
        lastBmi = BmiCalculator.calculate(kg, metres);
        showResult(lastBmi);
    }
    private void showResult(double bmi) {
        int label, color;
        switch (BmiCalculator.category(bmi)) {
            case UNDERWEIGHT: label = R.string.underweight; color = R.color.underweight; break;
            case HEALTHY: label = R.string.healthy; color = R.color.healthy; break;
            case OVERWEIGHT: label = R.string.overweight; color = R.color.overweight; break;
            default: label = R.string.obese; color = R.color.obese;
        }
        binding.result.setText(getString(R.string.result, bmi, getString(label)));
        binding.result.setBackgroundColor(ContextCompat.getColor(this, color));
        binding.result.setTextColor(ContextCompat.getColor(this, R.color.result_text));
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (lastBmi != null) state.putDouble("bmi", lastBmi);
        super.onSaveInstanceState(state);
    }
}
