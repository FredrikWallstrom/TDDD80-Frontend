package com.example.frewa814.livekrubb.flow;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.frewa814.livekrubb.asynctask.GetTask;
import com.example.frewa814.livekrubb.activity.MainActivity;
import com.example.frewa814.livekrubb.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fredrik on 06/04/2015.
 */
public class FlowFragment extends ListFragment {

    private static final String POST_TAG = "posts";
    private static final String RECIPE_NAME_TAG = "recipe_name";
    private static final String POST_INFORMATION_TAG = "post_information";
    private static final String POST_AUTHOR_ID_TAG = "user_id";
    private static final String USERNAME_TAG = "username";
    private final static String USER_TAG = "user";
    private static final String ID_TAG = "id";
    public static JSONArray posts;

    private List<String> recipeNameList;
    private List<String> postInformationList;
    private List<String> postAuthorList;
    private List<String> postIDList;
    private ArrayList myList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.flow_list, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDataInList();
        FlowListAdapter adapter = new FlowListAdapter(getActivity(), myList);

        setListAdapter(adapter);
    }

    private void getDataInList() {
        recipeNameList = new ArrayList<>();
        postInformationList = new ArrayList<>();
        postAuthorList = new ArrayList<>();
        postIDList = new ArrayList<>();
        myList = new ArrayList();


        try {
            posts = getAllPosts();
            if (posts != null) {
                if (posts.length() != 0) {
                    posts = getPostsSorted(posts);

                    for (int i = 0; i < posts.length(); i++) {
                        JSONObject object = posts.getJSONObject(i);

                        String recipeName = object.getString(RECIPE_NAME_TAG);
                        String postInformation = object.getString(POST_INFORMATION_TAG);
                        String postAuthorID = object.getString(POST_AUTHOR_ID_TAG);
                        String postID = object.getString(ID_TAG);

                        String postAuthor = getPostAuthor(postAuthorID);

                        postIDList.add(postID);
                        recipeNameList.add(recipeName);
                        postInformationList.add(postInformation);
                        postAuthorList.add(postAuthor);
                    }

                } else {
                    Toast serverError = Toast.makeText(getActivity(), "Your flow is empty. Search some friends and follow them!", Toast.LENGTH_LONG);
                    serverError.show();
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

            // Add this object into the ArrayList myList
            myList.add(flowListData);
            loopInteger++;
        }
    }

    private JSONArray getPostsSorted(JSONArray posts) {
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i <posts.length(); i++)
            try {
                jsonValues.add(posts.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                String valA = new String();
                String valB = new String();
                try {
                    valA = lhs.getString("timestamp");
                    valB = rhs.getString("timestamp");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int comp = valA.compareTo(valB);
                if(comp > 0)
                    return -1;
                if(comp < 0)
                    return 1;
                return 0;
            }
        });
        JSONArray sortedJsonArray = new JSONArray(jsonValues);

        return sortedJsonArray;
    }

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

    private JSONArray getAllPosts() {
        String result;
        JSONObject jsonObject;
        JSONArray jsonArray;
        try {
            result = new GetTask().execute(MainActivity.URL + "/all_posts").get();
        } catch (InterruptedException | ExecutionException e) {
            result = "server error";
            e.printStackTrace();
        }

        if (!result.equals("server error")) {
            try {
                jsonObject = new JSONObject(result);
                jsonArray = jsonObject.getJSONArray(POST_TAG);
                return jsonArray;
            } catch (JSONException e) {
                JSONArray emptyFlow = new JSONArray();
                return emptyFlow;
            }
        } else {
            return null;
        }

    }
}


