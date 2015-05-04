package com.example.frewa814.livekrubb.flow;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.AutoResizeTextView;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Adapter for the FlowFragment.
 */
public class FlowListAdapter extends BaseAdapter {

    private static final String RESULT_TAG = "result";
    private static final String LIKES_TAG = "likes";
    private static final String USERNAME_TAG = "username";
    private ArrayList myList = new ArrayList();
    private LayoutInflater inflater;
    private String mActivatedPerson = ActivatedUser.activatedUsername;
    OnButtonClickedListener mListener;


    public FlowListAdapter(Context cont, ArrayList myList) {
        this.myList = myList;
        Context context = cont;
        inflater = LayoutInflater.from(context);

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
     * This will run every time there is a post that's will need to be loaded.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

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

            // Store the holder with the view.
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        // Get the right data from the flowListData that will be displayed on the post.
        FlowListData flowListData = (FlowListData) myList.get(position);

        // Set the text in the views on the post items.
        mViewHolder.postAuthorView.setText(flowListData.getPostAuthor());
        mViewHolder.postInformationView.setText(flowListData.getPostInformation());
        mViewHolder.recipeButton.setText(flowListData.getRecipeName());

        // Get the postID
        String postID = flowListData.getPostID();
        // Update how many likes there is on the post.
        JSONArray jsonArray = updateLikeView(postID, mViewHolder);

        Boolean unLikeFlag = false;
        // Check if something was returned from the updateLikeViews.
        if (jsonArray != null) {
            // Check if one of the likerS is the activated person.
            for (int e = 0; e < jsonArray.length(); e++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(e);
                    String username = jsonObject.getString(USERNAME_TAG);
                    if (username.equals(mActivatedPerson)) {
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

                try {
                    // Make an like or unlike depend what the user want
                    // (the database will check if the user is liking the post already).
                    JSONObject likedPost = (JSONObject) FlowFragment.posts.get(position);
                    String postId = likedPost.getString("id");
                    MakeLikeTask postTask = new MakeLikeTask(postId, mActivatedPerson);
                    String result = postTask.execute((Void) null).get();

                    // Check if the user want to unlike or like and set the right text to the button.
                    if (result.equals("un_liked")) {
                        mViewHolder.likeButton.setText("Like");
                    }
                    else{
                        mViewHolder.likeButton.setText("Unlike");
                    }

                    // Update the likeView.
                    updateLikeView(postId, mViewHolder);

                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mViewHolder.recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                int position = listView.getPositionForView(parentRow);

                JSONObject recipe = null;

                try {
                    recipe = (JSONObject) FlowFragment.posts.get(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mListener.onShowRecipeButtonClicked(recipe);
            }
        });

        mViewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                int position = listView.getPositionForView(parentRow);

                String postId = null;

                try {
                    JSONObject post = (JSONObject) FlowFragment.posts.get(position);
                    postId = post.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mListener.onCommentButtonClicked(postId);
            }
        });

        mViewHolder.postAuthorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                int position = listView.getPositionForView(parentRow);

                String userId = null;

                try {
                    JSONObject post = (JSONObject) FlowFragment.posts.get(position);
                    userId = post.getString("user_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mListener.onMyPageClicked(userId);
            }
        });
        return convertView;
    }


    private JSONArray updateLikeView(String postID, MyViewHolder mViewHolder) {
        String likerS;
        JSONObject jsonObject;
        JSONArray jsonArray = null;
        try {
            likerS = new GetTask().execute(MainActivity.URL + "/all_likes_on_post/" + postID).get();
        } catch (InterruptedException | ExecutionException e) {
            likerS = "server error";
            e.printStackTrace();
        }

        if (!likerS.equals("server error")) {
            try {
                jsonObject = new JSONObject(likerS);
                jsonArray = jsonObject.getJSONArray(LIKES_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonArray != null){
            if (jsonArray.length() == 0) {
                mViewHolder.displayLikesView.setText("");
            }
            else {
                mViewHolder.displayLikesView.setText(jsonArray.length() + " " + "Likes");
            }
            return jsonArray;
        }
        return null;
    }

    /**
     * ViewHolder for all views in one list item.
     */
    private static class MyViewHolder {
        TextView postAuthorView, postInformationView, displayCommentsView, displayLikesView;
        Button recipeButton, likeButton, commentButton;
    }

    /**
     * This method will make a post to the database and like or unlike one post
     * depend if the user already like the post or not.
     */
    public String makePost(String postId, String liker) {
        InputStream inputStream;
        String result;
        try {
            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // Make makePost request to the given URL
            HttpPost httpPost = new HttpPost(MainActivity.URL + "/like_post");

            String json;

            // Build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("postid", postId);
            jsonObject.accumulate("liker", liker);

            // Convert JSONObject to JSON to String
            json = jsonObject.toString();

            // Set json to StringEntity
            StringEntity se = new StringEntity(json);

            // Set httpPost Entity
            httpPost.setEntity(se);

            // Execute makePost request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // Receive response as inputStream.
            inputStream = httpResponse.getEntity().getContent();

            // Convert the inputStream to string.
            result = convertInputStreamToString(inputStream);

        } catch (Exception e) {
            result = "server error";
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Private task class that will be running in the background,
     * when it is done it will return the result to the onClickListener for the like button.
     * @result will be "liked" or "un_liked" depend on what the database has did.
     */
    private class MakeLikeTask extends AsyncTask<Void, Void, String> {
        String postId;
        String liker;

        MakeLikeTask(String postId, String liker) {
            this.postId = postId;
            this.liker = liker;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            String dict = makePost(postId, liker);

            try {
                JSONObject jsonObject = new JSONObject(dict);
                result = jsonObject.getString(RESULT_TAG);
            } catch (JSONException e) {
                result = "server error";
                e.printStackTrace();
            }
            return result;
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
