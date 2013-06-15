package com.webcraftbd.radio;

import android.os.Bundle;
import com.wao975.aacplayer.R;

public class TwitterActivity extends WebviewActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		loadUrl("http://mobile.twitter.com/"+getResources().getString(R.string.twitter_id), getResources().getString(R.string.menu_twitter));		 
	}
}
