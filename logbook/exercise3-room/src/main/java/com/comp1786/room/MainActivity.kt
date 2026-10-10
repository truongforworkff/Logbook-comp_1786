package com.comp1786.room

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.comp1786.room.databinding.ActivityMainBinding
import com.comp1786.room.databinding.DialogEventBinding
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var model: EventViewModel
    private var editor: AlertDialog? = null
    private var editorFields: DialogEventBinding? = null
    private var editingEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(20.dp + bars.left, 20.dp + bars.top, 20.dp + bars.right, 20.dp + bars.bottom)
            insets
        }
        model = ViewModelProvider(this)[EventViewModel::class.java]
        val adapter = EventAdapter { showEditor(it) }
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        binding.add.setOnClickListener { showEditor() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    model.events.catch { showStorageError() }.collect { events ->
                        adapter.submitList(events)
                        binding.empty.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch { model.errors.collect { showStorageError(); editor?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true } }
            }
        }
        if (savedInstanceState?.getBoolean("editorOpen") == true) {
            val restored = Event(
                id = savedInstanceState.getLong("eventId"),
                name = savedInstanceState.getString("name").orEmpty(),
                date = savedInstanceState.getString("date").orEmpty(),
                location = savedInstanceState.getString("location").orEmpty(),
                organiser = savedInstanceState.getString("organiser").orEmpty(),
                type = savedInstanceState.getString("type").orEmpty(),
                status = savedInstanceState.getString("status") ?: "Upcoming"
            )
            showEditor(if (restored.id == 0L) null else restored, restored)
        }
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()

    private fun showEditor(existing: Event? = null, draft: Event? = existing) {
        val form = DialogEventBinding.inflate(layoutInflater)
        editorFields = form
        editingEvent = existing
        draft?.let {
            form.nameInput.setText(it.name)
            form.dateInput.setText(it.date)
            form.locationInput.setText(it.location)
            form.organiserInput.setText(it.organiser)
            form.typeInput.setSelection(resources.getStringArray(R.array.types).indexOf(it.type).coerceAtLeast(0))
            form.statusInput.setSelection(resources.getStringArray(R.array.statuses).indexOf(it.status).coerceAtLeast(0))
        }
        form.pickDate.setOnClickListener {
            val initial = if (EventValidation.validDate(form.dateInput.text.toString())) LocalDate.parse(form.dateInput.text.toString()) else LocalDate.now()
            DatePickerDialog(this, { _, year, month, day ->
                form.dateInput.setText(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day))
            }, initial.year, initial.monthValue - 1, initial.dayOfMonth).show()
        }
        val builder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) R.string.add else R.string.edit)
            .setView(form.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
        if (existing != null) builder.setNeutralButton(R.string.delete, null)
        val dialog = builder.create()
        editor = dialog
        dialog.setOnDismissListener {
            editor = null
            editorFields = null
            editingEvent = null
        }
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val textFields = listOf(form.nameInput, form.locationInput, form.organiserInput)
                textFields.forEach { field -> field.error = if (EventValidation.validText(field.text.toString())) null else getString(R.string.required) }
                val date = form.dateInput.text.toString().trim()
                form.dateInput.error = if (EventValidation.validDate(date)) null else getString(R.string.invalid_date)
                val invalid = (textFields + form.dateInput).firstOrNull { it.error != null }
                if (invalid != null) { invalid.requestFocus(); return@setOnClickListener }
                val event = Event(
                    id = existing?.id ?: 0,
                    name = form.nameInput.text.toString().trim(),
                    date = date,
                    location = form.locationInput.text.toString().trim(),
                    organiser = form.organiserInput.text.toString().trim(),
                    type = form.typeInput.selectedItem.toString(),
                    status = form.statusInput.selectedItem.toString()
                )
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                model.save(event) { dialog.dismiss() }
            }
            if (existing != null) dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                AlertDialog.Builder(this).setTitle(R.string.delete_title)
                    .setMessage(getString(R.string.delete_message, existing.name))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete) { _, _ -> model.delete(existing) { dialog.dismiss() } }
                    .show()
            }
        }
        dialog.show()
    }

    private fun showStorageError() { Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show() }

    override fun onSaveInstanceState(outState: Bundle) {
        editorFields?.let { form ->
            outState.putBoolean("editorOpen", true)
            outState.putLong("eventId", editingEvent?.id ?: 0)
            outState.putString("name", form.nameInput.text.toString())
            outState.putString("date", form.dateInput.text.toString())
            outState.putString("location", form.locationInput.text.toString())
            outState.putString("organiser", form.organiserInput.text.toString())
            outState.putString("type", form.typeInput.selectedItem.toString())
            outState.putString("status", form.statusInput.selectedItem.toString())
        }
        super.onSaveInstanceState(outState)
    }
}
