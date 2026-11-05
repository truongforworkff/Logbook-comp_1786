package com.comp1786.room.features.events.ui.list;

import com.comp1786.room.features.events.logic.EventCategories;
import com.comp1786.room.features.events.ui.EventPhoto;
import com.comp1786.room.R;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.events.logic.EventValidation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.comp1786.room.databinding.EventListItemBinding;
import java.time.LocalDate;
public class EventAdapter extends ListAdapter<Event, EventAdapter.Holder> {
    public interface OnEventClick { void onClick(Event event); }
    private final OnEventClick onClick;
    public EventAdapter(OnEventClick onClick) {
        super(new DiffUtil.ItemCallback<Event>() {
            @Override public boolean areItemsTheSame(@NonNull Event a,@NonNull Event b) { return a.id==b.id; }
            @Override public boolean areContentsTheSame(@NonNull Event a,@NonNull Event b) { return a.equals(b); }
        });
        this.onClick=onClick;
    }
    public static String statusLabel(Context context,String status) {
        return context.getString("Completed".equals(status)?R.string.status_completed:"Cancelled".equals(status)?R.string.status_cancelled:R.string.status_upcoming);
    }
    public static class Holder extends RecyclerView.ViewHolder {
        final EventListItemBinding binding;
        Holder(EventListItemBinding binding) { super(binding.getRoot()); this.binding=binding; }
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type) {
        return new Holder(EventListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder,int position) {
        Event event=getItem(position);
        LocalDate date=LocalDate.parse(event.date);
        Context context=holder.itemView.getContext();
        holder.binding.category.setText(context.getResources().getStringArray(R.array.event_categories)[
                EventCategories.position(event.categoryCode)]);
        EventPhoto.load(holder.binding.cover, event.imageUri, null);
        holder.binding.name.setText(event.name);
        holder.binding.day.setText(String.valueOf(date.getDayOfMonth()));
        holder.binding.month.setText(context.getString(R.string.month_short,date.getMonthValue()));
        holder.binding.details.setText(event.location);
        holder.binding.status.setText(statusLabel(context,event.status));
        holder.itemView.setClickable(true); holder.itemView.setFocusable(true);
        holder.itemView.setContentDescription(event.name+", "+EventValidation.displayDate(event.date)+", "+event.location+", "+statusLabel(context,event.status));
        holder.itemView.setOnClickListener(view->onClick.onClick(event));
    }
}
