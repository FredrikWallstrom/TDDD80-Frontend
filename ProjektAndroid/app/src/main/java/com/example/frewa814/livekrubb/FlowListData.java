package com.example.frewa814.livekrubb;

/**
 * Created by Fredrik on 2015-04-09.
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
