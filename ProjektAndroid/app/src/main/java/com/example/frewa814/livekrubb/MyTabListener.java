package com.example.frewa814.livekrubb;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by Fredrik on 2015-04-22.
 */
public class MyTabListener implements ActionBar.TabListener {

    private Fragment mFragment;
    private final Activity mActivity;
    private final String mFragName;

    public MyTabListener( Activity activity,
                          String fragName )
    {
        mActivity = activity;
        mFragName = fragName;
    }

    @Override
    public void onTabReselected( ActionBar.Tab tab,
                                 FragmentTransaction ft )
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabSelected( ActionBar.Tab tab,
                               FragmentTransaction ft )
    {
        mFragment = Fragment.instantiate(mActivity,mFragName);
        ft.add(android.R.id.content, mFragment );
    }



    @Override
    public void onTabUnselected( ActionBar.Tab tab,
                                 FragmentTransaction ft )
    {
        ft.remove( mFragment );
        mFragment = null;
    }
}

