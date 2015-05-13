package com.example.frewa814.livekrubb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.comment.CommentListData;
import com.example.frewa814.livekrubb.misc.ActivatedUser;
import com.example.frewa814.livekrubb.misc.AutoResizeTextView;
import com.example.frewa814.livekrubb.misc.OnButtonClickedListener;

import java.util.ArrayList;

/**
 * Adapter for the list view in the comment fragment.
 */
public class CommentListAdapter extends BaseAdapter {

    /**
     * This is the list that will be presented in the list view.
     */
    private ArrayList myList = new ArrayList();

    /**
     * Inflater.
     */
    private LayoutInflater inflater;


    /**
     * Constructor for the class that will init the list and the inflater.
     */
    public CommentListAdapter(Context cont, ArrayList myList) {
        this.myList = myList;
        inflater = LayoutInflater.from(cont);
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

    /**
     * This method is loaded every time the user "scroll/load" in one new list item.
     * This will set the right things on the right posts from the myList list.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        // If we are here for the first time we need to find the views in list items.
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.comment_list_item, parent, false);
            mViewHolder = new MyViewHolder();

            // Set up the ViewHolder one time.
            mViewHolder.commentAuthorView = (TextView) convertView.findViewById(R.id.comment_author);
            mViewHolder.commentTextView = (AutoResizeTextView) convertView.findViewById(R.id.comment_text);

            // Store the holder with the view.
            convertView.setTag(mViewHolder);
        } else {
            // Get the viewHolder if we already created one before.
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        // Get the right data from the flowListData that will be displayed on the post.
        CommentListData commentListData = (CommentListData) myList.get(position);

        // Set the text in the views on the post items.
        mViewHolder.commentAuthorView.setText(commentListData.getCommentAuthor());
        mViewHolder.commentTextView.setText(commentListData.getCommentText());

        return convertView;

    }


    /**
     * ViewHolder so we don't need to load the views every time
     * getView method is called.
     */
    private static class MyViewHolder {
        TextView commentAuthorView, commentTextView;
    }
}
