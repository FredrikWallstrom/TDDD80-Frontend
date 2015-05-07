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

import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A login screen that offers login via username and password.
 */
public class LoginActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    /**
     * UI references
     */
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    /**
     * Constant tags for http requests.
     */
    private final static String USER_TAG = "user";
    private final static String PASSWORD_TAG = "password";

    private String mUsername;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout to activity login.
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Find the views for username and password.
        mUsernameView = (EditText) findViewById(R.id.usernameField);
        mPasswordView = (EditText) findViewById(R.id.password);

        // Find the signInButton in the xml and set a click listener on it.
        // When the user click on the button, try to do a login.
        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(clickListener);

        // Find the registerButton in the xml and set a click listener on it.
        // When a user click on the button, change activity to registerActivity.
        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(clickListener);
    }

    /**
     * Click listener for register and sign in button.
     * Checks which button is clicked and than do the right thing.
     */
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Attempt to login.
            if (view.getId() == R.id.sign_in_button){
                attemptLogin();
            }
            // Change to register activity.
            else if (view.getId() == R.id.register_button){
                finish();
                Intent registerScreen = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerScreen);
            }
        }
    };

    /**
     * Attempts to login.
     * If there are form errors (invalid email, missing fields, etc.),
     * the errors are presented for the user and no actual login attempt is made.
     */
    public void attemptLogin() {
        // First of all we hide the keyboard so the user see whats happening.
        hideKeyboard();

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (!isInputValid(mPassword)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (!isInputValid(mUsername)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask();
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
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

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
     * Represents an asynchronous login task used to authenticate the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            JSONObject jsonObject;
            JSONObject object;
            String user;

            // Get the user from the database.
            user = getUser(mUsername);

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                // If we don't got any return from the database (length of array is 0), the user enter wrong username
                jsonObject = new JSONObject(user);
                JSONArray jsonArray = jsonObject.getJSONArray(USER_TAG);
                if (jsonArray.length() == 0) {
                    return "wrong username";

                }
                // Check if the user entered right password.
                object = jsonArray.getJSONObject(0);
                if (mPassword.equals(object.getString(PASSWORD_TAG))) {
                    // The user entered right password so then we can create a new object
                    // ActivatedUser so I know who is logged in.
                    new ActivatedUser(object);
                } else {
                    return "wrong password";
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return "server error";
            }
            return "success";
        }

        @Override
        protected void onPostExecute(final String result) {
            mAuthTask = null;
            showProgress(false);

            switch (result) {
                case "server error":
                    // Inform the user that something is wrong with the server.
                    Toast serverError = Toast.makeText(getApplicationContext(), "Server Error, please try again!", Toast.LENGTH_LONG);
                    serverError.show();
                    break;
                case "success":
                    // Switch activity to MainActivity.
                    finish();
                    Intent intent = new Intent(getApplicationContext(), LoadingScreenActivity.class);
                    intent.putExtra("START_ACTIVITY", "MainActivity");
                    startActivity(intent);
                    break;
                case "wrong username":
                    mUsernameView.setError(getString(R.string.error_incorrect_username));
                    mUsernameView.requestFocus();
                    break;
                default:
                    // Default is wrong password.
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
            }
        }

        /**
         * This method will do one http request and get the user from the database
         * with a given username that the user entered.
         */
        private String getUser(String username) {
            InputStream inputStream;
            String result;
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(MainActivity.URL + "/get_user/" + username);
                HttpResponse response;
                response = httpClient.execute(httpget);

                inputStream = response.getEntity().getContent();
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
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line;
            String result = "";
            while((line = bufferedReader.readLine()) != null)
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



