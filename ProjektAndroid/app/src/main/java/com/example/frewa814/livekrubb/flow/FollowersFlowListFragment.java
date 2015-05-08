package com.example.frewa814.livekrubb.flow;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.adapters.FlowListAdapter;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fredrik on 2015-05-05.
 */
public class FollowersFlowListFragment extends ListFragment {

    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String POST_INFORMATION_TAG = "post_information";
    private static final String POST_AUTHOR_ID_TAG = "user_id";
    private static final String ID_TAG = "id";
    private static final String USERS_TAG = "users";
    private static final String USER_ID_TAG = "user_id";
    private static final String USERNAME_TAG = "username";
    private static final String USER_TAG = "user";
    private static final String LOCATION_TAG = "location";

    private ArrayList myList;

    OnButtonClickedListener mListener;

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
        View rootView = inflater.inflate(R.layout.followers_flow_list, container, false);

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

        // Make custom adapter and set it to the listview.
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList, this);
        setListAdapter(adapter);
    }

    private void getDataInList() {
        List<String> recipeNameList = new ArrayList<>();
        List<String> postInformationList = new ArrayList<>();
        List<String> postAuthorList = new ArrayList<>();
        List<String> postIDList = new ArrayList<>();
        List<JSONObject> recipeList = new ArrayList<>();
        List<String> locationList = new ArrayList<>();
        myList = new ArrayList();
        JSONArray posts;
        JSONArray followersArray;
        String followers;
        JSONObject jsonObject;




        posts = PublicFlowFragment.posts;
        if (posts != null) {
            if (posts.length() != 0) {

                try {
                    followers = new GetTask().execute(MainActivity.URL + "/followed_by_id/" + ActivatedUser.activatedUserID).get();
                } catch (InterruptedException | ExecutionException e) {
                    followers = "server error";
                    e.printStackTrace();
                }

                if (!followers.equals("server error")) {
                    try {
                        jsonObject = new JSONObject(followers);
                        followersArray = jsonObject.getJSONArray(USERS_TAG);


                        for (int e = 0; e < followersArray.length(); e++) {
                            for (int i = 0; i < posts.length(); i++) {
                                JSONObject postObject = posts.getJSONObject(i);
                                JSONObject userObject = followersArray.getJSONObject(e);
                                String followerUserID = userObject.getString(ID_TAG);
                                String postUserID = postObject.getString(USER_ID_TAG);
                                if (followerUserID.equals(postUserID)) {
                                    String recipeName = postObject.getString(RECIPE_NAME_TAG);
                                    String postInformation = postObject.getString(POST_INFORMATION_TAG);
                                    String postAuthorID = postObject.getString(POST_AUTHOR_ID_TAG);
                                    String postID = postObject.getString(ID_TAG);
                                    String location = postObject.getString(LOCATION_TAG);

                                    String postAuthor = getPostAuthor(postAuthorID);

                                    locationList.add(location);
                                    postIDList.add(postID);
                                    recipeNameList.add(recipeName);
                                    postInformationList.add(postInformation);
                                    postAuthorList.add(postAuthor);
                                    recipeList.add(postObject);

                                }
                            }
                        }

                    }catch(JSONException e){
                        e.printStackTrace();
                }

            } else {
                Toast Error = Toast.makeText(getActivity(), "Your flow is empty. Search some friends and follow them!", Toast.LENGTH_LONG);
                Error.show();
            }
        } else {
            Toast serverError = Toast.makeText(getActivity(), "Failed to update, Try again!", Toast.LENGTH_LONG);
            serverError.show();
        }
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




    // When a button is clicked, notify the activity.
    // MainActivity will then create the new fragment.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onButtonClicked(view);
        }
    };

    public String getPostAuthor(String postAuthorID) {
        {
            String postAuthor;
            String postAuthorName = null;
            JSONObject jsonObject;
            JSONArray jsonArray;
            try {
                postAuthor = new GetTask().execute(MainActivity.URL + "/get_user_by_id/" + postAuthorID).get();
            } catch (InterruptedException | ExecutionException e) {
                postAuthor = "server error";
                e.printStackTrace();
            }

            if (!postAuthor.equals("server error")) {
                try {
                    jsonObject = new JSONObject(postAuthor);
                    jsonArray = jsonObject.getJSONArray(USER_TAG);
                    jsonObject = jsonArray.getJSONObject(0);
                    postAuthorName = jsonObject.getString(USERNAME_TAG);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return postAuthorName;
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void refresh(){
        getDataInList();
        // Make custom adapter and set it to the listview.
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList, this);
        setListAdapter(adapter);
    }
}
