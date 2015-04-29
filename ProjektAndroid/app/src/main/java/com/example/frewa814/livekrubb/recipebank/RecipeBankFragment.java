package com.example.frewa814.livekrubb.recipebank;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.frewa814.livekrubb.R;

/**
 * Created by Fredrik on 2015-04-24.
 */
public class RecipeBankFragment extends Fragment {

    OnButtonClickedListener mListener;

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
        View rootView = inflater.inflate(R.layout.recipe_bank, container, false);

        // Set button listener for the recipe bank button and toplist button.
        Button recipeBankButton = (Button) rootView.findViewById(R.id.recipe_bank_button);
        Button toplistButton = (Button) rootView.findViewById(R.id.toplist_button);
        recipeBankButton.setOnClickListener(clickListener);
        toplistButton.setOnClickListener(clickListener);

        return rootView;
    }

    // When a button is clicked, notify the activity.
    // MainActivity will then create the new fragment.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            mListener.onButtonClicked(view);
        }
    };
}
