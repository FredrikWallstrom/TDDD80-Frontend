package com.example.frewa814.livekrubb.recipe;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.asynctask.LikeTask;
import com.example.frewa814.livekrubb.flow.FlowListData;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.concurrent.ExecutionException;

/**
 * Created by Fredrik on 2015-05-01.
 */
public class ShowRecipeFragment extends Fragment {

    private static final String RECIPE_INFO_TAG = "recipe_information";
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String ID_TAG = "id";
    private static final String USER_ID_TAG = "user_id";
    private static final String USER_TAG = "user";
    private static final String USERNAME_TAG = "username";
    private static final String LIKES_TAG = "likes";
    private static final String COMMENTS_TAG = "comments";
    OnButtonClickedListener mListener;

    private TextView mRecipeNameView;
    private TextView mRecipeInfoView;
    private TextView mUsernameView;
    private TextView mLikeView;
    private Button mLikeButton;
    private JSONObject mRecipe;
    private TextView mCommentView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Hide the actionBar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Check if MainActivity is implementing the click listener.
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
        View rootView = inflater.inflate(R.layout.show_recipe, container, false);

        // Set click listener for the buttons in the xml.
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_button);


        mRecipeNameView = (TextView) rootView.findViewById(R.id.recipe_name_show_recipe);
        mRecipeInfoView = (TextView) rootView.findViewById(R.id.recipe_directions_show_recipe);
        mUsernameView = (TextView) rootView.findViewById(R.id.posted_by_name_view);
        mLikeView = (TextView) rootView.findViewById(R.id.like_view_show_recipe);
        mLikeButton = (Button) rootView.findViewById(R.id.like_button_show_recipe);
        mCommentView = (TextView) rootView.findViewById(R.id.comment_view_show_recipe);
        Button commentButton = (Button) rootView.findViewById(R.id.comment_button_show_recipe);

        mLikeButton.setOnClickListener(clickListener);
        backButton.setOnClickListener(clickListener);
        commentButton.setOnClickListener(clickListener);
        mUsernameView.setOnClickListener(clickListener);
        return rootView;
    }

    // Click listener for the buttons in the xml.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String postID = null;
            String result = null;
            String userID = null;

            try {
                postID = mRecipe.getString(ID_TAG);
                userID = mRecipe.getString(USER_ID_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (view.getId() == R.id.like_button_show_recipe) {



                try {
                    if (postID != null){
                        LikeTask likeTask = new LikeTask(postID, ActivatedUser.activatedUsername);
                        result = likeTask.execute((Void) null).get();
                    }
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }


                if (result != null){
                    // Check if the user want to unlike or like and set the right text to the button.
                    if (result.equals("un_liked")) {
                        mLikeButton.setText("Like");
                    } else {
                        mLikeButton.setText("Unlike");
                    }
                }

                updateLikeView();
            }
            else if (view.getId() == R.id.comment_button_show_recipe){
                mListener.onButtonClicked(postID, "CommentFragment");
            }
            else if (view.getId() == R.id.posted_by_name_view){
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                mListener.onButtonClicked(userID, "MyPageFragment");
            }
            // The user clicked on back button.
            else{
                mListener.onButtonClicked(view);
            }
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String recipeName = null;
        String recipeInformation= null;

        String recipe = getArguments().getString("recipe");
        try {
            mRecipe = new JSONObject(recipe);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (recipe != null) {
            try {
                recipeName = mRecipe.getString(RECIPE_NAME_TAG);
                recipeInformation = mRecipe.getString(RECIPE_INFO_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (recipeName != null && recipeInformation != null){
            mRecipeInfoView.setText(recipeInformation);
            mRecipeNameView.setText(recipeName);
        }

        setTheNameOnPoster();
        JSONArray likeArray = updateLikeView();
        updateLikeButton(likeArray);
        updateCommentView();
    }

    private void updateCommentView() {
        String comments;
        JSONArray jsonArray = null;
        String postID = null;

        try {
            postID = mRecipe.getString(ID_TAG);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (postID != null){
            try {
                comments = new GetTask().execute(MainActivity.URL + "/all_comments_on_post/" + postID).get();
            } catch (InterruptedException | ExecutionException e) {
                comments = "server error";
                e.printStackTrace();
            }

            if (!comments.equals("server error")) {
                try {
                    JSONObject jsonObject = new JSONObject(comments);
                    jsonArray = jsonObject.getJSONArray(COMMENTS_TAG);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (jsonArray != null) {
                if (jsonArray.length() == 0) {
                    mCommentView.setText("");
                } else if (jsonArray.length() == 1) {
                    mCommentView.setText(jsonArray.length() + " " + "Comment");
                } else{
                    mCommentView.setText(jsonArray.length() + " " + "Comments");
                }
            }
        }
    }

    private void setTheNameOnPoster() {
        String postAuthorID = null;
        String user;
        String username = null;

        try {
            postAuthorID = mRecipe.getString(USER_ID_TAG);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (postAuthorID != null){
            try {
                user = new GetTask().execute(MainActivity.URL + "/get_user_by_id/" + postAuthorID).get();
            } catch (InterruptedException | ExecutionException e) {
                user = "server error";
                e.printStackTrace();
            }

            if (!user.equals("server error")) {
                try {
                    JSONObject jsonObject = new JSONObject(user);
                    JSONArray jsonArray = jsonObject.getJSONArray(USER_TAG);
                    jsonObject = jsonArray.getJSONObject(0);
                    username = jsonObject.getString(USERNAME_TAG);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (username != null){
            mUsernameView.setText(username);
        }

    }

    private JSONArray updateLikeView() {
        String postID = null;
        String users;
        JSONArray jsonArray = null;

        try {
            postID = mRecipe.getString(ID_TAG);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            users = new GetTask().execute(MainActivity.URL + "/all_likes_on_post/" + postID).get();
        } catch (InterruptedException | ExecutionException e) {
            users = "server error";
            e.printStackTrace();
        }

        if (!users.equals("server error")) {
            try {
                JSONObject jsonObject = new JSONObject(users);
                jsonArray = jsonObject.getJSONArray(LIKES_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (jsonArray != null) {
            if (jsonArray.length() == 0) {
                mLikeView.setText("");
            } else {
                mLikeView.setText(jsonArray.length() + " " + "Likes");
            }
        }
        return jsonArray;
    }

    private void updateLikeButton(JSONArray likeArray){
        boolean unLikeFlag = false;
        if (likeArray != null) {
            // Check if one of the likerS is the activated person.
            for (int i = 0; i < likeArray.length(); i++) {
                try {
                    JSONObject jsonObject = likeArray.getJSONObject(i);
                    String username = jsonObject.getString(USERNAME_TAG);
                    if (username.equals(ActivatedUser.activatedUsername)) {
                        unLikeFlag = true;
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
            // Check if we gonna set the Like or Unlike button.
            if (unLikeFlag) {
                mLikeButton.setText("Unlike");
            } else {
                mLikeButton.setText("Like");
            }
        }
    }
}
