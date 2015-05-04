package com.example.frewa814.livekrubb.recipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class will show the fragment where you can share a recipe on the flow.
 * In other words, this class will handle how to make a post on the flow.
 */
public class ShareRecipeFragment extends Fragment {

    OnButtonClickedListener mListener;
    private static final String RESULT_TAG = "result";

    /**
     * Input strings by the user.
     */
    private String mRecipeName;
    private String mRecipeInformation;
    private String mPostInformation;

    /**
     * EditTextViews in the xml where the user should add the data about the recipe.
     */
    private EditText mRecipeNameView;
    private EditText mRecipeInfoView;
    private EditText mPostInfoView;

    /**
     * Views so i can handle handle the progressbar.
     */
    private View mProgressView;
    private View mPostForm;

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
        View rootView = inflater.inflate(R.layout.share_recipe, container, false);

        // Find the views that we want to hide or show when needed.
        mPostForm = rootView.findViewById(R.id.share_recipe_form);
        mProgressView = rootView.findViewById(R.id.share_progress);

        // Set click listener for the buttons in the xml.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_from_share_recipe);
        Button shareButton = (Button) rootView.findViewById(R.id.share_recipe_button);
        shareButton.setOnClickListener(clickListener);
        backButton.setOnClickListener(clickListener);


        // Find the editTextViews in the xml where the user should add information about the recipe.
        mRecipeNameView = (EditText) rootView.findViewById(R.id.recipe_name_field_share_recipe);
        mRecipeInfoView = (EditText) rootView.findViewById(R.id.recipe_information_share_recipe);
        mPostInfoView = (EditText) rootView.findViewById(R.id.post_information_share_recipe);

        // Set focusable on views to true.
        mRecipeNameView.setFocusable(true);
        mPostInfoView.setFocusable(true);
        mRecipeInfoView.setFocusable(true);

        return rootView;
    }

    // Click listener for the buttons in the xml (Back button and share recipe button)
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Check if the user clicked on the share recipe button.
            // If not, the user clicked on the back button and then we pass the click to the MainActivity.
            if (view.getId() == R.id.share_recipe_button) {
                // Hide the keyboard.
                hideKeyboard();

                // Reset the errors
                mRecipeInfoView.setError(null);
                mPostInfoView.setError(null);
                mRecipeInfoView.setError(null);

                // Get the information about the recipe that the user entered.
                mRecipeName = mRecipeNameView.getText().toString();
                mRecipeInformation = mRecipeInfoView.getText().toString();
                mPostInformation = mPostInfoView.getText().toString();

                // Check if the entered information is valid or not.
                // If it is valid, then do one http request to send the data to the database.
                // If it is not valid, setError on the views to inform the user about what was wrong.
                if (recipeNameIsValid(mRecipeName)){
                    if (infoIsValid(mRecipeInformation)){
                        if (infoIsValid(mPostInformation)){
                            mPostInformation = mPostInfoView.getText().toString();
                            new SharePostTask().execute((Void) null);
                        }else{
                            mPostInfoView.setError("Enter a description for your recipe!");
                            mPostInfoView.requestFocus();
                        }
                    }else{
                        mRecipeInfoView.setError("Enter directions for your recipe!");
                        mRecipeInfoView.requestFocus();
                    }
                }else{
                    mRecipeNameView.setError("Have you entered a recipe name? Check if it contains less than 30 letters.");
                    mRecipeNameView.requestFocus();
                }
            }else{
                hideKeyboard();
                mListener.onButtonClicked(view);
            }
        }
    };

    /**
     * Check if the entered recipe or post information is valid.
     */
    private boolean infoIsValid(String information) {
        return !information.trim().isEmpty() && !information.isEmpty();

    }

    /**
     * Check if the entered recipe name is valid.
     */
    private boolean recipeNameIsValid(String recipeName) {
        return recipeName.length() < 30 && !recipeName.trim().isEmpty() && !recipeName.isEmpty();
    }

    /**
     * Hides the keyboard.
     */
    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    /**
     * This method will hide or show the progressbar depend on the input boolean.
     * Input true will show the progressbar.
     * Input false will hide it and show the PostFormView.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mPostForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mPostForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPostForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mPostForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Do a http request and make a post to the database with the recipe information.
     */
    public String makePost(String url) {
        InputStream inputStream;
        String result;
        try {
            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // Make makePost request to the given URL
            HttpPost httpPost = new HttpPost(url);
            String json;
            // Build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("post_information", mPostInformation);
            jsonObject.accumulate("recipe_information", mRecipeInformation);
            jsonObject.accumulate("recipe_name", mRecipeName);
            jsonObject.accumulate("user_id",ActivatedUser.activatedUserID);
            // Convert JSONObject to JSON to String
            json = jsonObject.toString();
            // Set json to StringEntity
            StringEntity se = new StringEntity(json);
            // Set httpPost Entity
            httpPost.setEntity(se);
            // Execute makePost request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);
            // Receive response as inputStream.
            inputStream = httpResponse.getEntity().getContent();
            // Convert the inputStream to string.
            result = convertInputStreamToString(inputStream);

        } catch (Exception e) {
            result = "server error";
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Converting the result from the makePost method to a readable string.
     */
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    /**
     * Private anonymous class that will do one asyncTask so we can handle the httpRequest.
     */
    private class SharePostTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Try to add the post to the database.
            String dict = makePost(MainActivity.URL + "/add_post");

            try {
                // Get the result from the database and check if something went wrong.
                JSONObject jsonObject = new JSONObject(dict);
                result = jsonObject.getString(RESULT_TAG);
            } catch (JSONException e) {
                result = "server error";
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            showProgress(false);

            switch (result) {
                case "server error":
                    Toast serverError = Toast.makeText(getActivity(), "Server Error, please try again!", Toast.LENGTH_LONG);
                    serverError.show();
                    break;
                default:
                    mListener.onTaskDone();
                    break;
            }
        }
    }
}



