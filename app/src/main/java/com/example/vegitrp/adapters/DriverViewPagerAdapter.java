package com.example.vegitrp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vegitrp.fragments.DriverAllRidesFragment;
import com.example.vegitrp.fragments.DriverCompletedRidesFragment;
import com.example.vegitrp.fragments.DriverInterestedRidesFragment;
import com.example.vegitrp.fragments.DriverProcessingRidesFragment;

public class DriverViewPagerAdapter extends FragmentStateAdapter {

    public DriverViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DriverAllRidesFragment();
            case 1:
                return new DriverInterestedRidesFragment();
            case 2:
                return new DriverProcessingRidesFragment();
            case 3:
                return new DriverCompletedRidesFragment();
            default:
                return new DriverAllRidesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
