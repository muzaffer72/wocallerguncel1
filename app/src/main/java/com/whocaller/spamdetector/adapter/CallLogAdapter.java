/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.adapter;

import static com.whocaller.spamdetector.utils.Utils.formatDate;
import static com.whocaller.spamdetector.utils.Utils.generateAvatar;
import static com.whocaller.spamdetector.utils.Utils.getCallTypeString;
import static com.whocaller.spamdetector.utils.Utils.isPhoneNumberSaved;
import static com.whocaller.spamdetector.utils.Utils.isValidName;
import static com.whocaller.spamdetector.utils.Utils.toTextCase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.activities.ContactDetailsActivity;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.database.sqlite.ContactsDataDb;
import com.whocaller.spamdetector.modal.CallLogItem;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.modal.UserProfile;
import com.whocaller.spamdetector.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CallLogItem> callLogItems;
    private List<CallLogItem> callLogItemsFull;
    private Set<Integer> selectedItems = new HashSet<>();
    public boolean multiSelectMode = false;
    private SelectionListener selectionListener;
    private SetDefault setDefault;
    private Context context;
    private boolean isExpandable;
    private boolean isCallLogLay;
    private static final int MAX_ITEMS_COLLAPSED = 3;

    String displayName;
    boolean isSpam = false;

    ContactsDataDb dbContactsHelper;
    BlockCallerDbHelper dbCallBlockHelper;
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private boolean isDefaultDialer;

    // LiveData entegrasyonu için MutableLiveData nesnesi
    private MutableLiveData<List<CallLogItem>> callLogItemsLiveData;

    public CallLogAdapter(List<CallLogItem> callLogItems, Context context, boolean isExpandable, boolean isCallLogLay) {
        this.callLogItems = callLogItems != null ? callLogItems : new ArrayList<>();
        this.callLogItemsFull = new ArrayList<>(this.callLogItems);
        this.context = context;
        this.isExpandable = isExpandable;
        this.isCallLogLay = isCallLogLay;
        this.dbContactsHelper = new ContactsDataDb(context);
        this.dbCallBlockHelper = new BlockCallerDbHelper(context);
    }

    // LiveData bağlama metodu: adapter verilerini liveData ile günceller.
    public void setCallLogItemsLiveData(MutableLiveData<List<CallLogItem>> liveData, LifecycleOwner lifecycleOwner) {
        this.callLogItemsLiveData = liveData;
        liveData.observe(lifecycleOwner, new Observer<List<CallLogItem>>() {
            @Override
            public void onChanged(List<CallLogItem> items) {
                callLogItems = items;
                callLogItemsFull = new ArrayList<>(items);
                notifyDataSetChanged();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowHeader(boolean isDefaultDialer) {
        this.isDefaultDialer = isDefaultDialer;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (!isDefaultDialer && position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    // Gelen listeyi güncelleyen mevcut metot
    public void updateCallLogs(List<CallLogItem> callLogItems) {
        this.callLogItems = callLogItems;
        this.callLogItemsFull = new ArrayList<>(callLogItems);
        notifyDataSetChanged();
    }

    public void setSelectionListener(SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setSetDefault(SetDefault setDefault) {
        this.setDefault = setDefault;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_set_default, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(isCallLogLay ? R.layout.item_call_log : R.layout.item_call_log2, parent, false);
            return new ViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.setDefault.setOnClickListener(v -> setDefault.onItemClick(true));
        } else {
            int adjustedPosition = !isDefaultDialer ? position - 1 : position;
            ViewHolder callLogtHolder = (ViewHolder) holder;
            CallLogItem item = callLogItems.get(adjustedPosition);

            String newPhoneNumber = item.getPhoneNumber();

            callLogtHolder.whoProfile.setVisibility(View.GONE);
            callLogtHolder.whoTag.setVisibility(View.GONE);
            callLogtHolder.name.setTextColor(context.getResources().getColor(R.color.title_color, null));

            if (item.getName() != null) {
                if (isValidName(item.getName())) {
                    displayName = item.getName();
                    callLogtHolder.name.setText(toTextCase(displayName));
                } else {
                    displayName = Config.UNKNOWN_CALLER_NAME;
                    callLogtHolder.name.setText(item.getPhoneNumber());
                }
            } else {
                displayName = Config.UNKNOWN_CALLER_NAME;
                callLogtHolder.name.setText(item.getPhoneNumber());
            }

            callLogtHolder.avatar.setImageBitmap(generateAvatar(isValidName(displayName) ? displayName : "U"));
            callLogtHolder.callType.setText(getCallTypeString(item.getCallType()));
            callLogtHolder.callTypeIcon.setImageResource(item.getCallTypeIconResId());
            callLogtHolder.callDate.setText(formatDate(item.getCallDate()));
            callLogtHolder.callDuration.setText(item.getFormattedCallDuration(context));

            Contact contactdb = dbContactsHelper.getContactByPhoneNumber(newPhoneNumber);
            if (contactdb != null) {
                if (contactdb.getContactsBy() != null) {
                    if (contactdb.getContactsBy().equals("whocaller") || displayName.equals(Config.UNKNOWN_CALLER_NAME)) {
                        callLogtHolder.whoTag.setVisibility(View.VISIBLE);
                    }
                }
                if (!isPhoneNumberSaved(newPhoneNumber, context) && isValidName(contactdb.getName())) {
                    callLogtHolder.whoTag.setVisibility(View.VISIBLE);
                }
                if (isValidName(contactdb.getName())) {
                    displayName = contactdb.getName();
                    callLogtHolder.name.setText(toTextCase(contactdb.getName()));
                    callLogtHolder.avatar.setImageBitmap(generateAvatar(isValidName(contactdb.getName()) ? contactdb.getName() : "U"));
                }
                if (contactdb.isWho()) {
                    callLogtHolder.whoProfile.setVisibility(View.VISIBLE);
                }
                if (contactdb.isSpam()) {
                    isSpam = true;
                    callLogtHolder.callType.setTextColor(ContextCompat.getColor(context, R.color.red));
                    callLogtHolder.callTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                    callLogtHolder.callDate.setTextColor(context.getResources().getColor(R.color.red, null));
                    if (isCallLogLay) {
                        callLogtHolder.callType.setText(R.string.spammer);
                        callLogtHolder.avatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.spam_circle));
                    } else {
                        callLogtHolder.callType.setText(R.string.spammer + getCallTypeString(item.getCallType()));
                    }
                }
            }

            if (displayName.equals("Unknown")) {
                if (contactdb == null) {
                    if (Utils.isNetworkAvailable(context)) {
                        Utils.getContactDataDetails(newPhoneNumber, new Utils.ContactDataCallback() {
                            @Override
                            public void onSuccess(Object data) {
                                if (data != null) {
                                    if (data instanceof UserProfile) {
                                        UserProfile profile = (UserProfile) data;
                                        displayName = profile.getFirstName() + " " + profile.getLastName();
                                        callLogtHolder.whoProfile.setVisibility(View.VISIBLE);
                                        callLogtHolder.name.setText(toTextCase(displayName));
                                        callLogtHolder.whoTag.setVisibility(View.VISIBLE);
                                        callLogtHolder.avatar.setImageBitmap(generateAvatar(isValidName(displayName) ? displayName : "U"));
                                        dbContactsHelper.addContactOrUpdate(displayName, newPhoneNumber, true, false, "", "", null, null, "whocaller");
                                    } else if (data instanceof Contact) {
                                        Contact contact = (Contact) data;
                                        contact.setIsWho(false);
                                        contact.setContactsBy("whocaller");
                                        displayName = contact.getName();
                                        callLogtHolder.name.setText(toTextCase(displayName));
                                        callLogtHolder.whoTag.setVisibility(View.VISIBLE);
                                        callLogtHolder.avatar.setImageBitmap(generateAvatar(isValidName(displayName) ? displayName : "U"));
                                        if (contact.isSpam()) {
                                            isSpam = true;
                                            callLogtHolder.name.setTextColor(context.getResources().getColor(R.color.red, null));
                                            callLogtHolder.avatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.spam_circle));
                                        }
                                        dbContactsHelper.addContactOrUpdate(contact);
                                    } else {
                                        Log.e("ContactDataCallback", "Error: Unknown data type");
                                    }
                                } else {
                                    Log.e("ContactDataCallback", "Error: Received null data");
                                    callLogtHolder.name.setText(newPhoneNumber);
                                    callLogtHolder.whoTag.setVisibility(View.GONE);
                                }
                            }
                            @Override
                            public void onError(String errorMessage) {
                                Log.e("ContactDataCallback", "Error: " + errorMessage);
                                callLogtHolder.name.setText(newPhoneNumber);
                                callLogtHolder.whoTag.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }

            callLogtHolder.phoneNumber.setText(newPhoneNumber);

            final String finalNewPhoneNumber = newPhoneNumber;
            // WhatsApp butonunun tıklama olayı
            if (callLogtHolder.btnWhatsapp != null) {
                callLogtHolder.btnWhatsapp.setOnClickListener(v -> {
                    Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                    String url = "https://api.whatsapp.com/send?phone=" + finalNewPhoneNumber;
                    whatsappIntent.setData(Uri.parse(url));
                    context.startActivity(whatsappIntent);
                });
            }
            // SMS butonunun tıklama olayı
            if (callLogtHolder.btnSMS != null) {
                callLogtHolder.btnSMS.setOnClickListener(v -> {
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                    smsIntent.setData(Uri.parse("smsto:" + finalNewPhoneNumber));
                    context.startActivity(smsIntent);
                });
            }
            if (callLogtHolder.btnCall != null) {
                callLogtHolder.btnCall.setVisibility(View.VISIBLE);
                callLogtHolder.btnCall.setOnClickListener(v -> {
                    TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                    if (telecomManager != null) {
                        Uri uri = Uri.fromParts("tel", finalNewPhoneNumber, null);
                        Bundle extras = new Bundle();
                        extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false);
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // İzin isteme veya gerekli işlemleri yapın.
                        }
                        telecomManager.placeCall(uri, extras);
                    }
                });
            }

            if (isCallLogLay) {
                if (dbCallBlockHelper.isPhoneNumberBlocked(newPhoneNumber)) {
                    callLogtHolder.callType.setTextColor(ContextCompat.getColor(context, R.color.red));
                    callLogtHolder.callType.setText(R.string.blocks);
                    callLogtHolder.callTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                    callLogtHolder.callDate.setTextColor(context.getResources().getColor(R.color.red, null));
                    callLogtHolder.avatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.block_circle));
                }
                callLogtHolder.itemView.setOnClickListener(v -> {
                    if (multiSelectMode) {
                        toggleSelection(position);
                    } else {
                        Intent intent = new Intent(v.getContext(), ContactDetailsActivity.class);
                        intent.putExtra("name", callLogtHolder.name.getText().toString());
                        intent.putExtra("phoneNumber", finalNewPhoneNumber);
                        intent.putExtra("callType", getCallTypeString(item.getCallType()));
                        intent.putExtra("callDate", formatDate(item.getCallDate()));
                        intent.putExtra("isBlock", dbCallBlockHelper.isPhoneNumberBlocked(finalNewPhoneNumber));
                        intent.putExtra("isSpam", isSpam);
                        if (callLogtHolder.whoTag.getVisibility() == View.VISIBLE || callLogtHolder.whoProfile.getVisibility() == View.VISIBLE) {
                            intent.putExtra("whocaller", "whocaller");
                        }
                        if (callLogtHolder.whoProfile.getVisibility() == View.VISIBLE) {
                            intent.putExtra("whoprofile", true);
                        }
                        v.getContext().startActivity(intent);
                    }
                });
                callLogtHolder.itemView.setOnLongClickListener(v -> {
                    if (!multiSelectMode) {
                        multiSelectMode = true;
                        selectionListener.onSelectionModeChange(true);
                    }
                    toggleSelection(position);
                    return true;
                });
            } else {
                callLogtHolder.name.setVisibility(View.GONE);
                callLogtHolder.avatar.setVisibility(View.GONE);
                if (dbCallBlockHelper.isPhoneNumberBlocked(newPhoneNumber)) {
                    callLogtHolder.callType.setTextColor(ContextCompat.getColor(context, R.color.red));
                    callLogtHolder.callTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                    callLogtHolder.callDate.setTextColor(context.getResources().getColor(R.color.red, null));
                    callLogtHolder.callType.setText("Blocked - " + getCallTypeString(item.getCallType()));
                }
            }
            if (multiSelectMode) {
                int selectedColor = ContextCompat.getColor(context, R.color.colorPrimary_low);
                int defaultColor = Color.TRANSPARENT;
                holder.itemView.setBackgroundColor(selectedItems.contains(position) ? selectedColor : defaultColor);
                Bitmap avatarBitmap = generateAvatar(displayName != null ? displayName : "U");
                Drawable avatarDrawable = new BitmapDrawable(context.getResources(), avatarBitmap);
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.mark_circle);
                callLogtHolder.avatar.setImageDrawable(selectedItems.contains(position) ? drawable : avatarDrawable);
            }
        }
    }

    private void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);
        selectionListener.onItemSelectionChange(selectedItems.size());
    }

    @Override
    public int getItemCount() {
        boolean isExpanded = false;
        if (!isDefaultDialer) {
            if (isExpandable && !isExpanded) {
                return Math.min(callLogItems.size() + 1, MAX_ITEMS_COLLAPSED) + 1;
            } else {
                return callLogItems.size() + 1;
            }
        } else {
            if (isExpandable && !isExpanded) {
                return Math.min(callLogItems.size(), MAX_ITEMS_COLLAPSED);
            } else {
                return callLogItems.size();
            }
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView setDefault;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            setDefault = itemView.findViewById(R.id.set_default);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deselectAll() {
        selectedItems.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onItemSelectionChange(selectedItems.size());
        }
    }

    public boolean isAllSelected() {
        return getItemCount() == selectedItems.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAll() {
        selectedItems.clear();
        for (int i = 0; i < callLogItems.size(); i++) {
            selectedItems.add(i);
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onItemSelectionChange(selectedItems.size());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onItemSelectionChange(selectedItems.size());
        }
    }

    public void deleteSelectedItems() {
        List<CallLogItem> itemsToRemove = new ArrayList<>();
        for (int position : selectedItems) {
            CallLogItem item = callLogItems.get(position);
            itemsToRemove.add(item);
        }
        deleteCallLogEntries(itemsToRemove);
        callLogItems.removeAll(itemsToRemove);
        callLogItemsFull.removeAll(itemsToRemove);
        clearSelection();
        selectionListener.onItemDeleteChange(true);
        // LiveData kullanılıyorsa veriyi güncelle
        if (callLogItemsLiveData != null) {
            callLogItemsLiveData.setValue(callLogItems);
        }
    }

    private void deleteCallLogEntries(List<CallLogItem> items) {
        ContentResolver resolver = context.getContentResolver();
        for (CallLogItem item : items) {
            try {
                String phoneNumber = item.getPhoneNumber();
                String callDate = item.getCallDate();
                Uri callLogUri = CallLog.Calls.CONTENT_URI;
                String selection = CallLog.Calls.NUMBER + " = ? AND " + CallLog.Calls.DATE + " = ?";
                String[] selectionArgs = {phoneNumber, callDate};
                resolver.delete(callLogUri, selection, selectionArgs);
            } catch (SecurityException e) {
                Log.e("CallLogAdapter", "SecurityException: " + e.getMessage());
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phoneNumber, callType, callDate, callDuration;
        public ImageView callTypeIcon, whoTag, whoProfile;
        RoundedImageView avatar;
        public ImageButton btnCall, btnWhatsapp, btnSMS;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            phoneNumber = itemView.findViewById(R.id.phone_number);
            callType = itemView.findViewById(R.id.call_type);
            callDate = itemView.findViewById(R.id.call_date);
            callTypeIcon = itemView.findViewById(R.id.call_type_icon);
            avatar = itemView.findViewById(R.id.avatar);
            callDuration = itemView.findViewById(R.id.call_duration);
            whoTag = itemView.findViewById(R.id.who_tag);
            whoProfile = itemView.findViewById(R.id.who_profile);
            btnWhatsapp = itemView.findViewById(R.id.btnWhatsapp);
            btnSMS = itemView.findViewById(R.id.btnSMS);
            btnCall = itemView.findViewById(R.id.getCall);
        }
    }

    public interface SelectionListener {
        void onSelectionModeChange(boolean enabled);
        void onItemSelectionChange(int count);
        void onItemDeleteChange(boolean deleted);
    }

    public interface SetDefault {
        void onItemClick(boolean click);
    }
}
