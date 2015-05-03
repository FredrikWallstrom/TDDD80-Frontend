package com.example.frewa814.livekrubb.comment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.AutoResizeTextView;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import java.util.ArrayList;

/**
 * Created by Fredrik on 2015-05-01.
 */
public class CommentListAdapter extends BaseAdapter {
    private static final String RESULT_TAG = "result";
    private static final String LIKES_TAG = "likes";
    private static final String USERNAME_TAG = "username";
    private ArrayList myList = new ArrayList();
    private LayoutInflater inflater;
    private String mActivatedPerson = ActivatedUser.activatedUsername;
    OnButtonClickedListener mListener;


    public CommentListAdapter(Context cont, ArrayList myList) {
        this.myList = myList;
        Context context = cont;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return myList.size();
    }

    @Override
    public CommentListData getItem(int position) {
        return (CommentListData) myList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.comment_list_item, parent, false);
            mViewHolder = new MyViewHolder();

            // Set up the ViewHolder one time.
            mViewHolder.commentAuthorView = (TextView) convertView.findViewById(R.id.comment_author);
            mViewHolder.commentTextView = (AutoResizeTextView) convertView.findViewById(R.id.comment_text);

            // Store the holder with the view.
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        // Get the right data from the flowListData that will be displayed on the post.
        CommentListData commentListData = (CommentListData) myList.get(position);

        // Set the text in the views on the post items.
        mViewHolder.commentAuthorView.setText(commentListData.getCommentAuthor());
        mViewHolder.commentTextView.setText(commentListData.getCommentText());

        return convertView;

    }


    private static class MyViewHolder {
        TextView commentAuthorView, commentTextView;
    }
}
