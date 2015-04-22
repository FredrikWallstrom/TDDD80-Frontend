package com.example.frewa814.livekrubb;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;


public class MainActivity extends Activity {

    public final static String URL = "http://livekrubb-frewa814.openshift.ida.liu.se";
    private MenuItem mSearchButton;
    private MenuItem menuItem;
    private final int WAIT_TIME = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {
            // Create a new Fragment to be placed in the activity layout
            FlowFragment flowFragment = new FlowFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, flowFragment, "FLOW_FRAGMENT")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);


        // Find the buttons in the action bar.
        mSearchButton = menu.findItem(R.id.action_search);


        // Find the search view for the search button.
        SearchView searchView = (SearchView) mSearchButton.getActionView();

        // Display a hint on the searchView.
        searchView.setQueryHint("Search User");

        // Add a listener to the search button.
        searchView.setOnQueryTextListener(searchListener);



        return super.onCreateOptionsMenu(menu);

    }


    // Listener for the search button.
    SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String arg0) {
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            // TODO query will be the entered name, so make an http get request here.

            try {
                mSearchButton.collapseActionView();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        switch (id) {
            // Case Refresh button
            case R.id.action_refresh:
                // Start a progressbar instead of the update icon.
                menuItem = item;
                menuItem.setActionView(R.layout.progressbar_refreshbutton);
                menuItem.expandActionView();

                // Handler that wait for 2,5 sec, and than execute the new fragment.
                // Just to inform the users that's something happend.
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();

                        // Check if the activated fragment was the FlowFragment.
                        FlowFragment oldFlowFragment = (FlowFragment) fm.findFragmentByTag("FLOW_FRAGMENT");
                        if (oldFlowFragment.isVisible()) {
                            // Replace the old FlowFragment with a new one, my type of updating a fragment.
                            FlowFragment updatedFlowFragment = new FlowFragment();
                            ft.replace(R.id.fragment_container, updatedFlowFragment, "FLOW_FRAGMENT");
                            ft.commit();
                        }
                        // Stop the progressbar and return the update icon.
                        menuItem.collapseActionView();
                        menuItem.setActionView(null);
                    }
                }, WAIT_TIME);
                break;
        }
        return true;
    }
}
