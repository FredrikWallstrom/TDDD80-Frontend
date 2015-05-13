package com.example.frewa814.livekrubb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.frewa814.livekrubb.asynctask.LikeTask;
import com.example.frewa814.livekrubb.flow.FlowListData;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.AutoResizeTextView;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Adapter for the FlowFragment.
 */
public class FlowListAdapter extends BaseAdapter {

    /**
     * Constant tags for http requests.
     */
    private static final String LIKES_TAG = "likes";
    private static final String USERNAME_TAG = "username";
    private static final String USER_ID_TAG = "user_id";
    private static final String COMMENTS_TAG = "comments";

    /**
     * This is the list that will be presented in the list view.
     */
    private ArrayList myList = new ArrayList();

    /**
     * Inflater.
     */
    private LayoutInflater inflater;

    /**
     * Click listener instance that will point on MainActivity
     * so the MainActivity can handle the clicks in the fragments.
     */
    OnButtonClickedListener mListener;

    /**
     * Constructor for the class that will init the list and the inflater.
     * It will also init mListener field.
     */
    public FlowListAdapter(Context cont, ArrayList myList) {
        this.myList = myList;
        inflater = LayoutInflater.from(cont);

        // Make sure MainActivity is implementing the OnButtonClickedListener interface.
        try {
            mListener = (OnButtonClickedListener) cont;
        } catch (ClassCastException e) {
            throw new ClassCastException(cont.toString() + " must implement OnButtonClickedListener ");
        }
    }


    @Override
    public int getCount() {
        return myList.size();
    }

    @Override
    public FlowListData getItem(int position) {
        return (FlowListData) myList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * This method is loaded every time the user "scroll/load" in one new list item.
     * This will set the right things on the right posts from the myList list.
     */
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final MyViewHolder mViewHolder;
        Boolean unLikeFlag = false;

        // If we are here for the first time we need to find the views in list items.
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.flow_list_item, parent, false);
            mViewHolder = new MyViewHolder();

            // Set up the ViewHolder one time.
            mViewHolder.postAuthorView = (TextView) convertView.findViewById(R.id.post_author);
            mViewHolder.recipeButton = (Button) convertView.findViewById(R.id.recipe_button_flow_list);
            mViewHolder.postInformationView = (AutoResizeTextView) convertView.findViewById(R.id.post_information);
            mViewHolder.likeButton = (Button) convertView.findViewById(R.id.like_button);
            mViewHolder.commentButton = (Button) convertView.findViewById(R.id.comment_button);
            mViewHolder.displayLikesView = (TextView) convertView.findViewById(R.id.likes_count);
            mViewHolder.displayCommentsView = (TextView) convertView.findViewById(R.id.comments_count);
            mViewHolder.displayLocationView = (TextView) convertView.findViewById(R.id.location_view_flow_item);

