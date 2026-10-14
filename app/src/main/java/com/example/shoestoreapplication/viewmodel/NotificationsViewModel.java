package com.example.shoestoreapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.shoestoreapplication.models.NotificationItem;
import com.example.shoestoreapplication.repository.ShoeRepository;

import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {
    private final ShoeRepository repo;
    private final MutableLiveData<List<NotificationItem>> items = new MutableLiveData<>();

    public NotificationsViewModel(@NonNull Application app) {
        super(app);
        repo = ShoeRepository.get(app);
    }

    public LiveData<List<NotificationItem>> getItems() { return items; }

    public void load(int userId) {
        repo.run(() -> items.postValue(repo.getNotifications(userId)));
    }

    public void markRead(int id, int userId) {
        repo.run(() -> {
            repo.markNotificationRead(id);
            items.postValue(repo.getNotifications(userId));
        });
    }

    public void markAllRead(int userId) {
        repo.run(() -> {
            repo.markAllNotificationsRead(userId);
            items.postValue(repo.getNotifications(userId));
        });
    }
}