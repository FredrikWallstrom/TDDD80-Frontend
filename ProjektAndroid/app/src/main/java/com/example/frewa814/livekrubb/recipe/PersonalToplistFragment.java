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
 * Created by Fredrik on 2015-05-08.
 */
public class PersonalToplistFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private static final String ID_TAG = "id";
    private static final String LIKES_TAG = "likes";
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String POST_TAG = "post";
    private static final String USER_ID_TAG = "user_id";
    private static final String USER_TAG = "user";
    private static final String USERNAME_TAG = "username";
    OnButtonClickedListener mListener;
    private ArrayList<String> recipeList;
    private ArrayList<String> recipeIDList;
    private String mUserID;
    private TextView mNameView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Hide the actionBar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
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

        // Set button listener for the recipe bank button and toplist button.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_button);
        backButton.setOnClickListener(clickListener);

        mNameView = (TextView) rootView.findViewById(R.id.personal_toplist_name_view);

        return rootView;
    }

    // When the button is clicked, notify the activity.
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

        // Get all recipes in the database that's gonna represent the topList.
        getDataInList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, recipeList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    private void getDataInList() {
        recipeList = new ArrayList<>();
        recipeIDList = new ArrayList<>();
        ArrayList<String> tempList = new ArrayList<>();
        JSONArray recipes;
        String userID = null;
        JSONObject jsonObject;
        JSONArray jsonArray;
        String likes;

        // All posts
        recipes = MainActivity.allPosts;


        if (recipes != null) {
            try {
                for (int i = 0; i < recipes.length(); i++) {
                    jsonObject = recipes.getJSONObject(i);
                    userID = jsonObject.getString(USER_ID_TAG);
                    if (userID.equals(mUserID)) {
                        String post_id = jsonObject.getString(ID_TAG);
                        likes = getAllLikesOnPost(post_id);
                        tempList.add(likes + " " + post_id);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        tempList = sortListByLikes(tempList);

        Integer counter = 1;
        StringBuilder sb;
        for (int i = 0; i < tempList.size(); i++) {
            sb = new StringBuilder();
            String element = tempList.get(i);
            String[] splitedList = element.split(" ");
            Boolean firstRun = true;
            for (String s : splitedList) {
                if (!firstRun) {
                    if (counter <= 5) {
                        sb.append(s);
                    }
                } else {
                    firstRun = false;
                }
            }

            recipeIDList.add(sb.toString());

            String recipe;
            String recipeName = null;
            try {
                recipe = new GetTask().execute(MainActivity.URL + "/get_post_by_id/" + sb).get();
            } catch (InterruptedException | ExecutionException e) {
                recipe = "server error";
                e.printStackTrace();
            }

            if (!recipe.equals("server error")) {
                try {
                    jsonObject = new JSONObject(recipe);
                    jsonArray = jsonObject.getJSONArray(POST_TAG);
                    jsonObject = jsonArray.getJSONObject(0);
                    recipeName = jsonObject.getString(RECIPE_NAME_TAG);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (recipeName != null) {
                recipeList.add(counter + "." + " " + " " + recipeName);
            }

            counter++;
        }

        if (userID != null){
            String username = getUserName(userID);
            mNameView.setText(username);
        }

    }

    private String getUserName(String userID) {
        String user;
        String username = null;
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            user = new GetTask().execute(MainActivity.URL + "/get_user_by_id/" + userID).get();
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

    private ArrayList sortListByLikes(ArrayList testList) {

        Collections.sort(testList, new Comparator<String>() {
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
        return testList;
    }

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        String post_id = recipeIDList.get(position);
        String recipe;
        JSONObject jsonObject = null;
        JSONArray jsonArray;
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
}
