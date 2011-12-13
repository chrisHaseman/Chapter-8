package com.haseman.music;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MusicExampleActivity extends Activity implements OnClickListener, ServiceConnection, OnLoadCompleteListener<Cursor>{
    
    MediaPlayer mBeeper;
    IMusicService mService;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button beep = (Button)findViewById(R.id.beep_button);
        beep.setOnClickListener(this);
        mBeeper = MediaPlayer.create(getApplicationContext(), R.raw.beeeep);
        Button recent = (Button)findViewById(R.id.most_recent_song);
        recent.setOnClickListener(this);
        
        Intent serviceIntent = new Intent(getApplicationContext(), MusicService.class);
        startService(serviceIntent);
        bindService(serviceIntent, this, Service.START_STICKY);
    }

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mBeeper != null){
			mBeeper.stop();
			mBeeper.release();
			mBeeper = null;
		}
		if(mService!=null){
			unbindService(this);
		}
	}
	
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.beep_button)
			mBeeper.start();
		else if(v.getId() == R.id.most_recent_song){
			//check if we have a service and it's playing
			try{
				if(mService != null && mService.isPlaying()){
					mService.stop();
					Button recent = (Button)findViewById(R.id.most_recent_song);
					recent.setText("Play");
					return;
				}
			}catch(RemoteException re){
				
			}
			CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DATE_ADDED + " Desc Limit 1");
			cursorLoader.registerListener(0, this);
			cursorLoader.startLoading();
		}	
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService  = IMusicService.Stub.asInterface(service);	
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
	}

	@Override
	public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
		if(cursor == null || !cursor.moveToFirst()){
			Toast.makeText(getApplicationContext(), "No Music to Play", Toast.LENGTH_LONG).show();
			return;
		}
		int idIDX = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
		long id = cursor.getLong(idIDX);
		if(mService == null){
			Toast.makeText(getApplicationContext(), "No Service to play Music!", Toast.LENGTH_LONG).show();
			return;
		}
		try{
			mService.setDataSource(id);
			mService.play();
			Button recent = (Button)findViewById(R.id.most_recent_song);
			recent.setText("Stop "+mService.getSongTitle());
		}catch(Exception E){
			Log.e("MusicPlayerActivity", "Failed to set data source",E);
		}
		cursor.close();
	}
}