            // Store the holder with the view.
            convertView.setTag(mViewHolder);
        } else {
            // Get the viewHolder if we already created one before.
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        // Get the right data from the flowListData that will be displayed on the post.
        FlowListData flowListData = (FlowListData) myList.get(position);

        // Set the text in the views on the post items.
        mViewHolder.postAuthorView.setText(flowListData.getPostAuthor());
        mViewHolder.postInformationView.setText(flowListData.getPostInformation());
        mViewHolder.recipeButton.setText(flowListData.getRecipeName());
        mViewHolder.displayLocationView.setText(flowListData.getLocation());

        // Get the postID
        String postID = flowListData.getPostID();
        // Update how many likes there is on the post.
        JSONArray likeArray = updateLikeView(postID, mViewHolder);
        // Update ho many comments there is on the post.
        updateCommmentView(postID, mViewHolder);


        // Check if something was returned from the updateLikeViews.
        if (likeArray != null) {
            // Go through the like array with the people who liked the post and
            // check if one of the liker is the activated person.
            for (int e = 0; e < likeArray.length(); e++) {
                try {
                    JSONObject jsonObject = likeArray.getJSONObject(e);
                    String username = jsonObject.getString(USERNAME_TAG);
                    if (username.equals(ActivatedUser.activatedUsername)) {
                        // If the activated person is one of the liker, set the unlikeFlag.
                        unLikeFlag = true;
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
            // Check if we gonna set the Like or Unlike button.
            if (unLikeFlag) {
                mViewHolder.likeButton.setText("Unlike");
            } else {
                mViewHolder.likeButton.setText("Like");
            }
        }

        // Click listener for the like button.
        mViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the position in the list for the clicked post.
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                int position = listView.getPositionForView(parentRow);
                FlowListData flowListData = (FlowListData) myList.get(position);
                String postID = flowListData.getPostID();


                LikeTask postTask = new LikeTask(postID, ActivatedUser.activatedUsername);

                try {
                    String result = postTask.execute((Void) null).get();

                    // Check if the user want unlikeD or likeD and set the right text to the button.
                    if (result.equals("un_liked")) {
                        mViewHolder.likeButton.setText("Like");
                    } else {
                        mViewHolder.likeButton.setText("Unlike");
                    }
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }


                // Update the likeView (how many likes we got on the post).
                updateLikeView(postID, mViewHolder);
            }
        });

        // Click listener for the show recipe button.
        mViewHolder.recipeButton.setOnClickListener(new View.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(View v) {
                                                            // Get the position in the list for the clicked post.
                                                            View parentRow = (View) v.getParent();
                                                            ListView listView = (ListView) parentRow.getParent();
                                                            int position = listView.getPositionForView(parentRow);
                                                            FlowListData flowListData = (FlowListData) myList.get(position);
                                                            JSONObject recipe = flowListData.getRecipe();

                                                            // Call the MainActivity to change fragment to the ShowRecipeFragment.
                                                            // onShowRecipeButtonClicked method is implemented in the activity from the OnButtonClicked interface.
                                                            mListener.onShowRecipeButtonClicked(recipe);
                                                        }
                                                    }
        );

        // Click listener for the comment button.
        mViewHolder.commentButton.setOnClickListener(new View.OnClickListener()
                                                     {
                                                         @Override
                                                         public void onClick(View v) {
                                                             // Get the position in the list for the clicked post.
                                                             View parentRow = (View) v.getParent();
                                                             ListView listView = (ListView) parentRow.getParent();
                                                             int position = listView.getPositionForView(parentRow);
                                                             FlowListData flowListData = (FlowListData) myList.get(position);
                                                             String postID = flowListData.getPostID();

                                                             // Call the MainActivity to change fragment to the CommentFragment.
                                                             // OnButtonClicked method is implemented in the activity from the OnButtonClicked interface.
                                                             mListener.onButtonClicked(postID, "CommentFragment");
                                                         }
                                                     }
        );

        // Click listener for the postAuthorView (The username that did the post).
        mViewHolder.postAuthorView.setOnClickListener(new View.OnClickListener()
                                                      {
                                                          @Override
                                                          public void onClick(View v) {
                                                              String userID = null;

                                                              // Get the position in the list for the clicked post.
                                                              View parentRow = (View) v.getParent();
                                                              ListView listView = (ListView) parentRow.getParent();
                                                              int position = listView.getPositionForView(parentRow);
                                                              FlowListData flowListData = (FlowListData) myList.get(position);
                                                              JSONObject recipe = flowListData.getRecipe();

                                                              try {
                                                                  userID = recipe.getString(USER_ID_TAG);
                                                              } catch (JSONException e) {
                                                                  e.printStackTrace();
                                                              }

                                                              if (userID != null) {
                                                                  // Call the  MainActivity to change fragment to the MyPageFragment (profile page).
                                                                  // OnButtonClicked method is implemented in the activity from the OnButtonClicked interface.
                                                                  mListener.onButtonClicked(userID, "MyPageFragment");
                                                              }
                                                          }
                                                      }
        );
        return convertView;
    }

    /**
     * This method will update how many comments there are on one post.
     */
    private void updateCommmentView(String postID, MyViewHolder mViewHolder) {
        String comments;
        JSONArray jsonArray = null;

        // Get all comments on one post.
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

        // Check the arrays length and set the commentView to the same length as the array.
        if (jsonArray != null) {
            if (jsonArray.length() == 0) {
                mViewHolder.displayCommentsView.setText("");
            } else if (jsonArray.length() == 1){
                mViewHolder.displayCommentsView.setText(jsonArray.length() + " " + "Comment");
            } else{
                mViewHolder.displayCommentsView.setText(jsonArray.length() + " " + "Comments");
            }
        }
    }

    /**
     * This method will update how many likes there is on one post.
     */
    private JSONArray updateLikeView(String postID, MyViewHolder mViewHolder) {
        String users;
        JSONArray jsonArray = null;
        try {
            // Get all users that have liked the post.
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

        // Check the arrays length and set the likeViews to the same length as the array.
        if (jsonArray != null) {
            if (jsonArray.length() == 0) {
                mViewHolder.displayLikesView.setText("");
            } else {
                mViewHolder.displayLikesView.setText(jsonArray.length() + " " + "Likes");
            }
            return jsonArray;
        }
        return null;
    }

    /**
     * ViewHolder so we don't need to load the views every time
     * getView method is called.
     */
    private static class MyViewHolder {
        TextView postAuthorView, postInformationView, displayCommentsView,
                displayLikesView, displayLocationView;
        Button recipeButton, likeButton, commentButton;
    }




}
