package com.example.dailyreminder;


import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {


    private List<Reminder> reminderList;
    private OnReminderClickListener listener;

    public ReminderAdapter(List<Reminder> reminderList, OnReminderClickListener listener) {
        this.reminderList = reminderList;
        this.listener = listener;
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);

        holder.tvReminderTitle.setText(reminder.getTitle() != null ? reminder.getTitle() : "Untitled");
        holder.tvReminderDescription.setText(reminder.getDescription() != null ? reminder.getDescription() : "No description available");
        holder.tvReminderTime.setText(reminder.getTime() != null ? reminder.getTime() : "No time set");

        // Reflect completed state visually
        holder.checkBoxCompleted.setOnCheckedChangeListener(null); // Prevent accidental triggers
        holder.checkBoxCompleted.setChecked(reminder.isCompleted());
        if (reminder.isCompleted()) {
            holder.tvReminderTitle.setPaintFlags(holder.tvReminderTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvReminderDescription.setPaintFlags(holder.tvReminderDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvReminderTime.setPaintFlags(holder.tvReminderTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvReminderTitle.setPaintFlags(holder.tvReminderTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvReminderDescription.setPaintFlags(holder.tvReminderDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvReminderTime.setPaintFlags(holder.tvReminderTime.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Update completed state on checkbox change
        holder.checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.setCompleted(isChecked);
            if (isChecked) {
                holder.tvReminderTitle.setPaintFlags(holder.tvReminderTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvReminderDescription.setPaintFlags(holder.tvReminderDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvReminderTime.setPaintFlags(holder.tvReminderTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvReminderTitle.setPaintFlags(holder.tvReminderTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvReminderDescription.setPaintFlags(holder.tvReminderDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvReminderTime.setPaintFlags(holder.tvReminderTime.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        });

        // Handle button clicks
        holder.itemView.setOnClickListener(v -> listener.onReminderClick(reminder));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(reminder, position));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(reminder, position));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvReminderTitle, tvReminderDescription, tvReminderTime;
        Button btnEdit, btnDelete;  // Declare buttons here
        CheckBox checkBoxCompleted;
        public ReminderViewHolder(View itemView) {
            super(itemView);
            tvReminderTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvReminderDescription = itemView.findViewById(R.id.tvReminderDescription);
            tvReminderTime = itemView.findViewById(R.id.tvReminderTime);

            // Initialize the buttons
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            // Initialize checkbox
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
        }
    }


    public interface OnReminderClickListener {
        void onReminderClick(Reminder reminder);
        void onEditClick(Reminder reminder, int position);
        void onDeleteClick(Reminder reminder, int position);
    }

}