package com.example.frewa814.livekrubb;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;


public class MainActivity extends Activity {

    private MenuItem mMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
       //     if (savedInstanceState != null) {
       //         return;
       //     }
            // Create a new Fragment to be placed in the activity layout
            FlowFragment flowFragment = new FlowFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, flowFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Find the search button in the action bar.
        mMenuItem = menu.findItem(R.id.action_search);

        // Find the search view for the button.
        SearchView searchView = (SearchView) mMenuItem.getActionView();

        // Display a hint on the searchView.
        searchView.setQueryHint("Search User");

        // Add a listener to the search button.
        searchView.setOnQueryTextListener(searchListener);

        return super.onCreateOptionsMenu(menu);

    }

    // Listener for the search button.

    SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener(){
        @Override
        public boolean onQueryTextChange(String arg0) {
            return false;
        }
        @Override
        public boolean onQueryTextSubmit(String query) {
            // TODO query will be the entered name, so make an http get request here.

            try {
                mMenuItem.collapseActionView();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            return true;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
}
