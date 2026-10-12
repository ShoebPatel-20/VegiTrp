package com.example.vegitrp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vegitrp.fragments.FarmerAllFragment;
import com.example.vegitrp.fragments.FarmerCompletedFragment;
import com.example.vegitrp.fragments.FarmerInterestedFragment;
import com.example.vegitrp.fragments.FarmerProcessingFragment;

public class FarmerViewPagerAdapter extends FragmentStateAdapter {

    public FarmerViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FarmerAllFragment();
            case 1:
                return new FarmerInterestedFragment();
            case 2:
                return new FarmerProcessingFragment();
            case 3:
                return new FarmerCompletedFragment();
            default:
                return new FarmerAllFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
