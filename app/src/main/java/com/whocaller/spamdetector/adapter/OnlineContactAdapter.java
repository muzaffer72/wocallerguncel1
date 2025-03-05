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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.activities.ContactDetailsActivity;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.database.sqlite.ContactsDataDb;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.modal.UserProfile;
import com.whocaller.spamdetector.utils.Utils;

import java.util.List;

public class OnlineContactAdapter extends RecyclerView.Adapter<OnlineContactAdapter.ContactViewHolder> {

    private List<Contact> contacts;
    private Context context;

    private SelectionListener selectionListener;

    public OnlineContactAdapter(List<Contact> contacts, Context context) {
        this.contacts = contacts;
        this.context = context;

    }

    public void setSelectionListener(SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.online_contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        final Contact[] contact = {contacts.get(position)};
        if (contact[0].getType() != null) {
            Utils.getSearchContactDataDetails(contact[0].getId(), contact[0].getType(), new Utils.SearchContactDataCallback() {

                @Override
                public void onSuccess(Object data) {
                    if (data != null) {
                        if (data instanceof UserProfile) {
                            UserProfile profile = (UserProfile) data;
                            Contact contact1 = new Contact();

                            if (profile.getLastName() != null && !profile.getLastName().equals("null")) {
                                contact1.setName(profile.getFirstName() + " " + profile.getLastName());
                            } else {
                                contact1.setName(profile.getFirstName());
                            }

                            contact1.setPhoneNumber(profile.getPhone());
                            contact1.setIsWho(true);
                            contact1.setIsSpam(false);
                            contact[0] = contact1;
                        } else if (data instanceof Contact) {
                            Contact contacts = (Contact) data;
                            contact[0] = contacts;
                        } else {
                            Log.e("ContactDataCallback", "Error: Unknown data type");
                        }
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("ContactDataCallback", "Error: " + errorMessage);
                }
            });
        }

        if (contact[0].getName() != null) {
            holder.contactName.setText(toTextCase(contact[0].getName()));
        } else {
            holder.contactName.setText("Whocaller user");
        }

        holder.contactPhone.setText(contact[0].getPhoneNumber());

        holder.contactAvatar.setImageBitmap(generateAvatar(isValidName(contact[0].getName()) ? contact[0].getName() : "U"));


        if (contact[0].isWho()) {
            holder.whoProfile.setVisibility(View.VISIBLE);
        } else {
            holder.whoProfile.setVisibility(View.GONE);
        }

        if (contact[0].isSpam()) {
            holder.spamTag.setVisibility(View.VISIBLE);
            holder.contactName.setTextColor(context.getResources().getColor(R.color.red, null));
            holder.contactAvatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.spam_circle));
        }


        BlockCallerDbHelper db2 = new BlockCallerDbHelper(context);
        if (db2.isPhoneNumberBlocked(contact[0].getPhoneNumber())) {
            holder.blockTag.setVisibility(View.VISIBLE);
            holder.contactName.setTextColor(context.getResources().getColor(R.color.red, null));
            holder.contactPhone.setTextColor(context.getResources().getColor(R.color.red, null));
            holder.contactAvatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.block_circle));
        }


        holder.itemView.setOnClickListener(v -> {
            ContactsDataDb databaseHelper = new ContactsDataDb(context);

            String conName;

            if (contact[0].getName() != null && contact[0].getName().length() > 0) {
                conName = contact[0].getName();
            } else {
                conName = "Whocaller user";
            }

            databaseHelper.addContactOrUpdate(conName, contact[0].getPhoneNumber(), contact[0].isWho(),
                    contact[0].isSpam(), String.valueOf(contact[0].getSpamType()), contact[0].getTag(), contact[0].getCarrierName(), contact[0].getCountryName(), "whocaller");

            Intent intent = new Intent(context, ContactDetailsActivity.class);


            intent.putExtra("name", conName);
            intent.putExtra("phoneNumber", contact[0].getPhoneNumber());
            intent.putExtra("adapter", "contact");
            intent.putExtra("modal", contact[0]);

            intent.putExtra("whocaller", "whocaller");
            intent.putExtra("isBlock", db2.isPhoneNumberBlocked(contact[0].getPhoneNumber()));
            intent.putExtra("isSpam", holder.spamTag.getVisibility() == View.VISIBLE);
            intent.putExtra("spamType", String.valueOf(contact[0].getSpamType()));
            intent.putExtra("tags", String.valueOf(contact[0].getTag()));
            intent.putExtra("online", true);

            if (holder.whoProfile.getVisibility() == View.VISIBLE) {
                intent.putExtra("whoprofile", true);

            }
            context.startActivity(intent);

        });

        holder.itemView.setOnLongClickListener(v -> {
            selectionListener.onSelectionModeChange(contact[0].getPhoneNumber());
            return true;

        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        TextView contactPhone;
        ImageView whoProfile, blockTag, spamTag;
        RoundedImageView contactAvatar;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contact_name);
            contactPhone = itemView.findViewById(R.id.contact_phone);
            contactAvatar = itemView.findViewById(R.id.contact_avatar);
            whoProfile = itemView.findViewById(R.id.who_profile);
            blockTag = itemView.findViewById(R.id.block_tag);
            spamTag = itemView.findViewById(R.id.spam_tag);

        }
    }

    public interface SelectionListener {
        void onSelectionModeChange(String number);


    }
}