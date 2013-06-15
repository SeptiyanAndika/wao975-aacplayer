package com.webcraftbd.radio;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import com.wao975.aacplayer.R;

public class TabContainer extends TabActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_container);
 
        //TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        TabHost tabHost = getTabHost();
 
        // Tab for Radio
        TabSpec radioTabSpec = tabHost.newTabSpec("Radio Online");
        // setting Title and Icon for the Tab
        radioTabSpec.setIndicator("Radio Online", getResources().getDrawable(R.drawable.menu_radio));
        Intent radioIntent = new Intent(this, MainActivity.class);
        radioTabSpec.setContent(radioIntent);
 
        // Tab for Facebook
        TabSpec facebookTabSpec = tabHost.newTabSpec("Facebook");
        facebookTabSpec.setIndicator("Facebook", getResources().getDrawable(R.drawable.menu_facebook));
        Intent facebookIntent = new Intent(this, FacebookActivity.class);
        facebookTabSpec.setContent(facebookIntent);
 
        // Tab for Twitter
        TabSpec twitterTabSpec = tabHost.newTabSpec("Twitter");
        twitterTabSpec.setIndicator("Twitter", getResources().getDrawable(R.drawable.menu_twitter));
        Intent twitterIntent = new Intent(this, TwitterActivity.class);
        twitterTabSpec.setContent(twitterIntent);
        
        // Tab for About
        TabSpec aboutTabSpec = tabHost.newTabSpec("Acerca");
        aboutTabSpec.setIndicator("Acerca", getResources().getDrawable(R.drawable.menu_about));
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        aboutTabSpec.setContent(aboutIntent);
        // Adding all TabSpec to TabHost
     
        tabHost.addTab(radioTabSpec); // Adding photos tab
        tabHost.addTab(facebookTabSpec); // Adding songs tab
        tabHost.addTab(twitterTabSpec); // Adding videos tab
        tabHost.addTab(aboutTabSpec); // Adding videos tab
    }

}
