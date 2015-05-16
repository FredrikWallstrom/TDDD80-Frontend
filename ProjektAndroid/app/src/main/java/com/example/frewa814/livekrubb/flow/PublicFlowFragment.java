package com.example.frewa814.livekrubb.flow;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.frewa814.livekrubb.adapters.FlowListAdapter;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Fragment that gonna represent the public flow.
 */
public class PublicFlowFragment extends ListFragment {

    /**
     * Constant tags for http requests.
     */
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String POST_INFORMATION_TAG = "post_information";
    private static final String POST_AUTHOR_ID_TAG = "user_id";
    private static final String USERNAME_TAG = "username";
    private static final String USER_TAG = "user";
    private static final String ID_TAG = "id";
    private static final String LOCATION_TAG = "location";
    private static final String POST_TAG = "posts";

    /**
     * This is the list that will be presented in the list view.
     */
    private ArrayList<FlowListData> myList;

    /**
     * Click listener instance that will point on MainActivity
     * so the MainActivity can handle the clicks in the fragments.
     */
    OnButtonClickedListener mListener;

    /**
     * JSONArray with all posts in the database.
     */
    public static JSONArray allPosts;

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
        View rootView = inflater.inflate(R.layout.public_flow_list, container, false);

        // Set button listener for the share recipe button.
        Button postRecipeButton = (Button) rootView.findViewById(R.id.share_recipe_button);
        postRecipeButton.setOnClickListener(clickListener);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get all posts in the database that's gonna represent the flow.
        getDataInList();

        // Make custom adapter and set it to the listView.
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList);
        setListAdapter(adapter);
    }

    /**
     * This method will get all data that is gonna represent the flow.
     * It will add the data to a temp lists and then create one FlowListData
     * object for every items in the temp list.
     * And after that it will add the object to the list that will
     * be sent to the FlowListAdapter.
     */
    private void getDataInList() {
        // Temp list there all information will be saved.
        List<String> recipeNameList = new ArrayList<>();
        List<String> postInformationList = new ArrayList<>();
        List<String> postAuthorList = new ArrayList<>();
        List<String> postIDList = new ArrayList<>();
        List<JSONObject> recipeList = new ArrayList<>();
        List<String> locationList = new ArrayList<>();

        // The list that gonna represent the flow.
        myList = new ArrayList<>();

        try {
            // get all posts from database.
            allPosts = getPosts();

            if (allPosts != null) {
                if (allPosts.length() != 0) {

                    // Go through all posts and save them in the temp lists.
                    for (int i = 0; i < allPosts.length(); i++) {
                        JSONObject object = allPosts.getJSONObject(i);

                        String recipeName = object.getString(RECIPE_NAME_TAG);
                        String postInformation = object.getString(POST_INFORMATION_TAG);
                        String postAuthorID = object.getString(POST_AUTHOR_ID_TAG);
                        String postID = object.getString(ID_TAG);
                        String location = object.getString(LOCATION_TAG);

                        String postAuthor = getPostAuthor(postAuthorID);

                        locationList.add(location);
                        postIDList.add(postID);
                        recipeNameList.add(recipeName);
                        postInformationList.add(postInformation);
                        postAuthorList.add(postAuthor);
                        recipeList.add(object);
                    }
                }
            } else {
                Toast serverError = Toast.makeText(getActivity(), "Failed to update, Try again!", Toast.LENGTH_LONG);
                serverError.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Integer loopInteger = 0;
        while (recipeNameList.size() > loopInteger) {

            // Create a new object for each list item
            FlowListData flowListData = new FlowListData();
            flowListData.setPostAuthor(postAuthorList.get(loopInteger));
            flowListData.setRecipeName(recipeNameList.get(loopInteger));
            flowListData.setPostInformation(postInformationList.get(loopInteger));
            flowListData.setPostID(postIDList.get(loopInteger));
            flowListData.setRecipe(recipeList.get(loopInteger));
            flowListData.setLocation(locationList.get(loopInteger));

            // Add this object into the ArrayList myList
            myList.add(flowListData);
            loopInteger++;
        }
    }

    // Click listener for the share recipe button.
    // When the button is clicked, notify the activity.
    // MainActivity will then create the new fragment.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onShareRecipeButtonClicked("PublicFlowFragment");
        }
    };

    /**
     * This method will get the post author username from the database from a given post id.
     */
    public String getPostAuthor(String postAuthorID) {
        String postAuthor;
        String postAuthorName = null;

        try {
            postAuthor = new GetTask().execute(MainActivity.URL + "/get_user_by_id/" + postAuthorID).get();
        } catch (InterruptedException | ExecutionException e) {
            postAuthor = "server error";
            e.printStackTrace();
        }

        if (!postAuthor.equals("server error")) {
            try {
                JSONObject jsonObject = new JSONObject(postAuthor);
                JSONArray jsonArray = jsonObject.getJSONArray(USER_TAG);
                jsonObject = jsonArray.getJSONObject(0);
                postAuthorName = jsonObject.getString(USERNAME_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return postAuthorName;

    }

    /**
     * This method is used when the user want to refresh a fragment.
     * It will load the right data again in the getDataInList method
     * and after that set a new list adapter.
     */
    public void refresh() {
        getDataInList();
        // Make custom adapter and set it to the listView.
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList);
        setListAdapter(adapter);
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
}


