package com.webcraftbd.radio.activity;

import android.os.Bundle;
import com.wao975.aacplayer.R;

public class FacebookActivity extends WebviewActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		loadUrl("http://m.facebook.com/"+getResources().getString(R.string.facebook_id), getResources().getString(R.string.menu_facebook));		 
	}
	
}
