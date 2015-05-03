package com.example.frewa814.livekrubb.recipe;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Fredrik on 2015-05-01.
 */
public class ShowRecipeFragment extends Fragment {

    OnButtonClickedListener mListener;

    private TextView mRecipeNameView;
    private TextView mRecipeInfoView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Hide the actionBar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Check if MainActivity is implementing the click listener.
        try {
            mListener = (OnButtonClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnButtonClickedListener ");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.show_recipe, container, false);

        // Set click listener for the buttons in the xml.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_from_show_recipe);
        backButton.setOnClickListener(clickListener);

        mRecipeNameView = (TextView) rootView.findViewById(R.id.recipe_name_show_recipe);
        mRecipeInfoView = (TextView) rootView.findViewById(R.id.recipe_directions_show_recipe);
        return rootView;
    }

    // Click listener for the buttons in the xml (Back button and share recipe button)
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onButtonClicked(view);
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        JSONObject recipe = null;
        String recipeName = null;
        String recipeInformation= null;

        String recipeString = getArguments().getString("recipe");
        try {
            recipe = new JSONObject(recipeString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (recipe != null) {
            try {
                recipeName = recipe.getString("recipe_name");
                recipeInformation = recipe.getString("recipe_information");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (recipeName != null && recipeInformation != null){
            mRecipeInfoView.setText(recipeInformation);
            mRecipeNameView.setText(recipeName);
        }
    }
}
