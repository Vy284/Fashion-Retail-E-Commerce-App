package com.example.shoestoreapplication;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static class Notif {
        int id; String title; String message; boolean read; String time;
    }

    private DatabaseHelper db;
    private int userId;
    private final List<Notif> items = new ArrayList<>();
    private Adapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());
        userId = new SessionManager(requireContext()).getUserId();
        tvEmpty = view.findViewById(R.id.tv_empty);

        view.findViewById(R.id.tv_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        view.findViewById(R.id.tv_read_all).setOnClickListener(v -> {
            db.markAllNotificationsAsRead(userId);
            load();
        });

        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new Adapter();
        rv.setAdapter(adapter);
        load();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) load();
    }

    private void load() {
        items.clear();
        try (Cursor c = db.getNotificationsByUserId(userId)) {
            while (c.moveToNext()) {
                Notif n = new Notif();
                n.id = c.getInt(c.getColumnIndexOrThrow("notification_id"));
                n.title = c.getString(c.getColumnIndexOrThrow("title"));
                n.message = c.getString(c.getColumnIndexOrThrow("message"));
                n.read = c.getInt(c.getColumnIndexOrThrow("is_read")) == 1;
                n.time = c.getString(c.getColumnIndexOrThrow("created_at"));
                items.add(n);
            }
        }
        Log.d("NOTIF", "userId=" + userId + ", count=" + items.size());
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Notif n = items.get(position);
            h.title.setText((n.read ? "" : "● ") + n.title);
            h.message.setText(n.message);
            h.time.setText(n.time);
            h.itemView.setBackgroundColor(n.read ? Color.parseColor("#F7F7F7") : Color.parseColor("#FFF1EB"));
            h.itemView.setOnClickListener(v -> {
                if (!n.read) {
                    db.markNotificationAsRead(n.id);
                    load();
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, message, time;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_title);
                message = v.findViewById(R.id.tv_message);
                time = v.findViewById(R.id.tv_time);
            }
        }
    }
}