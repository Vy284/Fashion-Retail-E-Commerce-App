package com.example.shoestoreapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.models.NotificationItem;
import com.example.shoestoreapplication.utils.SessionManager;
import com.example.shoestoreapplication.viewmodel.NotificationsViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel vm;
    private NotifAdapter adapter;
    private TextView tvEmpty;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = new SessionManager(requireContext()).getUserId();
        vm = new ViewModelProvider(this).get(NotificationsViewModel.class);
        tvEmpty = view.findViewById(R.id.tv_empty);

        view.findViewById(R.id.tv_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        view.findViewById(R.id.tv_read_all).setOnClickListener(v -> vm.markAllRead(userId));

        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotifAdapter(item -> {
            if (!item.read) vm.markRead(item.id, userId);
        });
        rv.setAdapter(adapter);

        vm.getItems().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        vm.load(userId);
    }

    private static class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.VH> {
        interface OnClick { void onClick(NotificationItem item); }

        private final List<NotificationItem> items = new ArrayList<>();
        private final OnClick onClick;

        NotifAdapter(OnClick onClick) { this.onClick = onClick; }

        void submit(List<NotificationItem> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            NotificationItem n = items.get(position);
            h.title.setText((n.read ? "" : "● ") + n.title);
            h.message.setText(n.message);
            h.time.setText(n.time);
            h.itemView.setBackgroundColor(Color.parseColor(n.read ? "#F7F7F7" : "#FFF1EB"));
            h.itemView.setOnClickListener(v -> onClick.onClick(n));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
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