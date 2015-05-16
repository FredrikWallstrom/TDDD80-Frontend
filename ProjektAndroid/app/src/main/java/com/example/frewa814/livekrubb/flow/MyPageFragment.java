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
 * Fragment that will display the MyPageFragment (profile page).
 */
public class MyPageFragment extends ListFragment {

    /**
     * Constant tags for http requests.
     */
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String POST_INFORMATION_TAG = "post_information";
    private static final String USERNAME_TAG = "username";
    private static final String USER_TAG = "user";
    private static final String ID_TAG = "id";
    private static final String LOCATION_TAG = "location";
    private static final String USER_ID_TAG = "user_id";
    private static final String USERS_TAG = "users";

    /**
     * This is the list that will be presented in the list view.
     */
    private ArrayList<FlowListData> myList;

    /**
     * Click listener instance that will point on MainActivity
     * so the MainActivity can handle the clicks in the fragments.
     */
    OnButtonClickedListener mListener;

    private String mUserID;
    private TextView nameView;
    private Button followButton;

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
        View rootView = inflater.inflate(R.layout.my_page, container, false);

        // Find the buttons in the xml and set a click listener on them.
        nameView = (TextView) rootView.findViewById(R.id.name_my_page);
        followButton = (Button) rootView.findViewById(R.id.follow_button);
        Button personalToplist = (Button) rootView.findViewById(R.id.personal_toplist_button);
        personalToplist.setOnClickListener(clickListener);
        followButton.setOnClickListener(clickListener);

        // Get information that have been sent from activity to this fragment.
        // This is the user id on user that is gonna be represented in the fragment.
        mUserID = getArguments().getString("id");

        // Set the right username to the nameView field.
        setInformationAboutUser();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check if the user that is on the profile page is followed by the activated user.
        boolean following = checkIfFollow();

        // If the activated user follow the profiled user, set the button to unfollow.
        if (following) {
            followButton.setText("Unfollow");
        }

        // Get all posts in the database that's gonna represent the flow.
        getDataInList();

        // Make custom adapter and set it to the listview.
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList);
        setListAdapter(adapter);
    }

    /**
     * This method will check if the activated person follows the user that is represented
     * in the MyPageFragment.
     */
    private boolean checkIfFollow() {
        String users;

        // Check if the activated user is looking on his own page.
        // If yes, set the button to unClickable.
        if (!mUserID.equals(ActivatedUser.activatedUserID)) {
            try {
                users = new GetTask().execute(MainActivity.URL + "/get_followers_by_id/" + mUserID).get();
            } catch (InterruptedException | ExecutionException e) {
                users = "server error";
                e.printStackTrace();
            }

            if (!users.equals("server error")) {
                try {
                    JSONObject jsonObject = new JSONObject(users);
                    JSONArray jsonArray = jsonObject.getJSONArray(USERS_TAG);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        String userID = jsonObject.getString(ID_TAG);
                        // Go through all users that is following the user that is displayed in the fragment.
                        // If one of them is the activated user, return true.
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

    /**
     * This method will set the username on the user that is gonna be displayed in the fragment
     * on the nameView.
     */
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
        List<String> locationList = new ArrayList<>();
        List<JSONObject> recipeList = new ArrayList<>();

        // The list that gonna represent the flow.
        myList = new ArrayList<>();

        try {
            JSONArray posts = MainActivity.allPosts;
            if (posts != null) {
                if (posts.length() != 0) {
                    // Go through all posts and if posts is equal to the user id that is displayed
                    // on the fragment, add the post to the temp lists.
                    // So we can show only the posts that the displayed user have been posted to the list.
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
     * Get the posts authors username from the database from a given user id..
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

    // Click listener for the buttons in the xml.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Check if the user clicked on the personal topList button.
            if (view.getId() == R.id.personal_toplist_button) {
                mListener.onButtonClicked(mUserID, "PersonalToplistFragment");

            // Else, the user clicked on the follow button.
            } else {
                FollowTask followTask = new FollowTask(ActivatedUser.activatedUserID, mUserID);
                try {
                    String result = followTask.execute((Void) null).get();
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


    /**
     * This method is used when the user want to refresh a fragment.
     * It will load the right data again in the getDataInList method
     * and after that set a new list adapter.
     */
    public void refresh() {
        getDataInList();
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList);
        setListAdapter(adapter);
    }
}


