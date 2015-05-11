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
 * Created by Fredrik on 2015-04-24.
 */
public class RecipeBankFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String ID_TAG = "id";
    private static final String POST_TAG = "post";
    OnButtonClickedListener mListener;
    private ArrayList<String> recipeList;
    private ArrayList<String> recipeIDList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

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

        final SearchView searchButton = (SearchView) rootView.findViewById(R.id.searchView_recipe_bank);
        searchButton.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                searchButton.onActionViewCollapsed();
                submitSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateListView(newText);
                return false;
            }
        });

        return rootView;
    }

    private void submitSearch(String query) {
        JSONArray jsonArray = MainActivity.allPosts;
        recipeList = new ArrayList<>();
        recipeIDList = new ArrayList<>();
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
            mListener.onShowRecipeButtonClicked(jsonObject);
        }else{
            Toast noRecipeFound = Toast.makeText(getActivity(), "No recipe found!", Toast.LENGTH_LONG);
            noRecipeFound.show();
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void updateListView(String newText) {
        JSONArray jsonArray = MainActivity.allPosts;
        recipeList = new ArrayList<>();
        recipeIDList = new ArrayList<>();
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get all recipes in the database that's gonna represent the recipe bank.
        getDataInList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_1, recipeList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    private void getDataInList() {
        recipeList = new ArrayList<>();
        recipeIDList = new ArrayList<>();
            try {
                JSONArray jsonArray = MainActivity.allPosts;
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String post_id = recipeIDList.get(position);
        String recipe;
        JSONObject jsonObject = null;
        JSONArray jsonArray;

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
                jsonArray = jsonObject.getJSONArray(POST_TAG);
                jsonObject = jsonArray.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mListener.onShowRecipeButtonClicked(jsonObject);
    }

    public void refresh(){
        getDataInList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_1, recipeList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

}
