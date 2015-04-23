package com.example.frewa814.livekrubb;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fredrik on 2015-04-22.
 */
public class SearchResultsActivity extends Activity {
    private static final String USER_TAG = "users";
    private static final String USERNAME_TAG = "username";
    private TextView usernameField;
    private TextView resultField;
    private ArrayList<String> users;
    private ListView listView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        usernameField = (TextView) findViewById(R.id.searchResult);
        listView = (ListView) findViewById(R.id.listView_searchedResult);
        imageView = (ImageView) findViewById(R.id.icon_searchResult);

        // get the action bar
        ActionBar actionBar = getActionBar();
        // Enabling Back navigation on Action Bar icon
        actionBar.setDisplayHomeAsUpEnabled(true);

        getAllUsers();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Handling intent data
     */
    private void handleIntent(Intent intent) {
        boolean match = false;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchedUser = intent.getStringExtra(SearchManager.QUERY);
            searchedUser = searchedUser.toLowerCase();

            for (String user : users) {
                if (searchedUser.equals(user.toLowerCase())){
                    match = true;
                    usernameField.setText(user);
                }
            }
            if (!match){
                imageView.setVisibility(View.INVISIBLE);
                usernameField.setText("Did you mean?");
                usernameField.setClickable(false);
                ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1, users);
                listView.setAdapter(adapter);
            }


        }

    }

    private void getAllUsers() {
        String allUsers;
        JSONObject jsonObject;
        JSONArray jsonArray = null;
        users = new ArrayList<>();
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
                    users.add(username);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }
}
