/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.whocaller.spamdetector.tabsFragments.FavouritesTabFragment;
import com.whocaller.spamdetector.tabsFragments.RecentsTabFragment;

public class PagerAdapterCallLog extends FragmentStateAdapter {
    public PagerAdapterCallLog(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RecentsTabFragment();
            case 1:
                return new FavouritesTabFragment();
            default:
                return new RecentsTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

