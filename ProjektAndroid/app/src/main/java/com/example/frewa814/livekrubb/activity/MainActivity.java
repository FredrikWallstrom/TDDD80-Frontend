package com.example.frewa814.livekrubb.activity;

import android.annotation.TargetApi;
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
import android.widget.SearchView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.flow.FlowFragment;
import com.example.frewa814.livekrubb.mypage.MyPageFragment;
import com.example.frewa814.livekrubb.recipebank.OnButtonClickedListener;
import com.example.frewa814.livekrubb.recipebank.RecipeFragment;
import com.example.frewa814.livekrubb.recipebank.ToplistFragment;

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
    private MenuItem mSearchButton;
    private MenuItem menuItem;
    private static final int WAIT_TIME = 2500;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            FlowFragment updatedFlowFragment = new FlowFragment();
            ft.add(R.id.fragment_container, updatedFlowFragment);
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

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();

            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // TODO Här ska det ändras ifall användaren trycker på sök knappen så ska alla namn som ligger i listan skrivas ut.
                    return true;
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
        FragmentManager fm;
        FragmentTransaction ft;

        // Handle action bar item clicks.
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
                fm = getFragmentManager();
                ft = fm.beginTransaction();
                MyPageFragment myPageFragment = new MyPageFragment();
                ft.replace(R.id.fragment_container, myPageFragment);
                ft.commit();
                break;

            // Case Recipe bank button.
            case R.id.action_recipe_bank:
                fm = getFragmentManager();
                ft = fm.beginTransaction();
                RecipeFragment recipeBankFragment = new RecipeFragment();
                ft.replace(R.id.fragment_container, recipeBankFragment);
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



                // Check if the activated fragment was the FlowFragment.
                if (oldFragment instanceof FlowFragment) {
                    // Replace the old FlowFragment with a new one, my type of updating a fragment.
                    FlowFragment updatedFlowFragment = new FlowFragment();
                    ft.replace(R.id.fragment_container, updatedFlowFragment);
                    ft.commit();
                }
                // Check if the activated fragment was the RecipeFragment.
                if (oldFragment instanceof RecipeFragment) {
                    // Replace the old FlowFragment with a new one, my type of updating a fragment.
                    RecipeFragment updatedRecipeBankFragment = new RecipeFragment();
                    ft.replace(R.id.fragment_container, updatedRecipeBankFragment);
                    ft.commit();
                }

                // Stop the progressbar and return the update icon.
                menuItem.collapseActionView();
                menuItem.setActionView(null);
            }
        }, WAIT_TIME);
    }

    // History
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadHistory(String query) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            List items = getAllUsers(query);

            // Cursor
            String[] columns = new String[]{"_id", "text"};
            Object[] temp = new Object[]{0, "default"};
            MatrixCursor cursor = new MatrixCursor(columns);

            for (int i = 0; i < items.size(); i++) {
                temp[0] = i;
                temp[1] = items.get(i);
                cursor.addRow(temp);
            }
            // SearchView
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setSuggestionsAdapter(new SearchUserAdapter(this, cursor, items));
            search.setOnSuggestionListener(new SearchView.OnSuggestionListener(){

                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    // TODO Här kommer clicket i sökning in, har ska vi hämta vem som är tryckt och därefetr byta till hans profilsida.
                    return false;
                }
            });


        }
    }

    private List getAllUsers(String query) {
        String allUsers;
        JSONObject jsonObject;
        JSONArray jsonArray = null;
        List<String> items = new ArrayList<>();
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
        if (jsonArray != null) {
            // Check if the one of the likerS is the activated person.
            for (int e = 0; e < jsonArray.length(); e++) {
                try {
                    jsonObject = jsonArray.getJSONObject(e);
                    String username = jsonObject.getString(USERNAME_TAG);
                    if (username.toLowerCase().contains(query.toLowerCase())) {
                        items.add(username);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return items;
    }


    /**
     * Handle the buttons click from recipe bank fragment and toplist fragment to display the right fragment.
     * It's like tabs.
     * @param view got the information on which fragment there is to display.
     */
    @Override
    public void onButtonClicked(View view) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        // Switch to replace the fragment with the right one.
        switch (view.getId()){
            case R.id.toplist_button:
                ToplistFragment toplistFragment = new ToplistFragment();
                ft.replace(R.id.fragment_container, toplistFragment);
                ft.commit();
                break;
            case R.id.recipe_bank_button:
                RecipeFragment recipeFragment = new RecipeFragment();
                ft.replace(R.id.fragment_container, recipeFragment);
                ft.commit();
        }
    }
}






