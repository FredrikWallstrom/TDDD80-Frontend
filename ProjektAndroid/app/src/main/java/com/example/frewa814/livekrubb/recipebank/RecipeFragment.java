package com.example.frewa814.livekrubb.recipebank;

import android.app.Fragment;
import android.app.ListActivity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.frewa814.livekrubb.R;

/**
 * Created by Fredrik on 2015-04-24.
 */
public class RecipeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.recipe_bank, container, false);

        return rootView;
    }

}
