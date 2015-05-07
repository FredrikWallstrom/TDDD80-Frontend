package com.example.frewa814.livekrubb.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;

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
 * A register screen that offer one user to register one account so he can use the application.
 */
public class RegisterActivity extends Activity {

    /**
     * Constant tags for http requests.
     */
    private static final String RESULT_TAG = "result";

    /**
     * EditText views in the xml.
     */
    private EditText mUsernameView;
    private EditText mEmailView;
    private EditText mPasswordView;

    /**
     * This views is used to shoe the progress vies and than hide the register form view
     * and vice versa.
     */
    private View mProgressView;
    private View mRegisterForm;

    /**
     * Strings that will represent the entered information from the user.
     */
    private String mUsername;
    private String mEmail;
    private String mPassword;

    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Hide the keyboard when switch to this activity
        hideKeyboard();

        // Set up the login form.
        mRegisterForm = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        // Find the EditText views in the xml.
        mUsernameView = (EditText) findViewById(R.id.register_username);
        mEmailView = (EditText) findViewById(R.id.register_email);
        mPasswordView = (EditText) findViewById(R.id.register_password);

        // Find the backButton in the xml and set a click listener on it.
        // When the user click on the button, switch back to login activity.
        Button mBackButton = (Button) findViewById(R.id.back_from_register_button);
        mBackButton.setOnClickListener(clickListener);

        // Find the registerButton in the xml and set a click listener on it.
        // When the user click on the button, try to do a register attempt.
        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(clickListener);
    }

    /**
     * Click listener for register and back button.
     * Checks which button is clicked and than do the right thing.
     */
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Attempt to register one new user.
            if (view.getId() == R.id.register_button) {
                attemptRegister();

            }
            // Change back to the login screen.
            else if (view.getId() == R.id.back_from_register_button) {
                finish();
                Intent loginScreen = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginScreen);
            }
        }
    };

    /**
     * Calling this when i want to hide the keyboard.
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Attempts to register one user.
     * If there are form errors (invalid email, missing fields, etc.),
     * the errors are presented for the user and no actual register attempt is made.
     */
    public void attemptRegister() {
        // Flag to know if we want to cancel the attempt or not.
        boolean cancel = false;

        // Reset the focusView.
        View focusView = null;

        // Reset errors.
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        // Check for a valid password, if the user entered one.
        if (!isInputValid(mPassword)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (!isEmailValid(mEmail)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid username, if the user entered one.
        if (!isInputValid(mUsername)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt to register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask();
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Check if the input username and password is valid.
     * @param input is username or password.
     * @return boolean true or false depend if the input is valid or not.
     */
    private boolean isInputValid(String input) {
        return input.length() > 4 && !input.contains(" ");
    }

    /**
     * Check if the input email is valid.
     * @param email is the entered email address from user.
     * @return boolean true or false depend if the email is valid or not.
     */
    private boolean isEmailValid(String email) {
        return email.length() > 4 && email.contains("@") && !email.contains(" ");
    }

    /**
     * Shows the progress UI and hides the register form
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Private anonymous class that will do one asyncTask so we can handle the http request
     * and add the user to the database.
     */
    private class UserRegisterTask extends AsyncTask<Void, Void, String> {

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
            String dict = makePost(MainActivity.URL + "/add_user");

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
            mAuthTask = null;
            showProgress(false);

            // Check what the post to the server returned and inform the user if something went wrong.
            switch (result) {
                case "server error":
                    Toast serverError = Toast.makeText(getApplicationContext(), "Server Error, please try again!", Toast.LENGTH_LONG);
                    serverError.show();
                    break;
                case "email already exists":
                    mEmailView.setError(getString(R.string.error_email_exists));
                    mEmailView.requestFocus();
                    break;
                case "username already exists":
                    mUsernameView.setError(getString(R.string.error_username_exists));
                    mUsernameView.requestFocus();
                    break;
                default:
                    // default is that the registration has succeed, switch activity back to the login screen.
                    Toast succeed = Toast.makeText(getApplicationContext(), "The registration has succeed!", Toast.LENGTH_LONG);
                    succeed.show();
                    finish();
                    Intent loginScreen = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginScreen);
                    break;
            }
        }

        /**
         * Do a http request and make a post to the database with the user information.
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
                jsonObject.accumulate("username", mUsername);
                jsonObject.accumulate("email", mEmail);
                jsonObject.accumulate("password", mPassword);
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
         * Convert the inputStream to a readable string.
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

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
