package com.example.frewa814.livekrubb.misc;

import android.view.View;

import org.json.JSONObject;

/**
 * Interface that will be implemented by MainActivity.
 * The activity is interested in buttons click and will handle the fragment transactions.
 */
public interface OnButtonClickedListener {
    public void onButtonClicked(View view);
    public void onTaskDone();
    public void onShowRecipeButtonClicked(JSONObject recipe);
    public void onCommentButtonClicked(String postId);
    public void onMyPageClicked(String user_id);
}
