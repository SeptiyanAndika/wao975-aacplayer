package com.webcraftbd.radio.fragments;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.wao975.aacplayer.R;
import com.webcraftbd.radio.LastFMCover;
import com.webcraftbd.radio.RadioService;

public class RadioFragment extends SherlockFragment
{
	
	private static boolean displayAd;
	private static boolean isExitMenuClicked;
	private SeekBar volumeSeekbar = null;

	private Button playButton;
	private Button pauseButton;
	private Button stopButton;
	private Button nextButton;
	private Button previousButton;
	private ImageView stationImageView;
	
	private TextView albumTextView;
	private TextView artistTextView;
	private TextView trackTextView;
	private TextView statusTextView;	
	private TextView timeTextView;
	private AudioManager audioManager = null;

	private Intent bindIntent;
	private TelephonyManager telephonyManager;
	private boolean wasPlayingBeforePhoneCall = false;
	private RadioUpdateReceiver radioUpdateReceiver;
	private RadioService radioService;
	private AdView adView;
	
	private String STATUS_BUFFERING;
	private static final String TYPE_AAC = "aac";
	private static final String TYPE_MP3 = "mp3";
	
	private Handler handler;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		
		telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		if(telephonyManager != null)
		{
			telephonyManager.listen
			(
				phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE
			);
		}
		
