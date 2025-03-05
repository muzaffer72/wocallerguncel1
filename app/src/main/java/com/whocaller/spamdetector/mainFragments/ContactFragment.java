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
import com.whocaller.spamdetector.adapter.PagerAdapterContacts;

public class ContactFragment extends Fragment {

    private final int[] tabIcons = {R.drawable.phone_book, R.drawable.ic_person_search};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        // Initialize ViewPager2 and TabLayout
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);


        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));


        // Create an adapter for the ViewPager
        PagerAdapterContacts adapter = new PagerAdapterContacts(this);
        viewPager.setAdapter(adapter);

        // Attach TabLayout and ViewPager2 with TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Set custom view for each tab
            tab.setCustomView(R.layout.tab_item);

            // Customize tab view
            View tabView = tab.getCustomView();
            if (tabView != null) {
                // Set icon
                ImageView tabIcon = tabView.findViewById(R.id.tab_icon);
                tabIcon.setImageResource(tabIcons[position]);

                // Set initial text
                TextView tabText = tabView.findViewById(R.id.tab_text);
                tabText.setText(getTabTitle(position));

            }
        }).attach();

        // Handle page change listener to reset visibility and animate icon
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Reset visibility of tab text for all tabs except the selected one
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
                                tabIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary)));

                                // Animate icon translationX to the left
                                ObjectAnimator.ofFloat(tabIcon, "translationX", 0f, -45f).setDuration(300).start();
                            } else {


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
                return getString(R.string.my_contacts);
            case 1:
                return getString(R.string.identified);
            default:
                return null;
        }
    }
}



