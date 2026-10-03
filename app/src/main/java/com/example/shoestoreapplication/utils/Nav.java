package com.example.shoestoreapplication.utils;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class Nav {

    public static void open(Fragment from, Fragment to) {
        View root = from.getView();
        if (root == null || !(root.getParent() instanceof ViewGroup)) return;

        int containerId = ((ViewGroup) root.getParent()).getId();
        from.getParentFragmentManager().beginTransaction()
                .replace(containerId, to)
                .addToBackStack(null)
                .commit();
    }
}