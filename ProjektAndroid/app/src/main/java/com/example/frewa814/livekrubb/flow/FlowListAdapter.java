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
 * Created by Fredrik on 06/04/2015.
 */
public class FlowListAdapter extends BaseAdapter {

    private static final String RESULT_TAG = "result";
    private static final String LIKES_TAG = "likes";
    private static final String USERNAME_TAG = "username";
    ArrayList myList = new ArrayList();
    LayoutInflater inflater;
    Context context;
    private String mActivatedPerson = ActivatedUser.activatedUsername;


    public FlowListAdapter(Context context, ArrayList myList) {
        this.myList = myList;
        this.context = context;
        inflater = LayoutInflater.from(this.context);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.flow_list_item, parent, false);
            mViewHolder = new MyViewHolder();

            // Set up the ViewHolder
            mViewHolder.postAuthorView = (TextView) convertView.findViewById(R.id.post_author);
            mViewHolder.recipeButton = (Button) convertView.findViewById(R.id.recipe_button);
            mViewHolder.postInformationView = (AutoResizeTextView) convertView.findViewById(R.id.post_information);
            mViewHolder.likeButton = (Button) convertView.findViewById(R.id.like_button);
            mViewHolder.displayLikesView = (TextView) convertView.findViewById(R.id.likes_count);
            mViewHolder.displayCommentsView = (TextView) convertView.findViewById(R.id.comments_count);

            // Store the holder with the view.
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        FlowListData flowListData = (FlowListData) myList.get(position);

        mViewHolder.postAuthorView.setText(flowListData.getPostAuthor());
        mViewHolder.postInformationView.setText(flowListData.getPostInformation());
        mViewHolder.recipeButton.setText(flowListData.getRecipeName());

        String postID = flowListData.getPostID();
        JSONArray jsonArray = updateLikeView(postID, mViewHolder);

        Boolean unLikeFlag = false;
        // Check if something was returned from the updateLikeViews.
        if (jsonArray != null) {
            // Check if the one of the likerS is the activated person.
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


        mViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                int position = listView.getPositionForView(parentRow);
                try {
                    JSONObject likedPost = (JSONObject) FlowFragment.posts.get(position);
                    String postId = likedPost.getString("id");
                    MakeLikeTask postTask = new MakeLikeTask(postId, mActivatedPerson);
                    String result = postTask.execute((Void) null).get();

                    if (result.equals("un_liked")) {
                        mViewHolder.likeButton.setText("Like");
                    }
                    else{
                        mViewHolder.likeButton.setText("Unlike");
                    }

                    updateLikeView(postId, mViewHolder);

                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }

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


    private static class MyViewHolder {
        TextView postAuthorView, postInformationView, displayCommentsView, displayLikesView;
        Button recipeButton, likeButton;
    }

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
