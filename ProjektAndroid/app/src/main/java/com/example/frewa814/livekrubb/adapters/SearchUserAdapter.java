package com.example.frewa814.livekrubb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.frewa814.livekrubb.R;
import java.util.List;

/**
 * CursorAdapter for the dropdown list for the search action in the actionbar.
 */
public class SearchUserAdapter extends CursorAdapter {

    /**
     * TextView there we gonna represent the names in the cursor.
     */
    private TextView text;

    /**
     * This is the list that will be presented in the cursor list.
     */
    private List<String> items;

    /**
     * Constructor for the class that will init items that gonna represent the list in the cursor.
     */
    public SearchUserAdapter(Context context, Cursor cursor, List<String> items) {
        super(context, cursor, false);
        this.items = items;
    }

    /**
     * This method will set the text in the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        text.setText(items.get(cursor.getPosition()));
    }

    /**
     * This method will inflate the xml and find the textView.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_item, parent, false);
        text = (TextView) view.findViewById(R.id.text);
        return view;
    }
}
