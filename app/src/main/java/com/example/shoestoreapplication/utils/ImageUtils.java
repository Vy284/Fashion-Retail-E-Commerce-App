package com.example.shoestoreapplication.utils;

import android.content.Context;
import android.widget.ImageView;

import com.example.shoestoreapplication.R;

public class ImageUtils {

    public static void load(ImageView view, String name) {
        int resId = find(view.getContext(), name);
        view.setImageResource(resId != 0 ? resId : R.drawable.ic_product_placeholder);
    }

    private static int find(Context ctx, String name) {
        if (name == null || name.trim().isEmpty()) return 0;
        int dot = name.lastIndexOf('.');
        String clean = dot > 0 ? name.substring(0, dot) : name;   // "avatar_1.png" -> "avatar_1"
        return ctx.getResources().getIdentifier(clean, "drawable", ctx.getPackageName());
    }
}