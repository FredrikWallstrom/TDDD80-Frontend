package com.example.frewa814.livekrubb.flow;

import org.json.JSONObject;

/**
 * Used to create object with the data that's gonna represent the flow in three pages.
 * PublicFlowFragment, MyPageFragment and FollowersFlowListFragment.
 * All three is using same list items and therefore same listAdapter (FlowListAdapter).
 */
public class FlowListData {
    String postAuthor;
    String recipeName;
    String postInformation;
    String postID;
    JSONObject recipe;
    String location;

    public String getPostAuthor() {
        return postAuthor;
    }

    public JSONObject getRecipe() {
        return recipe;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public String getPostInformation() {
        return postInformation;
    }

    public void setPostAuthor(String postAuthor) {
        this.postAuthor = postAuthor;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setPostInformation(String postInformation) {
        this.postInformation = postInformation;
    }

    public void setRecipe(JSONObject recipe){
        this.recipe = recipe;
    }

    public void setPostID(String postID){
        this.postID = postID;
    }

    public String getPostID(){
        return postID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
