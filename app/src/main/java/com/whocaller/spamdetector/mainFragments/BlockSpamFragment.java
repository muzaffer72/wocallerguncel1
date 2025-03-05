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
import com.whocaller.spamdetector.adapter.PagerAdapterSpamBlock;


public class BlockSpamFragment extends Fragment {

    private final int[] tabIcons = {R.drawable.tab_spam_item, R.drawable.block_user};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blockspam, container, false);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.red));


        PagerAdapterSpamBlock adapter = new PagerAdapterSpamBlock(this);
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
                                // Animate text visibility
                                if (tabText.getVisibility() == View.INVISIBLE) {
                                    tabText.setVisibility(View.VISIBLE);
                                    ObjectAnimator.ofFloat(tabText, "alpha", 0f, 1f).setDuration(500).start();
                                }

                                // Animate icon translationX to the left
                                ObjectAnimator.ofFloat(tabIcon, "translationX", 0f, -45f).setDuration(300).start();
                                tabText.setTextColor(requireContext().getResources().getColor(R.color.red, null));
                                tabIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red)));
                            } else {
                                tabText.setTextColor(requireContext().getResources().getColor(R.color.black, null));
                                tabIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
                                tabText.setVisibility(View.INVISIBLE);
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
                return getString(R.string.spam_list);
            case 1:
                return getString(R.string.block_list);
            default:
                return null;
        }
    }


}