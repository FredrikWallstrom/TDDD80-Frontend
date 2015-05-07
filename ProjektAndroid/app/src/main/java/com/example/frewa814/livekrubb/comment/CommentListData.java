package com.example.frewa814.livekrubb.comment;

/**
 * Used to create object with the data that's gonna represent the flow in the comment page.
 */
public class CommentListData {

    String CommentAuthor;
    String CommentText;

    public String getCommentAuthor() {
        return CommentAuthor;
    }

    public String getCommentText() {
        return CommentText;
    }

    public void setCommentAuthor(String commentAuthor) {
        CommentAuthor = commentAuthor;
    }

    public void setCommentText(String commentText) {
        CommentText = commentText;
    }
}
