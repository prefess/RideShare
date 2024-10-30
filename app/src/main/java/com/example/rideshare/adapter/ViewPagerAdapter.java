package com.example.rideshare.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.rideshare.fragment.PhoneInputFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragmentList.add(new PhoneInputFragment()); // Initial fragment
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
        notifyItemInserted(fragmentList.size() - 1);
    }
}
