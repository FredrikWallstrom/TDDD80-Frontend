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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.comment.CommentFragment;
import com.example.frewa814.livekrubb.flow.FollowersFlowListFragment;
import com.example.frewa814.livekrubb.flow.PublicFlowFragment;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
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
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity implements OnButtonClickedListener {

    public final static String URL = "http://livekrubb-frewa814.openshift.ida.liu.se";
    private static final String USER_TAG = "users";
    private static final String USERNAME_TAG = "username";
    private static final String ID_TAG = "id";
    private MenuItem menuItem;
    private static final int WAIT_TIME = 2500;
    private Menu menu;
    private JSONArray allUsers;
    private MenuItem mSearchMenuItem;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allUsers = getAllUsers();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            PublicFlowFragment updatedPublicFlowFragment = new PublicFlowFragment();
            ft.add(R.id.fragment_container, updatedPublicFlowFragment);
            ft.commit();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        this.menu = menu;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            mSearchMenuItem = menu.findItem(R.id.action_search);

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    String user_id = null;
                    List<JSONObject> searchedUserNames = new ArrayList<>();

                    hideKeyboard();
                    mSearchMenuItem.collapseActionView();

                    if (allUsers != null) {
                        // Check if the one of the liker is the activated person.
                        for (int i = 0; i < allUsers.length(); i++) {
                            try {
                                JSONObject jsonObject = allUsers.getJSONObject(i);
                                String username = jsonObject.getString(USERNAME_TAG);
                                if (username.toLowerCase().contains(query.toLowerCase())) {
                                    searchedUserNames.add(jsonObject);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    try {
                        JSONObject jsonObject = searchedUserNames.get(0);
                        user_id = jsonObject.getString(ID_TAG);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (user_id != null) {
                        onMyPageClicked(user_id);
                        return true;
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    loadHistory(query);
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Handle action bar item clicks.

        hideKeyboard();

        switch (item.getItemId()) {
            // Case Refresh button.
            case R.id.action_refresh:
                refreshFragment(item);
                break;

            // Case Logout button.
            case R.id.action_logOut:
                finish();
                Intent intent = new Intent(getApplicationContext(), LoadingScreenActivity.class);
                intent.putExtra("START_ACTIVITY", "LoginActivity");
                startActivity(intent);
                break;

            // Case My page button.
            case R.id.action_my_page:
                onMyPageClicked(ActivatedUser.activatedUserID);
                break;

            // Case Recipe bank button.
            case R.id.action_recipe_bank:
                RecipeBankFragment recipeBankFragment = new RecipeBankFragment();
                ft.replace(R.id.fragment_container, recipeBankFragment);
                ft.commit();
                break;

            // Case Flow list button. Change to FlowFragment.
            case R.id.action_public_flow:
                PublicFlowFragment publicFlowFragment = new PublicFlowFragment();
                ft.replace(R.id.fragment_container, publicFlowFragment);
                ft.commit();
                break;

            case R.id.action__followers_flow:
                FollowersFlowListFragment followersFlowListFragment = new FollowersFlowListFragment();
                ft.replace(R.id.fragment_container, followersFlowListFragment);
                ft.commit();
                break;
        }
        return true;
    }

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
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Fragment oldFragment = getFragmentManager().findFragmentById(R.id.fragment_container);

                if (oldFragment instanceof FollowersFlowListFragment) {
                    FollowersFlowListFragment updatedFollowersListFragment = new FollowersFlowListFragment();
                    ft.replace(R.id.fragment_container, updatedFollowersListFragment);
                    ft.commit();
                }

                // Check if the activated fragment was the FlowFragment.
                if (oldFragment instanceof PublicFlowFragment) {
                    // Replace the old FlowFragment with a new one, my type of updating a fragment.
                    PublicFlowFragment updatedPublicFlowFragment = new PublicFlowFragment();
                    ft.replace(R.id.fragment_container, updatedPublicFlowFragment);
                    ft.commit();
                }
                // Check if the activated fragment was the RecipeFragment.
                if (oldFragment instanceof RecipeBankFragment) {
                    // Replace the old FlowFragment with a new one, my type of updating a fragment.
                    RecipeBankFragment updatedRecipeBankFragment = new RecipeBankFragment();
                    ft.replace(R.id.fragment_container, updatedRecipeBankFragment);
                    ft.commit();
                }

                // Stop the progressbar and return the update icon.
                menuItem.collapseActionView();
                menuItem.setActionView(null);
            }
        }, WAIT_TIME);
    }

    /**
     * Load the dropdown menu with the names that fit the query input.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadHistory(String query) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            List<String> searchedUserNames = new ArrayList<>();

        if (allUsers != null) {
            // Check if the one of the liker is the activated person.
            for (int i = 0; i < allUsers.length(); i++) {
                try {
                    JSONObject jsonObject = allUsers.getJSONObject(i);
                    String username = jsonObject.getString(USERNAME_TAG);
                    if (username.toLowerCase().contains(query.toLowerCase())) {
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

            for (int i = 0; i < searchedUserNames.size(); i++) {
                temp[0] = i;
                temp[1] = searchedUserNames.get(i);
                cursor.addRow(temp);
            }
            // SearchView
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setSuggestionsAdapter(new SearchUserAdapter(this, cursor, searchedUserNames));
            search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }


                @Override
                public boolean onSuggestionClick(int position) {
                    String user_id = null;

                    hideKeyboard();
                    mSearchMenuItem.collapseActionView();

                    if (allUsers != null){
                        try {
                            JSONObject jsonObject = allUsers.getJSONObject(position);
                            user_id = jsonObject.getString(ID_TAG);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (user_id != null){
                            onMyPageClicked(user_id);
                            return true;
                        }
                    }
                    return false;
                }
            });


        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

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
     * Handle the buttons click from recipe bank fragment and toplist fragment to display the right fragment.
     * It's like tabs.
     *
     * @param view got the information on which fragment there is to display.
     */
    @Override
    public void onButtonClicked(View view) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        hideKeyboard();
        // Switch to replace the fragment with the right one.
        switch (view.getId()) {
            case R.id.toplist_button:
                TopListFragment topListFragment = new TopListFragment();
                ft.replace(R.id.fragment_container, topListFragment);
                ft.commit();
                break;
            case R.id.recipe_bank_button:
                RecipeBankFragment recipeFragment = new RecipeBankFragment();
                ft.replace(R.id.fragment_container, recipeFragment);
                ft.commit();
                break;
            case R.id.share_recipe_button:
                // Change to ShareRecipeFragment if we go from FlowFragment or CreateRecipeFragment.
                ShareRecipeFragment shareRecipeFragment = new ShareRecipeFragment();
                ft.replace(R.id.fragment_container, shareRecipeFragment);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.back_button:
                onBackPressed();
                break;
        }
    }


    @Override
    public void onTaskDone() {
        // Show the actionbar.
        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        PublicFlowFragment publicFlowFragment = new PublicFlowFragment();
        ft.replace(R.id.fragment_container, publicFlowFragment);
        ft.commit();
    }

    @Override
    public void onShowRecipeButtonClicked(JSONObject recipe) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();


        Bundle details = new Bundle();
        String recipeString = recipe.toString();
        details.putString("recipe", recipeString);

        ShowRecipeFragment showRecipeFragment = new ShowRecipeFragment();
        ft.replace(R.id.fragment_container, showRecipeFragment);
        showRecipeFragment.setArguments(details);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onCommentButtonClicked(String postId, Fragment currentFragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Bundle bundle = new Bundle();
        bundle.putString("post_id", postId);

        CommentFragment commentFragment = new CommentFragment();
        ft.replace(R.id.fragment_container, commentFragment);
        commentFragment.setArguments(bundle);

        if (!(currentFragment instanceof CommentFragment)) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    @Override
    public void onMyPageClicked(String user_id) {
        hideKeyboard();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);

        MyPageFragment myPageFragment = new MyPageFragment();
        ft.replace(R.id.fragment_container, myPageFragment);
        myPageFragment.setArguments(bundle);
        ft.commit();

    }

    @Override
    public void onBackPressed() {
        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        super.onBackPressed();
    }
}






