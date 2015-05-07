package com.example.frewa814.livekrubb.recipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
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
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.gps.Constants;
import com.example.frewa814.livekrubb.gps.FetchAddressIntentService;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

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
 * You can also add your location to the post so this class will also have some gps methods.
 */
public class ShareRecipeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Constants.
     */
    private static final String RESULT_TAG = "result";

    /**
     * Click listener for MainActivity.
     */
    OnButtonClickedListener mListener;

    /**
     * This fields is for the gps workout.
     */
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private String mAddressOutput;
    private boolean mLocationRequested;

    /**
     * Input strings by the user.
     */
    private String mRecipeName;
    private String mRecipeInformation;
    private String mPostInformation;
    private String mLocationInformation;

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

    /**
     * Text view for the location view.
     */
    private TextView mLocationView;

    /**
     * Runs first when fragment is created.
     */
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

    /**
     * This will run as second method when creating fragment.
     / This will inflate the right xml and find the needed buttons and text views etc.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Need to connect to the GoogleApiService.
        buildGoogleApiClient();

        // At the beginning the user don't want to add the location.
        mLocationRequested = false;

        // Setting address output to "".
        mAddressOutput = "";

        // Make an new ResultReceiver object.
        mResultReceiver = new AddressResultReceiver(new android.os.Handler());

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.share_recipe, container, false);

        // Find the views in the share_recipe xml.
        mPostForm = rootView.findViewById(R.id.share_recipe_form);
        mProgressView = rootView.findViewById(R.id.share_progress);

        // Set click listener for the buttons in the share_recipe xml.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_button);
        Button shareButton = (Button) rootView.findViewById(R.id.share_recipe_button);
        Button mFetchAddressButton = (Button) rootView.findViewById(R.id.fetch_adress_button);
        shareButton.setOnClickListener(clickListener);
        backButton.setOnClickListener(clickListener);
        mFetchAddressButton.setOnClickListener(clickListener);

        // Find the TextViews in the xml where the user should add information about the recipe.
        mRecipeNameView = (EditText) rootView.findViewById(R.id.recipe_name_field_share_recipe);
        mRecipeInfoView = (EditText) rootView.findViewById(R.id.recipe_information_share_recipe);
        mPostInfoView = (EditText) rootView.findViewById(R.id.post_information_share_recipe);
        mLocationView = (TextView) rootView.findViewById(R.id.location_textview_share_recipe);

        // Set focusable on views to true, so we can set focus on them if the input is wrong.
        mRecipeNameView.setFocusable(true);
        mPostInfoView.setFocusable(true);
        mRecipeInfoView.setFocusable(true);

        return rootView;
    }

    // Click listener for the buttons in the xml (Back button, share recipe button and fetchAddressButton).
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Check if the user clicked on fetchAdressButton.
            if (view.getId() == R.id.fetch_adress_button){
                final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

                // Check if the gps is activated. If the gps is not active tell the user with a toast.
                // Else set the LocationRequested flag to true and connect to the service.
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER ) ) {
                    Toast noGps = Toast.makeText(getActivity(), "Your GPS is don't activated. Activate it and try again.", Toast.LENGTH_LONG);
                    noGps.show();
                }else{
                    mLocationRequested = true;
                    mGoogleApiClient.connect();
                }

            }
            // Check if the user clicked on the share recipe button.
            else if (view.getId() == R.id.share_recipe_button) {
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

                // Check if the user added a location to his recipe or not.
                if (mLocationRequested){
                    mLocationInformation = mLocationView.getText().toString();
                }else{
                    mLocationInformation = "Not added";
                }

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
            }
            // The user clicked on the back button and then we pass the click to the MainActivity via interface listener.
            else{
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
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            if (!Geocoder.isPresent()) {
                return;
            }
            startIntentService();
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        getActivity().startService(intent);
    }

    private void displayAddressOutput() {
        mLocationView.setText(mAddressOutput);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(android.os.Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();
        }
    }


    /**
     * Private anonymous class that will do one asyncTask so we can handle the httpRequest.
     * This class will make a call to the server so we can add the post/recipe to the database.
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
                jsonObject.accumulate("user_id", ActivatedUser.activatedUserID);
                jsonObject.accumulate("location", mLocationInformation);
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
    }
}



