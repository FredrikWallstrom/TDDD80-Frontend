package com.example.frewa814.livekrubb.flow;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;

/**
 * Created by Fredrik on 2015-04-27.
 */
public class ShareRecipeFragment extends Fragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.share_recipe, container, false);

        return rootView;
    }



}
