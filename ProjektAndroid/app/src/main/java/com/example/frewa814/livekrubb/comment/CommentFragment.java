package com.example.frewa814.livekrubb.comment;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.adapters.CommentListAdapter;
import com.example.frewa814.livekrubb.asynctask.CommentTask;
import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Fragment class that is gonna display the comment side in the application.
 */
public class CommentFragment extends ListFragment {

    /**
     * Constant tags for http requests.
     */
    private static final String USER_TAG = "user";
    private static final String USERNAME_TAG = "username";
    private static final String COMMENTS_TAG = "comments";
    private static final String USER_ID_TAG = "user_id";
    private static final String COMMENT_TEXT_TAG = "comment_text";

    /**
     * Click listener instance that will point on MainActivity
     * so the MainActivity can handle the clicks in the fragments.
     */
    OnButtonClickedListener mListener;

    /**
     * This is the list that will be presented in the list view.
     */
    private ArrayList<CommentListData> myList;

    /**
     * This field is representing which post we are on.
     */
    private String mPostID;

    /**
     * This view is the view where the user can add a comment if he wants.
     */
    private EditText mCommentView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Hide the actionBar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
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
        // Inflate the layout for this fragment.
        View rootView = inflater.inflate(R.layout.comments, container, false);

        // Set click listener for the buttons in the xml (back button and add comment button).
        Button commentButton = (Button) rootView.findViewById(R.id.comment);
        ImageView backButton = (ImageView) rootView.findViewById(R.id.back_button);
        backButton.setOnClickListener(clickListener);
        commentButton.setOnClickListener(clickListener);

        // Get the EditTextView for where the user can enter a comment.
        mCommentView = (EditText) rootView.findViewById(R.id.comment_edit_text_view);

        return rootView;
    }

    /**
     * Click listener for the back button and the button that will add a comment.
     * Checks which button is clicked and than do the right thing.
     * If the back button is clicked we gonna notify the MainActivity
     * and then the activity will handle it.
     */
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String result;

            // Hide the keyboard so the user can see the whole display.
            hideKeyboard();

            // Reset errors.
            mCommentView.setError(null);

            // Check if the clicked button was the add comment button.
            if (view.getId() == R.id.comment) {

                // Get the comment text that the user entered.
                String mCommentText = mCommentView.getText().toString();

                // Check if the user entered a valid comment.
                if (commentTextIsValid(mCommentText)){
                    // Make a CommentTask that will save the comment in the database.
                    try {
                        CommentTask task = new CommentTask(mPostID, mCommentText);
                        result = task.execute().get();
                    } catch (InterruptedException | ExecutionException e) {
                        result = "server error";
                        e.printStackTrace();
                    }
                    // Check if everything went OK in the AsyncTask.
                    if (!result.equals("server error")) {
                        // Refresh the fragment so the user can see the comment he just added.
                        refreshFragment();
                    }
                }
                // If the user don't entered a valid Comment,
                // notify the user by set error on the commentView.
                else {
                    mCommentView.setError("Have you entered a comment?, check if it not only contains spaces");
                    mCommentView.requestFocus();
                }
            }
            // If the clicked button don't was the add comment button
            // It was the back button, and then "send" the click to MainActivity.
            else {
                mListener.onButtonClicked(view);
            }
        }
    };

    /**
     * Check if the entered comment text is valid or not.
     */
    private boolean commentTextIsValid(String commentText) {
        return !commentText.trim().isEmpty() && !commentText.isEmpty();
    }

    /**
     * Running when creating the fragment.
     * It will run the getDataInList method to get all the data that gonna
     * represent the listView.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get the post id from the bundle so we know which commentPage
        // we gonna display.
        mPostID = getArguments().getString("id");

        // Get all posts in the database that's gonna represent the flow.
        getDataInList();

        // Make custom adapter and set it to the listView.
        CommentListAdapter adapter = new CommentListAdapter(getActivity(), myList);
        setListAdapter(adapter);
    }

    /**
     * This method will get all data that is gonna represent the flow.
     * It will add the data to a temp lists and then create one CommentListData
     * object for every items in the temp list.
     * And after that it will add the object to the list that will
     * be sent to the CommentListAdapter.
     */
    private void getDataInList() {
        JSONArray comments;

        // Temp lists there we will save the comment text and the commentAuthor.
        List<String> commentAuthorList = new ArrayList<>();
        List<String> commentTextList = new ArrayList<>();

        // The list that will be sent to the commentListAdapter.
        myList = new ArrayList<>();

        try {
            comments = getAllComments();
            if (comments != null) {
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject object = comments.getJSONObject(i);
                    String commentAuthorID = object.getString(USER_ID_TAG);
                    String commentText = object.getString(COMMENT_TEXT_TAG);

                    // Get the username on the commentAuthor.
                    String commentAuthor = getCommentAuthor(commentAuthorID);

                    // Add the username and the commentText to the temp lists.
                    commentAuthorList.add(commentAuthor);
                    commentTextList.add(commentText);
                }
            }
            // If there is problem with the server, make a toast to inform the user.
            else {
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

            // Add this object into the ArrayList myList that gonna represent the commentFlow.
            myList.add(commentListData);
            loopInteger++;
        }
    }

    /**
     * This method will get all comments that are displayed on the right post.
     */
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
                jsonArray = jsonObject.getJSONArray(COMMENTS_TAG);
                return jsonArray;
            } catch (JSONException e) {
                return new JSONArray();
            }
        } else {
            return null;
        }
    }

    /**
     * This method will get the comment authors username from the database from a given user id.
     */
    private String getCommentAuthor(String commentAuthorID) {
        String user;
        String commentAuthorName = null;
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            user = new GetTask().execute(MainActivity.URL + "/get_user_by_id/" + commentAuthorID).get();
        } catch (InterruptedException | ExecutionException e) {
            user = "server error";
            e.printStackTrace();
        }

        if (!user.equals("server error")) {
            try {
                jsonObject = new JSONObject(user);
                jsonArray = jsonObject.getJSONArray(USER_TAG);
                jsonObject = jsonArray.getJSONObject(0);
                commentAuthorName = jsonObject.getString(USERNAME_TAG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return commentAuthorName;
    }

    /**
     * This method is used when the user want to refresh a fragment.
     * It will load the right data again in the getDataInList method
     * and after that set a new list adapter.
     * It will also reset the comment editTextView if the user just added one post.
     */
    public void refreshFragment() {
        getDataInList();
        mCommentView.setText("");
        CommentListAdapter adapter = new CommentListAdapter(getActivity(), myList);
        setListAdapter(adapter);
    }

    /**
     * Calling this when i want to hide the keyboard.
     */
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}



