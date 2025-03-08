// Generated by view binder compiler. Do not edit!
package com.whocaller.spamdetector.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.whocaller.spamdetector.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class EditNameBottomLayoutBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final ImageView closeBtn;

  @NonNull
  public final EditText editText;

  @NonNull
  public final RadioButton radioBusiness;

  @NonNull
  public final RadioGroup radioGroup;

  @NonNull
  public final RadioButton radioPerson;

  @NonNull
  public final TextView save;

  private EditNameBottomLayoutBinding(@NonNull LinearLayout rootView, @NonNull ImageView closeBtn,
      @NonNull EditText editText, @NonNull RadioButton radioBusiness,
      @NonNull RadioGroup radioGroup, @NonNull RadioButton radioPerson, @NonNull TextView save) {
    this.rootView = rootView;
    this.closeBtn = closeBtn;
    this.editText = editText;
    this.radioBusiness = radioBusiness;
    this.radioGroup = radioGroup;
    this.radioPerson = radioPerson;
    this.save = save;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static EditNameBottomLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static EditNameBottomLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.edit_name_bottom_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static EditNameBottomLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.close_btn;
      ImageView closeBtn = ViewBindings.findChildViewById(rootView, id);
      if (closeBtn == null) {
        break missingId;
      }

      id = R.id.edit_text;
      EditText editText = ViewBindings.findChildViewById(rootView, id);
      if (editText == null) {
        break missingId;
      }

      id = R.id.radio_business;
      RadioButton radioBusiness = ViewBindings.findChildViewById(rootView, id);
      if (radioBusiness == null) {
        break missingId;
      }

      id = R.id.radio_group;
      RadioGroup radioGroup = ViewBindings.findChildViewById(rootView, id);
      if (radioGroup == null) {
        break missingId;
      }

      id = R.id.radio_person;
      RadioButton radioPerson = ViewBindings.findChildViewById(rootView, id);
      if (radioPerson == null) {
        break missingId;
      }

      id = R.id.save;
      TextView save = ViewBindings.findChildViewById(rootView, id);
      if (save == null) {
        break missingId;
      }

      return new EditNameBottomLayoutBinding((LinearLayout) rootView, closeBtn, editText,
          radioBusiness, radioGroup, radioPerson, save);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
