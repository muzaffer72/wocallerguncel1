/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.whocaller.spamdetector.R;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private final List<String> tags;
    private final OnTagClickListener onTagClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    Context context;

    public TagAdapter(Context context, List<String> tags, OnTagClickListener onTagClickListener) {
        this.tags = tags;
        this.onTagClickListener = onTagClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String tag = tags.get(position);
        holder.bind(tag, position);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder {

        private final Chip tagChip;

        TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagChip = itemView.findViewById(R.id.tagChip);
            if (tagChip == null) {
                Log.e("TagAdapter", "Error: Chip view not found!");
            }
        }

        void bind(String tag, int position) {
            if (tagChip != null) {
                tagChip.setText(tag);
                tagChip.setChipIcon(getIcon(context, tag));

                tagChip.setChecked(position == selectedPosition);
                tagChip.setOnClickListener(v -> {
                    int previousPosition = selectedPosition;
                    selectedPosition = getBindingAdapterPosition();
                    notifyItemChanged(previousPosition);
                    notifyItemChanged(selectedPosition);
                    onTagClickListener.onTagClick(tag);
                });
            }
        }
    }


    public static Drawable getIcon(Context context, String tag) {
        Resources res = context.getResources();

        if (tag.equals(res.getString(R.string.tag_education))) {
            return ContextCompat.getDrawable(context, R.drawable.edu);
        } else if (tag.equals(res.getString(R.string.tag_entertainment))) {
            return ContextCompat.getDrawable(context, R.drawable.confetti);
        } else if (tag.equals(res.getString(R.string.tag_finance))) {
            return ContextCompat.getDrawable(context, R.drawable.money);
        } else if (tag.equals(res.getString(R.string.tag_health))) {
            return ContextCompat.getDrawable(context, R.drawable.health_care);
        } else if (tag.equals(res.getString(R.string.tag_hotels))) {
            return ContextCompat.getDrawable(context, R.drawable.apartment);
        } else if (tag.equals(res.getString(R.string.tag_nightlife))) {
            return ContextCompat.getDrawable(context, R.drawable.cocktail);
        } else if (tag.equals(res.getString(R.string.tag_restaurants))) {
            return ContextCompat.getDrawable(context, R.drawable.restaurant);
        } else if (tag.equals(res.getString(R.string.tag_services))) {
            return ContextCompat.getDrawable(context, R.drawable.support);
        } else if (tag.equals(res.getString(R.string.tag_shopping))) {
            return ContextCompat.getDrawable(context, R.drawable.store);
        } else if (tag.equals(res.getString(R.string.tag_transportation))) {
            return ContextCompat.getDrawable(context, R.drawable.deliverytruck);
        } else if (tag.equals(res.getString(R.string.tag_travel))) {
            return ContextCompat.getDrawable(context, R.drawable.apartment);
        } else if (tag.equals(res.getString(R.string.tag_legal))) {
            return ContextCompat.getDrawable(context, R.drawable.auction);
        } else if (tag.equals(res.getString(R.string.tag_beauty))) {
            return ContextCompat.getDrawable(context, R.drawable.skincare);
        } else if (tag.equals(res.getString(R.string.tag_property))) {
            return ContextCompat.getDrawable(context, R.drawable.house);
        } else if (tag.equals(res.getString(R.string.tag_religious))) {
            return ContextCompat.getDrawable(context, R.drawable.religion);
        } else if (tag.equals(res.getString(R.string.tag_sports))) {
            return ContextCompat.getDrawable(context, R.drawable.football);
        }else if (tag.equals(res.getString(R.string.tag_phishing))) {
            return ContextCompat.getDrawable(context, R.drawable.phising);
        } else if (tag.equals(res.getString(R.string.tag_social_engineering))) {
            return ContextCompat.getDrawable(context, R.drawable.socialhack);
        } else if (tag.equals(res.getString(R.string.tag_fake_shopping))) {
            return ContextCompat.getDrawable(context, R.drawable.store);
        } else if (tag.equals(res.getString(R.string.tag_fake_loan))) {
            return ContextCompat.getDrawable(context, R.drawable.creditcard);
        } else if (tag.equals(res.getString(R.string.tag_pyramid_schemes))) {
            return ContextCompat.getDrawable(context, R.drawable.pyramid);
        } else if (tag.equals(res.getString(R.string.tag_fake_betting))) {
            return ContextCompat.getDrawable(context, R.drawable.footballbet);
        } else if (tag.equals(res.getString(R.string.tag_identity_theft))) {
            return ContextCompat.getDrawable(context, R.drawable.identitytheft);
        } else if (tag.equals(res.getString(R.string.tag_social_media_scam))) {
            return ContextCompat.getDrawable(context, R.drawable.socialhack);
        } else if (tag.equals(res.getString(R.string.tag_fake_technical_support))) {
            return ContextCompat.getDrawable(context, R.drawable.teknikservis);
        } else if (tag.equals(res.getString(R.string.tag_fake_rental))) {
            return ContextCompat.getDrawable(context, R.drawable.insurance);
        } else if (tag.equals(res.getString(R.string.tag_fake_job_offer))) {
            return ContextCompat.getDrawable(context, R.drawable.businessman);
        } else if (tag.equals(res.getString(R.string.tag_fake_health_products))) {
            return ContextCompat.getDrawable(context, R.drawable.medical);
        }

        // Eğer hiçbir eşleşme olmazsa varsayılan ikon döndür
        return ContextCompat.getDrawable(context, R.drawable.who);
    }



    public interface OnTagClickListener {
        void onTagClick(String tag);
    }
}
