package com.bunny.gochat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by hp on 12-01-2018.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }


    public CharSequence getPageTitle(int position){

        switch (position){
            case 2:
                return "Requests";
            case 0:
                return "Chats";
            case 1:
                return "Friends";
            default:
                    return null;
        }
    }
}
