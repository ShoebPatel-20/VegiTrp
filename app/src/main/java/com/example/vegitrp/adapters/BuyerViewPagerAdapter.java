package com.example.vegitrp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vegitrp.fragments.BuyerActiveOrdersFragment;
import com.example.vegitrp.fragments.BuyerCompletedOrdersFragment;
import com.example.vegitrp.fragments.BuyerProcessingOrdersFragment;

public class BuyerViewPagerAdapter extends FragmentStateAdapter {

    public BuyerViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BuyerActiveOrdersFragment();
            case 1:
                return new BuyerProcessingOrdersFragment();
            case 2:
                return new BuyerCompletedOrdersFragment();
            default:
                return new BuyerActiveOrdersFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
