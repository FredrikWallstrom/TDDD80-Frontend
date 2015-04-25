package com.example.frewa814.livekrubb.flow;

/**
 * Used to create object with the data that's gonna represent the flow.
 */
public class FlowListData {
    String postAuthor;
    String recipeName;
    String postInformation;
    String postID;

    public String getPostAuthor() {
        return postAuthor;
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

    public void setPostID(String postID){
        this.postID = postID;
    }

    public String getPostID(){
        return postID;
    }
}
