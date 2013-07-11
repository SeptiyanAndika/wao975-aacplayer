package com.webcraftbd.radio.activity;

import android.os.Bundle;
import com.wao975.aacplayer.R;

public class AboutActivity extends WebviewActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		loadUrl("file:///android_asset/html/about.html", getResources().getString(R.string.menu_about));		 
		//loadUrl("http://www.yoursite.com/about.html");
	}
}
