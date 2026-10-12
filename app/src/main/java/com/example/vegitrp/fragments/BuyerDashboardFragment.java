package com.example.vegitrp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vegitrp.R;
import com.example.vegitrp.activities.AddBuyerRequestActivity;
import com.example.vegitrp.adapters.DashboardPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BuyerDashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        FloatingActionButton fabAddRequest = view.findViewById(R.id.fab_add_request);

        DashboardPagerAdapter adapter = new DashboardPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("ALL RIDES");
                    break;
                case 1:
                    tab.setText("INTEREST");
                    break;
                case 2:
                    tab.setText("PROCESS");
                    break;
                case 3:
                    tab.setText("COMPLETED");
                    break;
            }
        }).attach();

        fabAddRequest.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddBuyerRequestActivity.class)));
    }
}
