package com.example.frewa814.livekrubb.flow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.login.LoginActivity;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.recipebank.OnButtonClickedListener;

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
import java.util.concurrent.ExecutionException;

/**
 * Created by Fredrik on 2015-04-27.
 */
public class ShareRecipeFragment extends Fragment {

    private static final String RESULT_TAG = "result";
    OnButtonClickedListener mListener;
    private Button mAddRecipeButton;
    private TextView mRecipeNameView;
    private String mRecipeName;
    private String mRecipeInformation;
    private EditText mPostInfoView;
    private String mPostInformation;
    private View mProgressView;
    private View mPostForm;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

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

        mPostForm = rootView.findViewById(R.id.share_recipe_form);
        mProgressView = rootView.findViewById(R.id.share_progress);

        // Set click listener for the buttons in the xml.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_from_share_recipe);
        mAddRecipeButton = (Button) rootView.findViewById(R.id.add_recipe_button);
        Button shareButton = (Button) rootView.findViewById(R.id.share_recipe_button);
        shareButton.setOnClickListener(clickListener);
        backButton.setOnClickListener(clickListener);
        mAddRecipeButton.setOnClickListener(clickListener);


        // Find the views.
        mRecipeNameView = (TextView) rootView.findViewById(R.id.recipe_name_field_share_recipe);
        mPostInfoView = (EditText) rootView.findViewById(R.id.post_information_share_recipe);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle data = getArguments();

        if (data != null) {
            mRecipeName = data.getString("name");
            mRecipeInformation = data.getString("information");
            mAddRecipeButton.setText("Edit recipe");
            mRecipeNameView.setText(mRecipeName);
        }
    }

    // When the back button is clicked, notify the activity.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.share_recipe_button) {
                // Hide the keyboard.
                hideKeyboard();

                // TODO Check if given information is valid.
                mPostInformation = mPostInfoView.getText().toString();
                new SharePostTask().execute((Void) null);
            }else{
                // TODO Check if the user want to edit recipe or add a new one.
                // TODO getText on button maybe will work.
                mListener.onButtonClicked(view);
            }
        }
    };

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

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

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

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

            // Try to register the new account
            String dict = makePost(MainActivity.URL + "/add_post");

            try {
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
        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}



