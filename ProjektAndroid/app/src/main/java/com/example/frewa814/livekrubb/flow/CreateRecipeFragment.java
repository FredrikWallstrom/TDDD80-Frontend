package com.example.frewa814.livekrubb.flow;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.recipebank.OnButtonClickedListener;


/**
 * Created by Fredrik on 2015-04-28.
 */
public class CreateRecipeFragment extends Fragment {

    OnButtonClickedListener mListener;
    private EditText mRecipeNameView;
    private EditText mRecipeInformationView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

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
        View rootView = inflater.inflate(R.layout.create_recipe, container, false);

        // Set click listener on the back button and confirmRecipeButton.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_from_create_recipe);
        Button confirmRecipeButton = (Button) rootView.findViewById(R.id.confirm_recipe_button);
        backButton.setOnClickListener(clickListener);
        confirmRecipeButton.setOnClickListener(clickListener);

        mRecipeNameView = (EditText) rootView.findViewById(R.id.recipe_name_field_create_recipe);
        mRecipeInformationView = (EditText) rootView.findViewById(R.id.recipe_information_field_create_recipe);

        return rootView;
    }

    // When the back button is clicked, notify the activity.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.confirm_recipe_button){
                String name = mRecipeNameView.getText().toString();
                String information = mRecipeInformationView.getText().toString();

                mListener.passRecipeData(name, information);
            }
            mListener.onButtonClicked(view);
        }
    };
}
