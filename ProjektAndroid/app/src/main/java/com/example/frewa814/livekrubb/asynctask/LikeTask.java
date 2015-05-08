package com.example.frewa814.livekrubb.asynctask;

import android.os.AsyncTask;

import com.example.frewa814.livekrubb.activity.MainActivity;

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
 * Private task class that will be running in the background,
 * when it is done it will return the result to the onClickListener for the like button.
 *
 * @result will be "liked" or "un_liked" depend on what the database has did.
 */
public class LikeTask extends AsyncTask<Void, Void, String> {
    private static final String RESULT_TAG = "result";
    String postId;
        String liker;

        public LikeTask(String postId, String liker) {
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



    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
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
}
