package com.example.frewa814.livekrubb.recipe;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.flow.PublicFlowFragment;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

/**
 * Fragment that will display a topList of the posts by one person.
 */
public class PersonalToplistFragment extends ListFragment implements AdapterView.OnItemClickListener {

    /**
     * Constant tags for http requests.
     */
    private static final String ID_TAG = "id";
    private static final String LIKES_TAG = "likes";
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String POST_TAG = "post";
    private static final String USER_ID_TAG = "user_id";
    private static final String USER_TAG = "user";
    private static final String USERNAME_TAG = "username";

    /**
     * Click listener instance that will point on MainActivity
     * so the MainActivity can handle the clicks in the fragments.
     */
    OnButtonClickedListener mListener;

    /**
     * The user id on the user that we gonna represent the topList of.
     */
    private String mUserID;

    private ArrayList<String> recipeList;
    private ArrayList<String> recipeIDList;
    private TextView mNameView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Hide the actionBar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.personal_toplist, container, false);

        // Set button listener for the back button.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_button);
        backButton.setOnClickListener(clickListener);

        // Find the username view.
        mNameView = (TextView) rootView.findViewById(R.id.personal_toplist_name_view);
        return rootView;
    }

    // When the back button is clicked, notify the activity.
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

        // Get the post id from the bundle so we know which topList
        // we gonna display.
        mUserID = getArguments().getString("id");

        // Get the username and set the username text on the username view.
        String username = getUserName();
        mNameView.setText(username);


        // Get all recipes in the database that's gonna represent the topList.
        getDataInList();

        // Set the adapter to the listView.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, recipeList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    /**
     * This method will get all data that is gonna represent the flow.
     * It will add the data to a temp lists and then sort it by likes.
     * After that it will go through the temp list and add the name on the recipe to the list
     * that will represent the listView.
     */
    private void getDataInList() {
        // Temp list there we will save likes and the post id.
        ArrayList<String> tempList = new ArrayList<>();
        // Recipe list that will represent the listView.
        recipeList = new ArrayList<>();
        // List that will save the id's on the posts so we can take out right
        // recipe when the user click on one recipe.
        recipeIDList = new ArrayList<>();

        JSONArray recipes = PublicFlowFragment.allPosts;
        if (recipes != null) {
            try {
                // Go through all recipes and if the recipe is posted by the user
                // that is showed, save it to the temp list.
                for (int i = 0; i < recipes.length(); i++) {
                    JSONObject jsonObject = recipes.getJSONObject(i);
                    String userID = jsonObject.getString(USER_ID_TAG);
                    if (userID.equals(mUserID)) {
                        String post_id = jsonObject.getString(ID_TAG);
                        String likes = getAllLikesOnPost(post_id);
                        tempList.add(likes + " " + post_id);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Sort the list by likes.
        tempList = sortListByLikes(tempList);

        Integer counter = 1;
        StringBuilder sb;
        String recipe;
        String recipeName = null;
        // Go through the tempList and for every item, split the list and take out the post id
        // and save it to the string builder.
        for (int i = 0; i < tempList.size(); i++) {
            if (counter <= 5) {
                sb = new StringBuilder();
                String element = tempList.get(i);
                String[] splitedList = element.split(" ");
                Boolean firstRun = true;
                for (String s : splitedList) {
                    if (!firstRun) {
                        sb.append(s);
                    } else {
                        firstRun = false;
                    }
                }

                // Save the post id to the recipeIDList so we can take out the right id when the user
                // click on one recipe in the fragment.
                recipeIDList.add(sb.toString());

                try {
                    // Get the post by id that is in the stringBuilder.
                    recipe = new GetTask().execute(MainActivity.URL + "/get_post_by_id/" + sb).get();
                } catch (InterruptedException | ExecutionException e) {
                    recipe = "server error";
                    e.printStackTrace();
                }

                if (!recipe.equals("server error")) {
                    // Get the recipe name on the post.
                    try {
                        JSONObject jsonObject = new JSONObject(recipe);
                        JSONArray jsonArray = jsonObject.getJSONArray(POST_TAG);
                        jsonObject = jsonArray.getJSONObject(0);
                        recipeName = jsonObject.getString(RECIPE_NAME_TAG);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Add the recipe name to the list that will represent the listView
                // it will also add "counter" that will be the position in the topList.
                if (recipeName != null) {
                    recipeList.add(counter + "." + " " + " " + recipeName);
                }
                counter++;
            }
        }
    }

    /**
     * This method will get the username that we will represent the topList of.
     */
    private String getUserName() {
        String user;
        String username = null;
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            user = new GetTask().execute(MainActivity.URL + "/get_user_by_id/" + mUserID).get();
        } catch (InterruptedException | ExecutionException e) {
            user = "server error";
            e.printStackTrace();
        }

        if (!user.equals("server error")) {
            try {
                jsonObject = new JSONObject(user);
                jsonArray = jsonObject.getJSONArray(USER_TAG);
                jsonObject = jsonArray.getJSONObject(0);
                username = jsonObject.getString(USERNAME_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return username;
    }

    /**
     * This method will sort the tempList by likes, the first parameter in the list items
     * will be the number of likes on each post.
     */
    private ArrayList<String> sortListByLikes(ArrayList<String> tempList) {
        Collections.sort(tempList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int comp = lhs.compareTo(rhs);
                if (comp > 0)
                    return -1;
                if (comp < 0)
                    return 1;
                return 0;
            }
        });
        return tempList;
    }

    /**
     * This method will get all likes on one post from the database.
     */
    private String getAllLikesOnPost(String post_id) {
        String likes;
        JSONObject jsonObject;
        JSONArray jsonArray = null;
        String result = "0";
        try {
            likes = new GetTask().execute(MainActivity.URL + "/all_likes_on_post/" + post_id).get();
        } catch (InterruptedException | ExecutionException e) {
            likes = "server error";
            e.printStackTrace();
        }
        if (!likes.equals("server error")) {
            try {
                jsonObject = new JSONObject(likes);
                jsonArray = jsonObject.getJSONArray(LIKES_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonArray != null) {
            result = String.valueOf(jsonArray.length());
        }
        return result;
    }

    /**
     * This method will handle the click on the recipes in the fragment.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get the post id of the clicked post.
        String post_id = recipeIDList.get(position);
        String recipe;
        JSONObject jsonObject = null;
        JSONArray jsonArray;
        try {
            // Get the recipe from the database.
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
    }
}
