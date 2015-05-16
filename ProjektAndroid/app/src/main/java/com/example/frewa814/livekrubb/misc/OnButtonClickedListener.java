package com.example.frewa814.livekrubb.misc;

import android.view.View;

import org.json.JSONObject;

/**
 * Interface that will be implemented by MainActivity.
 * The activity is interested in buttons click and will handle the fragment transactions.
 */
public interface OnButtonClickedListener {
    void onButtonClicked(View view);
    void onTaskDone(String previousFragment);
    void onShowRecipeButtonClicked(JSONObject recipe);
    void onButtonClicked(String id, String nextFragment);
    void onShareRecipeButtonClicked(String previousFragment);
}