		handler = new Handler();
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_radio_player, container, false);
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		initialize(view);

		initControls();
		bindToService();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initControls();
		if
		(
			newConfig.orientation == Configuration.ORIENTATION_PORTRAIT || 
			newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
		)
		{
			//setContentView(R.layout.activity_main);
			
			try
			{
				handler.post( new Runnable()
				{
	                public void run()
	                {
	                	//initialize(getActivity().findViewById(R.id.fragment_container));
				
						if(radioService.getTotalStationNumber() <= 1)
						{
							nextButton		.setEnabled(false);
							nextButton		.setVisibility(View.INVISIBLE);
							previousButton	.setEnabled(false);
							previousButton	.setVisibility(View.INVISIBLE);
						}
						
						updateStatus();
						updateMetadata();
						updateAlbum();
								
						System.out.println
						(
							"radioService.isPreparingStarted() = " + 
							radioService.isPreparingStarted()
						);							
	                }
				});
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
		
		if(radioService!=null)
		{
			if
			(
				!radioService.isPlaying() &&
				!radioService.isPreparingStarted()
			)
			{
				//radioService.stopSelf();
				radioService.stop();

				radioService.stopService(bindIntent);
			}
		}	    	
		
	    if (adView != null)
	    {
	      adView.destroy();
	    }
	    
	    if(telephonyManager != null)
	    {
	    	telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
	    }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (radioUpdateReceiver != null) 
			getActivity().unregisterReceiver(radioUpdateReceiver);
	}
	
	@Override
	public void onResume() {		
		super.onResume();
	
	}

	public void bindToService()
	{
		try
		{
			bindIntent = new Intent
			(
				getActivity().getApplicationContext(),
				RadioService.class
			);
			getActivity().getApplicationContext().bindService
			(
				bindIntent,
				radioConnection,
				Context.BIND_AUTO_CREATE
			);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	

	public void registerReceiver()
	{
		Log.d("BROADCAST", "registering receiver");
		/* Register for receiving broadcast messages */
		if (radioUpdateReceiver == null) radioUpdateReceiver = new RadioUpdateReceiver();	
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_CREATED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_DESTROYED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_STARTED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_START_PREPARING));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_PREPARED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_PLAYING));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_PAUSED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_STOPPED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_COMPLETED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_ERROR));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_BUFFERING_START));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_BUFFERING_END));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_METADATA_UPDATED));
		getActivity().registerReceiver(radioUpdateReceiver, new IntentFilter(RadioService.MODE_ALBUM_UPDATED));

		if(wasPlayingBeforePhoneCall) {
			radioService.play();
			wasPlayingBeforePhoneCall = false;
		}
	}
	
	public void initialize(View view)
	{
		Log.d(this.getClass().getName(), "initialize() called");
		try
		{
			
			displayAd = (boolean) Boolean.parseBoolean(getResources().getString(R.string.is_display_ad));
			
			STATUS_BUFFERING = getResources().getString(R.string.status_buffering);
			
			playButton = (Button) view.findViewById(R.id.PlayButton);
			playButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onClickPlayButton(v);
				}
			});
			
			pauseButton	= (Button) view.findViewById(R.id.PauseButton);
			pauseButton.setOnClickListener(new OnClickListener()
			{	
				@Override
				public void onClick(View v)
				{
					onClickPauseButton(v);
				}
			});
			
			stopButton	= (Button) view.findViewById(R.id.StopButton);
			Log.d("test test","setting stop button listener");
			stopButton.setOnClickListener(new OnClickListener()
			{	
				@Override
				public void onClick(View v)
				{
					onClickStopButton(v);
				}
			});
			
			nextButton 		= (Button) view.findViewById(R.id.NextButton);
			
			
			previousButton 	= (Button) view.findViewById(R.id.PreviousButton);
			
			pauseButton.setEnabled(false);
			pauseButton.setVisibility(View.INVISIBLE);
			
			stationImageView = (ImageView) view.findViewById(R.id.stationImageView);
			
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
			
			albumTextView	= (TextView) view.findViewById(R.id.albumTextView);
			artistTextView 	= (TextView) view.findViewById(R.id.artistTextView);
			trackTextView 	= (TextView) view.findViewById(R.id.trackTextView);
			statusTextView 	= (TextView) view.findViewById(R.id.statusTextView);
			timeTextView 	= (TextView) view.findViewById(R.id.timeTextView);
			
			
			getActivity().startService(new Intent(getActivity(), RadioService.class));
			
			displayAd();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public void displayAd()
	{
		if(displayAd==true) {
			// Create the adView
			try {
				
				if (adView != null) {
			      adView.destroy();
			    }
							
				adView = new AdView(getActivity(), AdSize.SMART_BANNER, this.getString(R.string.admob_publisher_id));
			    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.adLayout);
			    layout.addView(adView);
			    adView.loadAd(new AdRequest());
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.adLayout);
			//layout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
			layout.setVisibility(View.GONE);
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
	
	
	public void onClickPlayButton(View view) {
		radioService.play();
	}
	
	public void onClickPauseButton(View view) {
		radioService.pause();
	}
	
	public void onClickStopButton(View view) {
		Log.d("test test", "onClickStopButton() called");
		radioService.stop();
		resetMetadata();
		updateDefaultCoverImage();
	}
	
	public void onClickNextButton(View view) {
		resetMetadata();
		playNextStation();
		updateDefaultCoverImage();		
	}
	
	public void onClickPreviousButton(View view) {
		resetMetadata();
		playPreviousStation();
		updateDefaultCoverImage();				
	}
	
	public void playNextStation() {
		radioService.stop();
		radioService.setNextStation();		
		
		/*
		if(radioService.isPlaying()) {
			radioService.setStatus(STATUS_BUFFERING);
			updateStatus();
			radioService.stop();
			radioService.play();
		}
		else {
			radioService.stop();
		}
		*/
	}
	
	public void playPreviousStation() {
		radioService.stop();
		radioService.setPreviousStation();		
				
		/*
		if(radioService.isPlaying()) {
			radioService.setStatus(STATUS_BUFFERING);
			updateStatus();
			radioService.stop();
			radioService.play();
		}
		else {
			radioService.stop();
		}
		*/
	}
	
	public void updateDefaultCoverImage()
	{
		
		String mDrawableName = "station_" + (radioService.getCurrentStationID() + 1);
		
		int resID = getResources().getIdentifier
		(
			mDrawableName,
			"drawable",
			getActivity().getPackageName()
		);
				
		stationImageView.setImageResource(resID);
		albumTextView.setText("");
	}
	
	public void updateAlbum()
	{
		
		String album		= radioService.getAlbum();
		String artist		= radioService.getArtist();
		String track		= radioService.getTrack();
		Bitmap albumCover	= radioService.getAlbumCover();
		
		albumTextView.setText(album);
		
		if
		(
			albumCover==null || ( artist.equals("") && track.equals("") )
		)
			updateDefaultCoverImage();
		else
		{
			stationImageView.setImageBitmap(albumCover);
			radioService.setAlbum(LastFMCover.album);
			
			if
			(
				radioService.getAlbum().length()
				+ 
				radioService.getArtist().length()
				> 50
			)
			{
				albumTextView.setText("");
			}
		}
	}
	
	public void updateMetadata() {
		//String artist = radioService.getArtist();
		//String track = radioService.getTrack();
		//if(artist.length()>30)
			//artist = artist.substring(0, 30)+"...";
		artistTextView.setText("WAO 97.5 FM");
		trackTextView.setText("La Revoluci�n de la Radio");
		albumTextView.setText("");
	}
	
	public void resetMetadata() {
		radioService.resetMetadata();
		artistTextView.setText("WAO 97.5 FM");
		albumTextView.setText("");
		trackTextView.setText("La Revoluci�n de la Radio");
	}
	
	

		
	/* Receive Broadcast Messages from RadioService */
	private class RadioUpdateReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	Log.d("BROADCAST", "BROADCAST RECEIVED");
	    	if (intent.getAction().equals(RadioService.MODE_CREATED)) {
	    		
	        }
	    	else if (intent.getAction().equals(RadioService.MODE_DESTROYED)) {
	    		playButton.setEnabled(true);
	    		pauseButton.setEnabled(false);
	        	stopButton.setEnabled(false);
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);
	        	updateDefaultCoverImage();
	        	updateMetadata();
	        	updateStatus();
	        }
	    	else if (intent.getAction().equals(RadioService.MODE_STARTED)) {	    		
	    		pauseButton.setEnabled(false);	        	
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);	        	
	        	
    			playButton.setEnabled(true);
    			stopButton.setEnabled(false);
    			updateStatus();	    		
	        }
	    	else if (intent.getAction().equals(RadioService.MODE_START_PREPARING)) {	    		
	    		pauseButton.setEnabled(false);	        	
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);	        	
	        	playButton.setEnabled(false);			
	    		stopButton.setEnabled(true);
	    		updateStatus();
	    	}
	    	else if (intent.getAction().equals(RadioService.MODE_PREPARED)) {
	    		playButton.setEnabled(true);
	    		pauseButton.setEnabled(false);
	        	stopButton.setEnabled(false);
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);
	        	updateStatus();
	        }
	    	else if (intent.getAction().equals(RadioService.MODE_BUFFERING_START)) {
	        	updateStatus();
	        }
	    	else if (intent.getAction().equals(RadioService.MODE_BUFFERING_END)) {
	        	updateStatus();
	        }
	    	else if (intent.getAction().equals(RadioService.MODE_PLAYING)) {
	    		if(radioService.getCurrentStationType().equals(TYPE_AAC)) {
		    		playButton.setEnabled(false);
		        	stopButton.setEnabled(true);
	    		}
	    		else {
	    			playButton.setEnabled(false);
		    		pauseButton.setEnabled(true);
		        	stopButton.setEnabled(true);
		        	playButton.setVisibility(View.INVISIBLE);
		        	pauseButton.setVisibility(View.VISIBLE);
	    		}
	        	updateStatus();
	        }
	    	else if(intent.getAction().equals(RadioService.MODE_PAUSED)) {
	    		playButton.setEnabled(true);
	    		pauseButton.setEnabled(false);
	        	stopButton.setEnabled(true);
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);
	        	updateStatus();
	    	}
	    	else if(intent.getAction().equals(RadioService.MODE_STOPPED)) {
	    		playButton.setEnabled(true);
	    		pauseButton.setEnabled(false);
	        	stopButton.setEnabled(false);
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);
	        	updateStatus();
	    	}
	    	else if(intent.getAction().equals(RadioService.MODE_COMPLETED)) {
	    		playButton.setEnabled(true);
	    		pauseButton.setEnabled(false);
	        	stopButton.setEnabled(false);
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);
	        	updateStatus();
	    	}
	    	else if(intent.getAction().equals(RadioService.MODE_ERROR)) {
	    		playButton.setEnabled(true);
	    		pauseButton.setEnabled(false);
	        	stopButton.setEnabled(false);
	        	playButton.setVisibility(View.VISIBLE);
	        	pauseButton.setVisibility(View.INVISIBLE);
	        	updateStatus();
	    	}
	    	else if(intent.getAction().equals(RadioService.MODE_METADATA_UPDATED)) {
	    		updateMetadata();
	        	updateStatus();
	        	updateDefaultCoverImage();
	    	}
	    	else if(intent.getAction().equals(RadioService.MODE_ALBUM_UPDATED)) {
	    		updateAlbum();
	    	}
	    }
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
	
	
	public void updateStatus() {
		String status = radioService.getStatus();
		if(radioService.getTotalStationNumber() > 1) {
			if(status.length() > 0)
				status = radioService.getCurrentStationName()+" - "+status;
			else
				status = radioService.getCurrentStationName();
		}
			
		try {	
				statusTextView.setText(status);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// Handles the connection between the service and activity
	private ServiceConnection radioConnection = new ServiceConnection() {
		
		public void onServiceConnected
		(
			ComponentName className,
			IBinder service
		)
		{
			radioService = ((RadioService.RadioBinder)service).getService();
			
			if(radioService	.getTotalStationNumber() <=1 )
			{
				nextButton		.setEnabled(false);
				nextButton		.setVisibility(View.INVISIBLE);
				previousButton	.setEnabled(false);
				previousButton	.setVisibility(View.INVISIBLE);
			}
			updateStatus();
			updateMetadata();
			updateAlbum();
			updatePlayTimer();
			radioService.showNotification();
			
			registerReceiver();

		}
		
		public void onServiceDisconnected(ComponentName className)
		{
			radioService = null;
		}
	};

	private void initControls()
	{
		try
		{
			volumeSeekbar	= (SeekBar) getActivity().findViewById(R.id.seekBar1);
			audioManager	= (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
			
			volumeSeekbar.setMax
			(
				audioManager.getStreamMaxVolume
				(
					AudioManager.STREAM_MUSIC
				)
			);
			
			volumeSeekbar.setProgress
			(
				audioManager.getStreamVolume
				(
					AudioManager.STREAM_MUSIC
				)
			);

			volumeSeekbar.setOnSeekBarChangeListener
			(new OnSeekBarChangeListener()
			{
				@Override
				public void onStopTrackingTouch(SeekBar arg0)
				{
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0)
				{
				}

				@Override
				public void onProgressChanged
				(
					SeekBar arg0,
					int progress,
					boolean arg2
				)
				{
					audioManager.setStreamVolume
					(
						AudioManager.STREAM_MUSIC, progress,
						0
					);
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
