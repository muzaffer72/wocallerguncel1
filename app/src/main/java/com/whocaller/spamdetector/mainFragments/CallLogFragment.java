/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.mainFragments;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.adapter.PagerAdapterCallLog;

public class CallLogFragment extends Fragment {

    private final int[] tabIcons = {R.drawable.ic_history, R.drawable.favorite_border};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_log, container, false);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);


        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));


        PagerAdapterCallLog adapter = new PagerAdapterCallLog(this);
        viewPager.setAdapter(adapter);


        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setCustomView(R.layout.tab_item);

            View tabView = tab.getCustomView();
            if (tabView != null) {
                ImageView tabIcon = tabView.findViewById(R.id.tab_icon);
                tabIcon.setImageResource(tabIcons[position]);

                TextView tabText = tabView.findViewById(R.id.tab_text);
                tabText.setText(getTabTitle(position));

            }
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null) {
                        View tabView = tab.getCustomView();
                        if (tabView != null) {
                            TextView tabText = tabView.findViewById(R.id.tab_text);
                            ImageView tabIcon = tabView.findViewById(R.id.tab_icon);
                            if (i == position) {
                                if (tabText.getVisibility() == View.INVISIBLE) {
                                    tabText.setVisibility(View.VISIBLE);
                                    ObjectAnimator.ofFloat(tabText, "alpha", 0f, 1f).setDuration(500).start();
                                }
                                tabIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary)));

                                if (tabText.getText().equals(R.string.favourites2)) {
                                    tabIcon.setImageResource(R.drawable.favorite_fill);
                                }
                                ObjectAnimator.ofFloat(tabIcon, "translationX", 0f, -45f).setDuration(300).start();
                            } else {

                                if (tabText.getText().equals(R.string.favourites2)) {
                                    tabIcon.setImageResource(R.drawable.favorite_border);
                                }
                                tabText.setVisibility(View.INVISIBLE);
                                tabIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
                                ObjectAnimator.ofFloat(tabIcon, "translationX", 0f, +45f).setDuration(300).start();
                            }
                        }
                    }
                }
            }
        });

        return view;
    }

    private String getTabTitle(int position) {
        switch (position) {
            case 0:
                return getString(R.string.recents);
            case 1:
                return getString(R.string.favourites2);
            default:
                return null;
        }
    }
}