package com.haseman.music;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

public class MusicService extends Service implements OnCompletionListener{

	Cursor mCursor;
	MediaPlayer mPlayer = null;
	
	private void stop(){
		if(mPlayer != null)
			mPlayer.stop();
		setForegroundState(false);
		stopSelf();
	}
	private void play(){
		if(mPlayer != null)
			mPlayer.start();
		setForegroundState(true);
	}
	
	private boolean isPlaying(){
		if(mPlayer == null)
			return false;
		return mPlayer.isPlaying();
	}
	
	public void setDataSource(long id){	
		if(mCursor != null){
			mCursor.close();
		}
		mCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media._ID + " = "+ id, null, null);
		if(mCursor == null)
			return;
		
		if(!mCursor.moveToFirst()){
			mCursor.close();
			mCursor = null;
			return;
		}
		int pathIDX = mCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
		String path = mCursor.getString(pathIDX);
		try{
			mPlayer.reset();
			mPlayer.setDataSource(path);
			mPlayer.prepare();
		}catch(IOException io){
			Log.e("MediaService", "Unable to set data source",io);
		}
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);
	}
	
	public String getSongTitle(){
		if(mCursor == null)
			return "Nothing Playing!";
		int titleIDX = mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
		return mCursor.getString(titleIDX);
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mCursor != null)
			mCursor.close();
		if(mPlayer != null){
			mPlayer.stop();
			mPlayer.release();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		return Service.START_NOT_STICKY;
	}
	
	private void setForegroundState(boolean enable){
		
		if(enable){
			Notification n = new Notification(R.drawable.icon, "playing "+getSongTitle(), System.currentTimeMillis());
			
			n.contentView = new RemoteViews("com.haseman.serviceExample", R.layout.notification);
			//n.contentView.setString(R.id.notification_text, "setText", "playing "+getSongTitle());
			Intent clickIntent = new Intent(getApplicationContext(), MusicExampleActivity.class);
			n.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, clickIntent , 0);
			
			startForeground(1, n);
		}
		else{
			stopForeground(true);
		}
			
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	 static class MusicServiceStub extends IMusicService.Stub {
	        WeakReference<MusicService> mService;
	        
	        MusicServiceStub(MusicService service) {
	            mService = new WeakReference<MusicService>(service);
	        }
	        public void stop(){
	        	mService.get().stop();
	        }
	        public void play(){
	        	mService.get().play();
	        }
	        public void setDataSource(long id){
	        	mService.get().setDataSource(id);
	        }
	        public String getSongTitle(){
	        	return mService.get().getSongTitle();
	        }
	        public boolean isPlaying(){
	        	return mService.get().isPlaying();
	        }
	 }
	 private final IBinder mBinder = new MusicServiceStub(this);

	 
	@Override
	public void onCompletion(MediaPlayer mp) {
		stopSelf();
	}
}
