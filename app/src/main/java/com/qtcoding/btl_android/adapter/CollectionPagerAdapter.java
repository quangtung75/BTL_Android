package com.qtcoding.btl_android.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.qtcoding.btl_android.FollowingCollectionsFragment;
import com.qtcoding.btl_android.OwnerCollectionsFragment;

public class CollectionPagerAdapter extends FragmentStateAdapter {

    public CollectionPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new OwnerCollectionsFragment();
        } else {
            return new FollowingCollectionsFragment();
        }
    }
}
