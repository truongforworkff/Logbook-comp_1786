package com.comp1786.room.features.sessions.ui.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.comp1786.room.databinding.SessionListItemBinding;
import com.comp1786.room.features.sessions.data.model.Session;
import java.util.function.Consumer;

public final class SessionAdapter extends ListAdapter<Session, SessionAdapter.Holder> {
    private final Consumer<Session> onSelect;
    public SessionAdapter(Consumer<Session> onSelect) {
        super(new DiffUtil.ItemCallback<Session>() {
            @Override public boolean areItemsTheSame(@NonNull Session a, @NonNull Session b) {
                return a.id == b.id;
            }
            @Override public boolean areContentsTheSame(@NonNull Session a, @NonNull Session b) {
                return a.name.equals(b.name) && a.time.equals(b.time) && a.type.equals(b.type);
            }
        });
        this.onSelect = onSelect;
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(SessionListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Session session = getItem(position);
        holder.binding.name.setText(session.name);
        holder.binding.time.setText(session.time);
        holder.binding.type.setText(session.type);
        // Read the current item so edits to optional fields cannot leave a stale click target.
        holder.binding.getRoot().setOnClickListener(view -> {
            int index = holder.getBindingAdapterPosition();
            if (index != RecyclerView.NO_POSITION) onSelect.accept(getItem(index));
        });
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final SessionListItemBinding binding;
        Holder(SessionListItemBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
