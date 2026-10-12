package com.example.vegitrp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vegitrp.fragments.AllRidesFragment;
import com.example.vegitrp.fragments.CompletedFragment;
import com.example.vegitrp.fragments.InterestFragment;
import com.example.vegitrp.fragments.ProcessFragment;

public class DashboardPagerAdapter extends FragmentStateAdapter {

    public DashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AllRidesFragment();
            case 1:
                return new InterestFragment();
            case 2:
                return new ProcessFragment();
            case 3:
                return new CompletedFragment();
            default:
                return new AllRidesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
