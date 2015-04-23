package com.example.frewa814.livekrubb;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SearchView;


public class MainActivity extends Activity {

    public final static String URL = "http://livekrubb-frewa814.openshift.ida.liu.se";
    private MenuItem mSearchButton;
    private MenuItem menuItem;
    private final int WAIT_TIME = 2500;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_main);

        // Notice that setContentView() is not used, because we use the root
        // android.R.id.content as the container for each fragment

        // Setup action bar for tabs.
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(false);

            // Add to Fragments to the actionbar tabs.
            ActionBar.Tab tab = actionBar.newTab()
                    .setText(R.string.flow_fragment)
                    .setTag("FLOW_FRAGMENT")
                    .setTabListener(new MyTabListener<>(this, "flow", FlowFragment.class));
            actionBar.addTab(tab);
            tab = actionBar.newTab()
                    .setText(R.string.my_page_fragment)
                    .setTag("MY_PAGE_FRAGMENT")
                    .setTabListener(new MyTabListener<>(this, "my_page", MyPageFragment.class));
            actionBar.addTab(tab);
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

        searchView.setSubmitButtonEnabled(true);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


        return super.onCreateOptionsMenu(menu);

    }

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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();


                        // Check if the activated fragment was the FlowFragment.
                   //     FlowFragment oldFlowFragment = (FlowFragment) fm.findFragmentByTag("FLOW_FRAGMENT");


              /*          if (oldFlowFragment.isVisible()) {
                            // Replace the old FlowFragment with a new one, my type of updating a fragment.
                            FlowFragment updatedFlowFragment = new FlowFragment();
                            ft.replace(R.id.fragment_container, updatedFlowFragment, "FLOW_FRAGMENT");
                            ft.commit();
                        }*/

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
