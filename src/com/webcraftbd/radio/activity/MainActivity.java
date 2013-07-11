package com.webcraftbd.radio.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.wao975.aacplayer.R;
import com.webcraftbd.radio.RadioService;
import com.webcraftbd.radio.fragments.AboutFragment;
import com.webcraftbd.radio.fragments.FacebookFragment;
import com.webcraftbd.radio.fragments.RadioFragment;
import com.webcraftbd.radio.fragments.TwitterFragment;

public class MainActivity extends SherlockFragmentActivity
{		
	
	private static boolean displayAd;
	private static boolean isExitMenuClicked;
	private SeekBar volumeSeekbar = null;

	
	private TextView timeTextView;
	
	private boolean wasPlayingBeforePhoneCall = false;
	private RadioService radioService;
	private AdView adView;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		//setContentView(R.layout.activity_main);

		setupActionBar();
	}

	
	public void setupActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		
		//main/radio tab
		actionBar.addTab
		(
			actionBar
			.newTab()
			.setText("Home")
			.setTabListener
			(
				new MainTabListener<RadioFragment>
				(
					this,
					"Home",
					RadioFragment.class
				)
			)
		);
		
		//about tab
		actionBar.addTab
		(
			actionBar
			.newTab()
			.setText("About")
			.setTabListener
			(
				new MainTabListener<AboutFragment>
				(
					this,
					"About",
					AboutFragment.class
				)
			)
		);
		
		//facebook tab
		actionBar.addTab
		(
			actionBar
			.newTab()
			.setText("Facebook")
			.setTabListener
			(
				new MainTabListener<FacebookFragment>
				(
					this,
					"Facebook",
					FacebookFragment.class
				)
			)
		);
		
		//Twitter Tab
		actionBar.addTab
		(
			actionBar
			.newTab()
			.setText("Twitter")
			.setTabListener
			(
				new MainTabListener<TwitterFragment>
				(
					this,
					"Twitter",
					TwitterFragment.class
				)
			)
		);
		
	}
	
	
	public void displayAd()
	{
		if(displayAd==true) {
			// Create the adView
			try {
				
				if (adView != null) {
			      adView.destroy();
			    }
							
				adView = new AdView(this, AdSize.SMART_BANNER, this.getString(R.string.admob_publisher_id));
			    LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
			    layout.addView(adView);
			    adView.loadAd(new AdRequest());
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
			layout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
			layout.setVisibility(View.INVISIBLE);
		}
	}
	
	public void updatePlayTimer()
	{
		timeTextView.setText(radioService.getPlayingTime());
		
		final Handler handler = new Handler();
	    Timer timer = new Timer();
	    TimerTask doAsynchronousTask = new TimerTask()
	    {   
	        @Override
	        public void run()
	        {
	            handler.post(new Runnable()
	            {
	                public void run()
	                {    
	                	timeTextView.setText
	                	(
	                		radioService.getPlayingTime()
	                	);	                	
	                }
	            });
	        }
	    };
	    timer.schedule(doAsynchronousTask, 0, 1000);	    
	}
	
	
	
		
	
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {
	    @Override
	    public void onCallStateChanged(int state, String incomingNumber) {
	        if (state == TelephonyManager.CALL_STATE_RINGING) {
	        	wasPlayingBeforePhoneCall = radioService.isPlaying();
	            radioService.stop();
	        } else if(state == TelephonyManager.CALL_STATE_IDLE) {
	        	if(wasPlayingBeforePhoneCall) {
	            	radioService.play();
	            }
	        } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
	            //A call is dialing, active or on hold
	        	wasPlayingBeforePhoneCall = radioService.isPlaying();
	        	radioService.stop();
	        }
	        super.onCallStateChanged(state, incomingNumber);
	    }
	};
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			int index = volumeSeekbar.getProgress();
			volumeSeekbar.setProgress(index + 1);
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			int index = volumeSeekbar.getProgress();
			volumeSeekbar.setProgress(index - 1);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	
	
	
	public static class MainTabListener<T extends SherlockFragment> implements TabListener
	{

		private 		Fragment	mFragment;
		private final 	Activity	mActivity;
		private final 	String 		mTag;
		private final 	Class<T> 	mClass;

		/** Constructor used each time a new tab is created.
		 * @param activity  The host Activity, used to instantiate the fragment
		 * @param tag  The identifier tag for the fragment
		 * @param clz  The fragment's Class, used to instantiate the fragment
		 */
		public MainTabListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			try
			{
			// Check if the fragment is already initialized
			if (mFragment == null) {
				Log.d("test test", "fragment null");
				// If not, instantiate and add it to the activity
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				Log.d("test test" , "fragment not null");
				// If it exists, simply attach it in order to show it
				ft.attach(mFragment);
			}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				// Detach the fragment, because another one is being attached
				ft.detach(mFragment);
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// User selected the already selected tab. Usually do nothing.
		}


	}

}
