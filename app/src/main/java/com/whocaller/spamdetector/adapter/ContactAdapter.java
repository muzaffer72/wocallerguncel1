/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.adapter;

import static com.whocaller.spamdetector.utils.Utils.generateAvatar;
import static com.whocaller.spamdetector.utils.Utils.isValidName;
import static com.whocaller.spamdetector.utils.Utils.toTextCase;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Glide ile resim yükleme
import com.makeramen.roundedimageview.RoundedImageView;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.activities.ContactDetailsActivity;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.database.sqlite.ContactsDataDb;
import com.whocaller.spamdetector.modal.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private List<Contact> contacts;
    private boolean isDifferentLay;
    private boolean isShowHeader;
    Context context;

    public ContactAdapter(List<Contact> contacts, Context context, boolean isDifferentLay, boolean isShowHeader) {
        this.contacts = contacts;
        this.context = context;
        this.isDifferentLay = isDifferentLay;
        this.isShowHeader = isShowHeader;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowHeader && position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_contacts_safe, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(isDifferentLay ? R.layout.contact_item2 : R.layout.contact_item, parent, false);
            return new ContactViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            // Header (opsiyonel)
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            // Örnek: headerHolder.headerText.setText("Header Text");
        } else {
            // İtem
            int adjustedPosition = isShowHeader ? position - 1 : position;
            ContactViewHolder contactHolder = (ContactViewHolder) holder;
            Contact contact = contacts.get(adjustedPosition);

            contactHolder.contactName.setText(toTextCase(contact.getName()));
            contactHolder.contactPhone.setText(contact.getPhoneNumber());

            BlockCallerDbHelper db = new BlockCallerDbHelper(context);
            boolean isBlocked = db.isPhoneNumberBlocked(contact.getPhoneNumber());

            if (isBlocked) {
                // Engelli numara
                contactHolder.blockTag.setVisibility(View.VISIBLE);
                contactHolder.contactName.setTextColor(context.getResources().getColor(R.color.red, null));
                contactHolder.contactPhone.setTextColor(context.getResources().getColor(R.color.red, null));
                contactHolder.contactAvatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.block_circle));
            } else {
                // Engelli değilse
                contactHolder.blockTag.setVisibility(View.GONE);
                contactHolder.contactName.setTextColor(context.getResources().getColor(R.color.black, null));
                contactHolder.contactPhone.setTextColor(context.getResources().getColor(R.color.title_color, null));

                // Burada imageUrl yoksa avatar, varsa Glide ile yüklüyoruz
                // Not: "contact.getImageUrl()" metodunu Contact modelinizde tanımlamış olmanız gerekiyor.
                String imageUrl = contact.getImageUrl(); // eğer modelde yoksa eklemelisiniz

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // İnternetten resmi çek
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_avatar)  // placeholder
                            .error(R.drawable.ic_avatar)       // hata olursa
                            .into(contactHolder.contactAvatar);
                } else {
                    // imageUrl yoksa veya boşsa -> generateAvatar
                    contactHolder.contactAvatar.setImageBitmap(
                            generateAvatar(isValidName(contact.getName()) ? contact.getName() : "U")
                    );
                }
            }

            // İsteğe bağlı: "isDifferentLay" ise telefon gizlenir
            if (isDifferentLay) {
                contactHolder.contactPhone.setVisibility(View.GONE);
                contactHolder.blockTag.setVisibility(View.GONE);
            }

            // "whoProfile" kontrolü
            ContactsDataDb contactData = new ContactsDataDb(context);
            Contact contactDb = contactData.getContactByPhoneNumber(contact.getPhoneNumber());
            if (contactDb != null && contactDb.isWho()) {
                contactHolder.whoProfile.setVisibility(View.VISIBLE);
            } else {
                contactHolder.whoProfile.setVisibility(View.GONE);
            }

            // Tıklama olayları
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ContactDetailsActivity.class);
                intent.putExtra("name", contact.getName());
                intent.putExtra("phoneNumber", contact.getPhoneNumber());
                intent.putExtra("modal", contact);
                intent.putExtra("online", false);
                intent.putExtra("isBlock", isBlocked);
                if (contactHolder.whoProfile.getVisibility() == View.VISIBLE) {
                    intent.putExtra("whoprofile", true);
                }
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return isShowHeader ? contacts.size() + 1 : contacts.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        // Header layout içindeki bileşenler
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            // örn: headerText = itemView.findViewById(R.id.header_text);
        }
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        TextView contactPhone;
        ImageView blockTag, whoProfile;
        RoundedImageView contactAvatar;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contact_name);
            contactPhone = itemView.findViewById(R.id.contact_phone);
            contactAvatar = itemView.findViewById(R.id.contact_avatar);
            blockTag = itemView.findViewById(R.id.block_tag);
            whoProfile = itemView.findViewById(R.id.who_profile);
        }
    }
}
