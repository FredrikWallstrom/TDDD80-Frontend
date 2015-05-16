package com.example.frewa814.livekrubb.recipe;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.flow.PublicFlowFragment;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * This fragment will display the recipe bank (all fragments in the database).
 */
public class RecipeBankFragment extends ListFragment implements AdapterView.OnItemClickListener {

    /**
     * Constant tags for http requests.
     */
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String ID_TAG = "id";
    private static final String POST_TAG = "post";

    /**
     * Click listener instance that will point on MainActivity
     * so the MainActivity can handle the clicks in the fragments.
     */
    OnButtonClickedListener mListener;

    private ArrayList<String> recipeList;
    private ArrayList<String> recipeIDList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure MainActivity is implementing the OnButtonClickedListener interface.
        try {
            mListener = (OnButtonClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnButtonClickedListener ");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.recipe_bank, container, false);

        // Set button listener for the recipe bank button and toplist button.
        Button recipeBankButton = (Button) rootView.findViewById(R.id.recipe_bank_button);
        Button toplistButton = (Button) rootView.findViewById(R.id.toplist_button);
        recipeBankButton.setOnClickListener(clickListener);
        toplistButton.setOnClickListener(clickListener);

        // Find the searchView and set a OnQueryTextListener on it.
        final SearchView searchButton = (SearchView) rootView.findViewById(R.id.searchView_recipe_bank);
        searchButton.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            /**
             * When the user enter the searchbutton from keyboard or the search icon
             * this method will be called and hide the keyboard and collapse the searchview.
             * After that it will submit the search.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                searchButton.onActionViewCollapsed();
                submitSearch(query);
                return false;
            }

            /**
             * When the user enter one letter in the searchView this method will e run
             * every time and update the listView that are displayed based on the search.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                updateListView(newText);
                return false;
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get all recipes in the database that's gonna represent the recipe bank.
        getDataInList();

        // Create one adapter that wil be set to the listView.
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_1, recipeList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    /**
     * Method that take the first recipe from the list that matches
     * with the query and show it.
     */
    private void submitSearch(String query) {
        recipeIDList = new ArrayList<>();

        JSONArray jsonArray = PublicFlowFragment.allPosts;
        // Go through all posts and if the query matches with some of them
        // add them to the recipeIDList.
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String recipe_name = jsonObject.getString(RECIPE_NAME_TAG);
                String post_id = jsonObject.getString(ID_TAG);
                if (recipe_name.toLowerCase().contains(query.toLowerCase())){
                    recipeIDList.add(post_id);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Take the first match from the recipeIDList and display the recipe for the user.
        if (recipeIDList.size() != 0){
            String post_id = recipeIDList.get(0);
            String recipe;
            JSONObject jsonObject = null;
            try {
                recipe = new GetTask().execute(MainActivity.URL + "/get_post_by_id/" + post_id).get();
            } catch (InterruptedException | ExecutionException e) {
                recipe = "server error";
                e.printStackTrace();
            }

            if (!recipe.equals("server error")) {
                try {
                    jsonObject = new JSONObject(recipe);
                    jsonArray = jsonObject.getJSONArray(POST_TAG);
                    jsonObject = jsonArray.getJSONObject(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Inform the activity that we gonna change fragment to ShowRecipeFragment.
            mListener.onShowRecipeButtonClicked(jsonObject);
        }else{
            Toast noRecipeFound = Toast.makeText(getActivity(), "No recipe found!", Toast.LENGTH_LONG);
            noRecipeFound.show();
        }
    }

    /**
     * Calling this when i want to hide the keyboard.
     */
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * This method will update the listView when a user enter one letter in the searchView.
     * It will update the listView with the matches of the entered query from the user and
     * the recipes in the database.
     */
    private void updateListView(String newText) {
        recipeList = new ArrayList<>();
        recipeIDList = new ArrayList<>();

        JSONArray jsonArray = PublicFlowFragment.allPosts;
        // Go through all posts in the database and if we get a match with the query,
        // save the recipe name and the post id in different arrays.
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String recipe_name = jsonObject.getString(RECIPE_NAME_TAG);
                String post_id = jsonObject.getString(ID_TAG);
                if (recipe_name.toLowerCase().contains(newText.toLowerCase())){
                    recipeIDList.add(post_id);
                    recipeList.add(recipe_name);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Create one new adapter with the right information and set it to the listView.
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_1, recipeList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    // When a button is clicked, notify the activity.
    // MainActivity will then create the new fragment.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onButtonClicked(view);
        }
    };

    /**
     * This method will get all data that is gonna represent the bank.
     * It will add the data to de recipeList and that's the list that will be represented.
     */
    private void getDataInList() {
        recipeList = new ArrayList<>();
        recipeIDList = new ArrayList<>();
            try {
                JSONArray jsonArray = PublicFlowFragment.allPosts;
                // Go through all posts and add them to the to different lists,
                // one with the id on the post and one with the recipe name.
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String recipe_name = jsonObject.getString(RECIPE_NAME_TAG);
                    String post_id = jsonObject.getString(ID_TAG);
                    recipeIDList.add(post_id);
                    recipeList.add(recipe_name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    /**
     * This method will handle the click on the recipes in the bank.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String post_id = recipeIDList.get(position);
        String recipe;
        JSONObject jsonObject = null;

        // Hide the keyboard.
        hideKeyboard();

        try {
            recipe = new GetTask().execute(MainActivity.URL + "/get_post_by_id/" + post_id).get();
        } catch (InterruptedException | ExecutionException e) {
            recipe = "server error";
            e.printStackTrace();
        }

        if (!recipe.equals("server error")) {
            try {
                jsonObject = new JSONObject(recipe);
                JSONArray jsonArray = jsonObject.getJSONArray(POST_TAG);
                jsonObject = jsonArray.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Inform the activity that we gonna change fragment to ShowRecipeFragment.
        mListener.onShowRecipeButtonClicked(jsonObject);
    }

    /**
     * This method is used when the user want to refresh a fragment.
     * It will load the right data again in the getDataInList method
     * and after that set a new list adapter.
     */
    public void refresh(){
        getDataInList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_1, recipeList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

}
