/* 
 * Developed By: Mohammad Zakaria Chowdhury
 * Company: Webcraft Bangladesh
 * Email: zakaria.cse@gmail.com
 * Website: http://www.webcraftbd.com
 */

package com.webcraftbd.radio;

import com.wao975.aacplayer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {
	
	private Intent bindIntent;
	private RadioService radioService;
	
	private static boolean isExitMenuClicked;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isExitMenuClicked = false;
		// Bind to the service	
		try {
			bindIntent = new Intent(getApplicationContext(), RadioService.class);
			boolean suc = getApplicationContext().bindService(bindIntent, radioConnection, Context.BIND_AUTO_CREATE);
			Log.d("fuck", "success bound: " + suc);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		if(isExitMenuClicked==true)
			finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
		final String thisClassName = this.getClass().getName();
		final String thisPackageName = this.getPackageName();
		
		switch (item.getItemId()) {
		case R.id.exit:
			//String title = "Salir de la aplicacion";
			String message = "Desea salir de la aplicacion?";
			String buttonYesString = "Si";
			String buttonNoString = "No";
			
			isExitMenuClicked = true;
			
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			//ad.setTitle(title);
			ad.setMessage(message);
			ad.setCancelable(true);
			ad.setPositiveButton(buttonYesString, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {					
					
					if(radioService!=null) {
						radioService.exitNotification();
						radioService.stop();						
						radioService.stopService(bindIntent);						
						isExitMenuClicked = true;
						finish();
					}
				}
			});
			
			ad.setNegativeButton(buttonNoString, null);
			
			ad.show();
			
			return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	// Handles the connection between the service and activity
	private ServiceConnection radioConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			radioService = ((RadioService.RadioBinder)service).getService();			
		}
		public void onServiceDisconnected(ComponentName className) {
			radioService = null;
		}
	};
	
	
}
