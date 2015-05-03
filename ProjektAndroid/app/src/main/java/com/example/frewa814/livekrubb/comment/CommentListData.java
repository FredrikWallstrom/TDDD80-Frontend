package com.example.frewa814.livekrubb.comment;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Fredrik on 2015-05-01.
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
