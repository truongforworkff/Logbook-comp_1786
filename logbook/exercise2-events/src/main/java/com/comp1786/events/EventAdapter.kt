package com.comp1786.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.comp1786.events.databinding.ItemEventBinding

class EventAdapter(private val onClick: ((Event) -> Unit)? = null) :
    ListAdapter<Event, EventAdapter.Holder>(object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(old: Event, new: Event) = old.id == new.id
        override fun areContentsTheSame(old: Event, new: Event) = old == new
    }) {
    class Holder(val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val event = getItem(position)
        val context = holder.itemView.context
        holder.binding.name.text = event.name
        holder.binding.details.text = context.getString(R.string.details, event.date, event.location, event.organiser, event.type)
        holder.itemView.isClickable = onClick != null
        holder.itemView.isFocusable = onClick != null
        holder.itemView.setOnClickListener { onClick?.invoke(event) }
    }
}
