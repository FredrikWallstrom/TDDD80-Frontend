package com.example.frewa814.livekrubb.comment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fredrik on 2015-05-01.
 */
public class CommentFragment extends ListFragment {

    private static final String USER_TAG = "user";
    private static final String USERNAME_TAG = "username";
    private static final String RESULT_TAG = "result";
    OnButtonClickedListener mListener;

    private static final String COMMENT_TAG = "comments";
    private static final String USER_ID_TAG = "user_id";
    private static final String COMMENT_TEXT_TAG = "comment_text";

    private EditText mCommentView;
    private ArrayList myList;

    private String mCommentText;
    private String mPostID;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Hide the actionBar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

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
        View rootView = inflater.inflate(R.layout.comments, container, false);

        // Set button listener for the share recipe button.
        Button commentButton = (Button) rootView.findViewById(R.id.comment);
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_button);
        backButton.setOnClickListener(clickListener);
        commentButton.setOnClickListener(clickListener);

        mCommentView = (EditText) rootView.findViewById(R.id.comment_edit_text_view);

        return rootView;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideKeyboard();
            if (view.getId() == R.id.comment) {
                mCommentText = mCommentView.getText().toString();
                String result;

                try {
                    MakeCommentTask task = new MakeCommentTask();
                    result = task.execute().get();
                    System.out.println(result);
                } catch (InterruptedException | ExecutionException e) {
                    result = "server error";
                    e.printStackTrace();
                }

                if (!result.equals("server error")){
                    mListener.onCommentButtonClicked(mPostID);
                }

            } else {
                mListener.onButtonClicked(view);
            }
        }
    };

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


         mPostID = getArguments().getString("post_id");

        // Get all posts in the database that's gonna represent the flow.
        getDataInList();

        // Make custom adapter and set it to the listview.
        CommentListAdapter adapter = new CommentListAdapter(getActivity(), myList);
        setListAdapter(adapter);
    }

    private void getDataInList() {
        List<String> commentAuthorList = new ArrayList<>();
        List<String> commentTextList = new ArrayList<>();
        myList = new ArrayList();
        JSONArray comments;

        try {
            comments = getAllComments();
            if (comments != null) {
                if (comments.length() != 0) {
                    comments = getCommentsSorted(comments);


                    for (int i = 0; i < comments.length(); i++) {
                        JSONObject object = comments.getJSONObject(i);

                        String commentAuthorID = object.getString(USER_ID_TAG);
                        String commentText = object.getString(COMMENT_TEXT_TAG);

                        String commentAuthor = getCommentAuthor(commentAuthorID);

                        commentAuthorList.add(commentAuthor);
                        commentTextList.add(commentText);
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
        while (commentAuthorList.size() > loopInteger) {

            // Create a new object for each list item
            CommentListData commentListData = new CommentListData();
            commentListData.setCommentAuthor(commentAuthorList.get(loopInteger));
            commentListData.setCommentText(commentTextList.get(loopInteger));

            // Add this object into the ArrayList myList
            myList.add(commentListData);
            loopInteger++;
        }
    }


    private JSONArray getAllComments() {
        String comments;
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            comments = new GetTask().execute(MainActivity.URL + "/all_comments_on_post/" + mPostID).get();
        } catch (InterruptedException | ExecutionException e) {
            comments = "server error";
            e.printStackTrace();
        }

        if (!comments.equals("server error")) {
            try {
                jsonObject = new JSONObject(comments);
                jsonArray = jsonObject.getJSONArray(COMMENT_TAG);
                return jsonArray;
            } catch (JSONException e) {
                return new JSONArray();
            }
        } else {
            return null;
        }
    }

    private JSONArray getCommentsSorted(JSONArray comments) {
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i <comments.length(); i++)
            try {
                jsonValues.add(comments.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                String valA = "";
                String valB = "";
                try {
                    valA = lhs.getString("timestamp");
                    valB = rhs.getString("timestamp");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int comp = valA.compareTo(valB);
                if (comp > 0)
                    return -1;
                if (comp < 0)
                    return 1;
                return 0;
            }
        });
        return new JSONArray(jsonValues);
    }

    private String getCommentAuthor(String commentAuthorID) {
        String postAuthor;
        String postAuthorName = null;
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            postAuthor = new GetTask().execute(MainActivity.URL + "/get_user_by_id/" + commentAuthorID).get();
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


    private class MakeCommentTask extends AsyncTask<Void, Void, String> {

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



