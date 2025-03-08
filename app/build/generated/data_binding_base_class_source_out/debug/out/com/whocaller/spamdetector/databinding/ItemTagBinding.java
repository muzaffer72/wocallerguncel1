// Generated by view binder compiler. Do not edit!
package com.whocaller.spamdetector.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.google.android.material.chip.Chip;
import com.whocaller.spamdetector.R;
import java.lang.NullPointerException;
import java.lang.Override;

public final class ItemTagBinding implements ViewBinding {
  @NonNull
  private final Chip rootView;

  @NonNull
  public final Chip tagChip;

  private ItemTagBinding(@NonNull Chip rootView, @NonNull Chip tagChip) {
    this.rootView = rootView;
    this.tagChip = tagChip;
  }

  @Override
  @NonNull
  public Chip getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemTagBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemTagBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
      boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_tag, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemTagBinding bind(@NonNull View rootView) {
    if (rootView == null) {
      throw new NullPointerException("rootView");
    }

    Chip tagChip = (Chip) rootView;

    return new ItemTagBinding((Chip) rootView, tagChip);
  }
}
