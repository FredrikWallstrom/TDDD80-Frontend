package com.example.frewa814.livekrubb.flow;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.asynctask.FollowTask;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.adapters.FlowListAdapter;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fredrik on 2015-04-22.
 */
public class MyPageFragment extends ListFragment {

    private static final String USER_ID_TAG = "user_id";
    private static final String USERS_TAG = "users";
    private ArrayList<FlowListData> myList;
    private String mUserID;
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String POST_INFORMATION_TAG = "post_information";
    private static final String USERNAME_TAG = "username";
    private static final String USER_TAG = "user";
    private static final String ID_TAG = "id";
    private static final String LOCATION_TAG = "location";
    private TextView nameView;
    private Button followButton;
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
        View rootView = inflater.inflate(R.layout.my_page, container, false);

        nameView = (TextView) rootView.findViewById(R.id.name_my_page);
        followButton = (Button) rootView.findViewById(R.id.follow_button);
        Button personalToplist = (Button) rootView.findViewById(R.id.personal_toplist_button);
        personalToplist.setOnClickListener(clickListener);
        followButton.setOnClickListener(clickListener);

        mUserID = getArguments().getString("id");

        setInformationAboutUser();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean following = checkIfFollow();

        if (following) {
            followButton.setText("Unfollow");
        }

        // Get all posts in the database that's gonna represent the flow.
        getDataInList();

        // Make custom adapter and set it to the listview.
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList, this);
        setListAdapter(adapter);
    }

    private boolean checkIfFollow() {
        String users;
        JSONObject jsonObject;
        JSONArray jsonArray;
        if (!mUserID.equals(ActivatedUser.activatedUserID)) {
            try {
                users = new GetTask().execute(MainActivity.URL + "/get_followers_by_id/" + mUserID).get();
            } catch (InterruptedException | ExecutionException e) {
                users = "server error";
                e.printStackTrace();
            }

            if (!users.equals("server error")) {
                try {
                    jsonObject = new JSONObject(users);
                    jsonArray = jsonObject.getJSONArray(USERS_TAG);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        String userID = jsonObject.getString(ID_TAG);
                        if (userID.equals(ActivatedUser.activatedUserID)) {
                            return true;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else {
            followButton.setClickable(false);
        }
        return false;
    }

    private void setInformationAboutUser() {
        String user;
        JSONObject jsonObject;
        JSONArray jsonArray;
        String userName = null;
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
                userName = jsonObject.getString(USERNAME_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (userName != null) {
            nameView.setText(userName);
        }
    }


    private void getDataInList() {
        List<String> recipeNameList = new ArrayList<>();
        List<String> postInformationList = new ArrayList<>();
        List<String> postAuthorList = new ArrayList<>();
        List<String> postIDList = new ArrayList<>();
        List<String> locationList = new ArrayList<>();
        myList = new ArrayList<>();
        JSONArray posts;
        List<JSONObject> recipeList = new ArrayList<>();


        try {
            posts = PublicFlowFragment.posts;
            if (posts != null) {
                if (posts.length() != 0) {
                    for (int i = 0; i < posts.length(); i++) {
                        JSONObject object = posts.getJSONObject(i);

                        if (object.getString(USER_ID_TAG).equals(mUserID)) {
                            String recipeName = object.getString(RECIPE_NAME_TAG);
                            String postInformation = object.getString(POST_INFORMATION_TAG);
                            String postID = object.getString(ID_TAG);
                            String location = object.getString(LOCATION_TAG);

                            String postAuthor = getPostAuthor(mUserID);

                            locationList.add(location);
                            postIDList.add(postID);
                            recipeNameList.add(recipeName);
                            postInformationList.add(postInformation);
                            postAuthorList.add(postAuthor);
                            recipeList.add(object);
                        }
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

    /**
     * Get the posts author from the database.
     */
    private String getPostAuthor(String postAuthorID) {
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

    // When a button is clicked, notify the activity.
    // MainActivity will then create the new fragment.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.personal_toplist_button) {
                mListener.onButtonClicked(mUserID, "PersonalToplistFragment");
            } else {
                FollowTask followTask = new FollowTask(ActivatedUser.activatedUserID, mUserID);
                try {
                    String result = followTask.execute((Void) null).get();
                    System.out.println(result);
                    if (result.equals("un_followed")) {
                        followButton.setText("Follow");
                    } else {
                        followButton.setText("Unfollow");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void refresh() {
        getDataInList();
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList, this);
        setListAdapter(adapter);
    }
}


