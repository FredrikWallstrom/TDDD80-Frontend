package com.example.frewa814.livekrubb.asynctask;

import android.os.AsyncTask;

import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.misc.ActivatedUser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A class that is used when we want to do add one comment to the database.
 */
public class CommentTask extends AsyncTask<Void, Void, String> {

    /**
     * Constant tags for http requests.
     */
    private static final String RESULT_TAG = "result";

    /**
     * This fields will represent which post the user want to add a comment on
     * and what text the user want to add.
     */
    private String mPostID;
    private String mCommentText;

    /**
     * Constructor for this class that will init the postID and the commentText
     */
    public CommentTask(String postID, String commentText) {
        this.mPostID = postID;
        this.mCommentText = commentText;
    }

    @Override
    protected String doInBackground(Void... params) {
        String result;
        String dict = makePost();

        try {
            JSONObject jsonObject = new JSONObject(dict);
            result = jsonObject.getString(RESULT_TAG);
        } catch (JSONException e) {
            result = "server error";
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Do a http request and make a post to the database with the comment information.
     */
    public String makePost() {
        InputStream inputStream;
        String result;
        try {
            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // Make makePost request to the given URL
            HttpPost httpPost = new HttpPost(MainActivity.URL + "/add_comment");
            String json;
            // Build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("post_id", mPostID);
            jsonObject.accumulate("user_id", ActivatedUser.activatedUserID);
            jsonObject.accumulate("comment_text", mCommentText);
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
     * Convert the inputStream to a readable string.
     */
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
