package com.comp1786.events;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.comp1786.events.databinding.ItemEventBinding;
public class EventAdapter extends ListAdapter<Event, EventAdapter.Holder> {
    public interface OnEventClick { void onClick(Event event); }
    private final OnEventClick onClick;
    public EventAdapter(OnEventClick onClick) {
        super(new DiffUtil.ItemCallback<Event>() {
            @Override public boolean areItemsTheSame(@NonNull Event old, @NonNull Event next) { return old.id == next.id; }
            @Override public boolean areContentsTheSame(@NonNull Event old, @NonNull Event next) { return old.equals(next); }
        });
        this.onClick = onClick;
    }
    public static class Holder extends RecyclerView.ViewHolder {
        final ItemEventBinding binding;
        Holder(ItemEventBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Event event = getItem(position);
        holder.binding.name.setText(event.name);
        holder.binding.details.setText(holder.itemView.getContext().getString(R.string.details,
                event.date, event.location, event.organiser, event.type));
        holder.itemView.setClickable(onClick != null);
        holder.itemView.setFocusable(onClick != null);
        holder.itemView.setOnClickListener(view -> { if (onClick != null) onClick.onClick(event); });
    }
}
