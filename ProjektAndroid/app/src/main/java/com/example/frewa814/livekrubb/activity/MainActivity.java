package com.example.frewa814.livekrubb.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.adapters.SearchUserAdapter;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.comment.CommentFragment;
import com.example.frewa814.livekrubb.flow.FollowersFlowListFragment;
import com.example.frewa814.livekrubb.flow.PublicFlowFragment;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.recipe.PersonalToplistFragment;
import com.example.frewa814.livekrubb.recipe.ShareRecipeFragment;
import com.example.frewa814.livekrubb.recipe.ShowRecipeFragment;
import com.example.frewa814.livekrubb.flow.MyPageFragment;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;
import com.example.frewa814.livekrubb.recipe.RecipeBankFragment;
import com.example.frewa814.livekrubb.recipe.TopListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity implements OnButtonClickedListener {

    /**
     * Constant fields that is needed in the moste classes and
     * therefore this fields is static.
     * We will load the allPosts field when we creating the activity.
     */
    public final static String URL = "http://livekrubb-frewa814.openshift.ida.liu.se";
    public static JSONArray allPosts;

    /**
     * Constant tags for http requests.
     */
    private static final String USER_TAG = "users";
    private static final String USERNAME_TAG = "username";
    private static final String POST_TAG = "posts";
    private static final String ID_TAG = "id";

    /**
     * Constant for the timer.
     */
    private static final int WAIT_TIME = 2500;

    /**
     * Fields for tha actionbar menu and menu items.
     */
    private MenuItem menuItem;
    private MenuItem mSearchMenuItem;
    private Menu menu;

    /**
     * All users will be represented in this JSONArray.
     * It will be loaded when creating the activity.
     */
    private JSONArray allUsers;

    /**
     * All searched user will be presented in this List.
     * For the searchItem in the actionbar.
     */
    private List<JSONObject> searchedUsers;

    /**
     * Fields for the shake sensor.
     */
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    /**
     * This method will run at the beginning when i change to this activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Init the activity_main layout.
        setContentView(R.layout.activity_main);

        // Init the Shake Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // Init all registered users (get them from the database).
        allUsers = getAllUsers();
        // Init all posts (get them from the database).
        allPosts = getPosts();
        // Sort the posts by timestamp.
        allPosts = getPostsSorted();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        // and load the PublicFlowFragment as first fragment.
        if (findViewById(R.id.fragment_container) != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            PublicFlowFragment updatedPublicFlowFragment = new PublicFlowFragment();
            ft.add(R.id.fragment_container, updatedPublicFlowFragment);
            ft.commit();
        }
    }

    /**
     * This method is handling the actionbar and inflate it.
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Find the searchItem in the actionbar.
            mSearchMenuItem = menu.findItem(R.id.action_search);

            // Add one listener for the searchItem in the actionbar.
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                /**
                 * This method will run when the user click
                 * on the search or "ok" button in the keyboard when he is searching.
                 */
                @Override
                public boolean onQueryTextSubmit(String query) {
                    String user_id = null;
                    List<JSONObject> searchedUserNames = new ArrayList<>();

                    // Hide the keyboard and collapse the actionView.
                    hideKeyboard();
                    mSearchMenuItem.collapseActionView();

                    if (allUsers != null) {
                        // Go through all registered users.
                        for (int i = 0; i < allUsers.length(); i++) {
                            try {
                                // Take out the username for every registered user.
                                JSONObject jsonObject = allUsers.getJSONObject(i);
                                String username = jsonObject.getString(USERNAME_TAG);
                                // Check if one of the username match the query that the user
                                // have entered before he clicked on search.
                                if (username.toLowerCase().contains(query.toLowerCase())) {
                                    searchedUserNames.add(jsonObject);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    // Check if we added someone to the temp list.
                    if (searchedUserNames.size() != 0){
                        try {
                            // Get the first user that is represented in the temp list.
                            JSONObject jsonObject = searchedUserNames.get(0);
                            user_id = jsonObject.getString(ID_TAG);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // Check if we found some user id.
                    if (user_id != null) {
                        // Change to the profileFragment (MyPageFragment) for the first user
                        // in the temp list.
                        onButtonClicked(user_id, "MyPageFragment");
                        return true;
                    }
                    return false;
                }

                /**
                 * This method will run every time the user click on one letter on the keyboard
                 * and then will it load a new list that will match with the entered query.
                 */
                @Override
                public boolean onQueryTextChange(String query) {
                    loadNewList(query);
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Load the dropdown menu with the names that fit the query input.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadNewList(String query) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // SearchedUser list that will save the JSONObject if we got a match.
            searchedUsers = new ArrayList<>();
            // Temp list that will save all matched users as Strings with userNames.
            List<String> searchedUserNames = new ArrayList<>();

            if (allUsers != null) {
                // Go through the users.
                for (int i = 0; i < allUsers.length(); i++) {
                    try {
                        // Take out the username for every user in allUsers.
                        JSONObject jsonObject = allUsers.getJSONObject(i);
                        String username = jsonObject.getString(USERNAME_TAG);
                        // Check if the one of the user match the entered query.
                        // If it does, add it to the temp list.
                        if (username.toLowerCase().contains(query.toLowerCase())) {
                            searchedUsers.add(jsonObject);
                            searchedUserNames.add(username);
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            // Cursor
            String[] columns = new String[]{"_id", "text"};
            Object[] temp = new Object[]{0, "default"};
            MatrixCursor cursor = new MatrixCursor(columns);

            // Go through the list with matches and add it to the cursor.
            for (int i = 0; i < searchedUserNames.size(); i++) {
                temp[0] = i;
                temp[1] = searchedUserNames.get(i);
                cursor.addRow(temp);
            }
            // Display the searchView
            getSystemService(Context.SEARCH_SERVICE);
            final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setSuggestionsAdapter(new SearchUserAdapter(this, cursor, searchedUserNames));
            search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                /**
                 * This method will run when the user click on one item in the  drop down list (cursor)
                 * When the user click on one item it will load the right profilePage (MyPageFragment).
                 */
                @Override
                public boolean onSuggestionClick(int position) {
                    String user_id = null;

                    // Control that we found some users that match the search.
                    if (searchedUsers != null) {
                        try {
                            // Get the right user that have been clicked on in the cursor.
                            JSONObject jsonObject = searchedUsers.get(position);
                            user_id = jsonObject.getString(ID_TAG);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // If we found the user, change to the users profilePage (MyPageFragment)
                        if (user_id != null) {
                            // Hide the keyboard and collapse the searchView.
                            hideKeyboard();
                            mSearchMenuItem.collapseActionView();
                            onButtonClicked(user_id, "MyPageFragment");
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }


    /**
     * This method will handle the actionbar item clicks
     * except for the search action that got one own listener.
     * Depend on what action that have been clicked,
     * it will display the right fragment via, fragment transaction.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Just in case if the keyboard are up, we will hide it.
        hideKeyboard();

        // Handle action bar item clicks.
        switch (item.getItemId()) {
            // Case Refresh button, call the refreshFragment method.
            case R.id.action_refresh:
                refreshFragment(item);
                break;

            // Case Logout button, change to the login activity via LoadingScreenActivity.
            case R.id.action_logOut:
                finish();
                Intent intent = new Intent(getApplicationContext(), LoadingScreenActivity.class);
                intent.putExtra("START_ACTIVITY", "LoginActivity");
                startActivity(intent);
                break;

            // Case My page button, we let the onButtonClicked method call the
            // MyPageFragment.
            case R.id.action_my_page:
                onButtonClicked(ActivatedUser.activatedUserID, "MyPageFragment");
                break;

            // Case Recipe bank button, change fragment to recipeBankFragment.
            case R.id.action_recipe_bank:
                RecipeBankFragment recipeBankFragment = new RecipeBankFragment();
                ft.replace(R.id.fragment_container, recipeBankFragment);
                ft.commit();
                break;

            // Case PublicFlowList button, change to PublicFlowFragment.
            case R.id.action_public_flow:
                PublicFlowFragment publicFlowFragment = new PublicFlowFragment();
                ft.replace(R.id.fragment_container, publicFlowFragment);
                ft.commit();
                break;

            // Case FollowersFlowList button, change to the FollowersFlowFragment.
            case R.id.action__followers_flow:
                FollowersFlowListFragment followersFlowListFragment = new FollowersFlowListFragment();
                ft.replace(R.id.fragment_container, followersFlowListFragment);
                ft.commit();
                break;
        }
        return true;
    }

    /**
     * This method will start a progressbar instead of the refresh button in the actionbar.
     * At the moment the progressbar is spinning we got one timer (handler)
     * When the handler is done we will reset the progressbar to the icon and
     * call the right refresh method depend on which fragment the user want to refresh.
     * There are only four fragment that is refreshable and they got an method that will do the refresh.
     */
    private void refreshFragment(MenuItem item) {
        // Start a progressbar instead of the update icon.
        menuItem = item;
        menuItem.setActionView(R.layout.progressbar_refreshbutton);
        menuItem.expandActionView();

        // Handler that wait for 2,5 sec, and than execute the new fragment.
        // Just to inform the users that's something happening.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Get the fragment that the user are in then he click on the refresh button.
                Fragment oldFragment = getFragmentManager().findFragmentById(R.id.fragment_container);

                // Check if the activated fragment was the MyPageFragment.
                if (oldFragment instanceof MyPageFragment){
                    ((MyPageFragment) oldFragment).refresh();
                }

                // Check if the activated fragment was the FollowersFlowListFragment.
                if (oldFragment instanceof FollowersFlowListFragment) {
                    ((FollowersFlowListFragment) oldFragment).refresh();
                }

                // Check if the activated fragment was the PublicFlowFragment.
                if (oldFragment instanceof PublicFlowFragment) {
                    ((PublicFlowFragment) oldFragment).refresh();
                }
                // Check if the activated fragment was the RecipeBankFragment.
                if (oldFragment instanceof RecipeBankFragment) {
                    ((RecipeBankFragment) oldFragment).refresh();
                }

                // Stop the progressbar and return the update icon.
                menuItem.collapseActionView();
                menuItem.setActionView(null);
            }
        }, WAIT_TIME);
    }

    /**
     * I Implemented an own sort method that's is sorting the posts by the timestamp column.
     */
    private JSONArray getPostsSorted() {
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < allPosts.length(); i++)
            try {
                jsonValues.add(allPosts.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                String valA = "";
                String valB = "";
                try {
                    valA = lhs.getString("timestamp");
                    valB = rhs.getString("timestamp");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int comp = valA.compareTo(valB);
                if (comp > 0)
                    return -1;
                if (comp < 0)
                    return 1;
                return 0;
            }
        });
        return new JSONArray(jsonValues);
    }

    /**
     * This method will hide the keyboard if it is visible.
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * This method will call on the GetTask and get all registered users from the database.
     */
    private JSONArray getAllUsers() {
        String allUsers;
        JSONObject jsonObject;
        JSONArray jsonArray = null;

        try {
            allUsers = new GetTask().execute(MainActivity.URL + "/all_users").get();
        } catch (InterruptedException | ExecutionException e) {
            allUsers = "server error";
            e.printStackTrace();
        }
        if (!allUsers.equals("server error")) {
            try {
                jsonObject = new JSONObject(allUsers);
                jsonArray = jsonObject.getJSONArray(USER_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }


    /**
     * Handle the buttons click from recipe bank fragment and topList fragment to display the right fragment.
     * It's like tabs.
     * It will also handle the button clicks on the back button and the share recipe button.
     *
     * This method is an Override from the OnButtonClickListener interface.
     *
     * @param view got the information on which fragment there is to display.
     */
    @Override
    public void onButtonClicked(View view) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Hide the keyboard.
        hideKeyboard();

        // Switch to replace the fragment with the right one.
        switch (view.getId()) {
            // Case topList button in the RecipeBankFragment.
            // Change to the TopListFragment.
            case R.id.toplist_button:
                TopListFragment topListFragment = new TopListFragment();
                ft.replace(R.id.fragment_container, topListFragment);
                ft.commit();
                break;

            // Case recipe bank button in the topListFragment.
            // Change to the RecipeBankFragment.
            case R.id.recipe_bank_button:
                RecipeBankFragment recipeFragment = new RecipeBankFragment();
                ft.replace(R.id.fragment_container, recipeFragment);
                ft.commit();
                break;

            // Case share recipe button.
            // Change to the ShareRecipeFragment.
            case R.id.share_recipe_button:
                // Change to ShareRecipeFragment if we go from FlowFragment or CreateRecipeFragment.
                ShareRecipeFragment shareRecipeFragment = new ShareRecipeFragment();
                ft.replace(R.id.fragment_container, shareRecipeFragment);
                ft.addToBackStack(null);
                ft.commit();
                break;

            // Case back button, just do a normal onBackPressed.
            case R.id.back_button:
                onBackPressed();
                break;
        }
    }

    /**
     * This method is an Override from the OnButtonClickListener interface.
     *
     * This is called when the AsyncTask is done in the ShareRecipeFragment.
     * It will replace the current fragment (ShareRecipeFragment)
     * and replace it with the previous fragment there the user was before ShareRecipeFragment.
     */
    @Override
    public void onTaskDone() {
        // Show the actionbar.
        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Get the fragment that the user are in then he click on the refresh button.
        Fragment oldFragment = getFragmentManager().findFragmentById(R.id.fragment_container);

        // Check if the user clicked on ShareRecipe button from the public flow
        // or followers flow fragment.
        if (oldFragment instanceof PublicFlowFragment){
            PublicFlowFragment publicFlowFragment = new PublicFlowFragment();
            ft.replace(R.id.fragment_container, publicFlowFragment);
        }
        else {
            FollowersFlowListFragment followersFlowListFragment = new FollowersFlowListFragment();
            ft.replace(R.id.fragment_container, followersFlowListFragment);

        }
        ft.commit();
    }

    /**
     * This method is an Override from the OnButtonClickListener interface.
     *
     * This will be called when we gonna show one recipe for the user.
     */
    @Override
    public void onShowRecipeButtonClicked(JSONObject recipe) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Send an bundle with fragment transaction that is gonna represent the ShowRecipeFragment.
        Bundle details = new Bundle();
        String recipeString = recipe.toString();
        details.putString("recipe", recipeString);

        ShowRecipeFragment showRecipeFragment = new ShowRecipeFragment();
        ft.replace(R.id.fragment_container, showRecipeFragment);
        showRecipeFragment.setArguments(details);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * This method is an Override from the OnButtonClickListener interface.
     *
     * This method will take one id as a string and the next fragment that will
     * be displayed as inputs and that is the difference from the other onButtonClicked method.
     *
     * This method is called when a user want to see the personal topList, comments on a post
     * or to see the profilePage of one user.
     */
    @Override
    public void onButtonClicked(String id, String nextFragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Send a bundle with the fragment transaction.
        // Need to send the id so we know what we gonna display.
        // This id can be either user_id or post_id depends on which fragment we gonna display.
        Bundle bundle = new Bundle();
        bundle.putString("id", id);

        switch (nextFragment) {

            // Case PersonalTopListFragment, here we send the user id so we know which
            // toplist we gonna get from the database.
            case "PersonalToplistFragment":
                PersonalToplistFragment personalToplist = new PersonalToplistFragment();
                ft.replace(R.id.fragment_container, personalToplist);
                personalToplist.setArguments(bundle);
                ft.addToBackStack(null);
                ft.commit();
                break;

            // Case CommentFragment, here we send the post id so we can get all comments on this post.
            case "CommentFragment":
                CommentFragment commentFragment = new CommentFragment();
                ft.replace(R.id.fragment_container, commentFragment);
                commentFragment.setArguments(bundle);
                ft.addToBackStack(null);
                ft.commit();
                break;

            // Case MyPageFragment, here we send user id so we know who we gonna display on pMyPageFragment.
            case "MyPageFragment":
                MyPageFragment myPageFragment = new MyPageFragment();
                ft.replace(R.id.fragment_container, myPageFragment);
                myPageFragment.setArguments(bundle);
                ft.commit();
                break;
        }
    }

    /**
     * Get all posts from the database.
     */
    private JSONArray getPosts() {
        String result;
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            result = new GetTask().execute(MainActivity.URL + "/all_posts").get();
        } catch (InterruptedException | ExecutionException e) {
            result = "server error";
            e.printStackTrace();
        }

        if (!result.equals("server error")) {
            try {
                jsonObject = new JSONObject(result);
                jsonArray = jsonObject.getJSONArray(POST_TAG);
                return jsonArray;
            } catch (JSONException e) {
                return new JSONArray();
            }
        } else {
            return null;
        }

    }

    /**
     * ShakeListener that is waiting for shakes and will be called every time a user
     * shakes hes phone. It doesn't matter where the user is in the application.
     * He can shake it from every fragment.
     */
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > 15) {
                Random rand = new Random();

                JSONArray jsonArray = allPosts;

                int randomNum = rand.nextInt(jsonArray.length());

                try {
                    onShowRecipeButtonClicked(jsonArray.getJSONObject(randomNum));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * Activate the ShakeListener.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Pause the ShakeListener.
     */
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}






