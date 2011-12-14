package com.haseman.media;

import android.app.Activity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.provider.MediaStore.Video;
import android.widget.Toast;
import android.widget.VideoView;

public class MediaExampleActivity extends Activity implements OnCompletionListener{
    /** Called when the activity is first created. */
	
	Cursor mediaCursor = null;
	VideoView videoView = null;
	int dataIdx = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        videoView = (VideoView)findViewById(R.id.my_video_view);
        videoView.setOnCompletionListener(this);
        String projection[] = new String[] {Video.Media.DATA};
        mediaCursor = getContentResolver().query(Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        if(mediaCursor != null && mediaCursor.getCount()>0){
        	dataIdx = mediaCursor.getColumnIndex(Video.Media.DATA);
        	playNextVideo();
        }
        
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
    	if(mediaCursor!=null){
    		mediaCursor.close();
    	}
    }
    
   @Override
   public void onPause(){
       super.onPause();
	   if(videoView != null)
		   videoView.pause();
   }
    
    private void playNextVideo(){
    	mediaCursor.moveToNext();
    	if(mediaCursor.isAfterLast()){
    		Toast.makeText(getApplicationContext(), "End of Line.", Toast.LENGTH_SHORT).show();
    	}
    	else{
    		String path = mediaCursor.getString(dataIdx);
    		Toast.makeText(getApplicationContext(), "Playing: "+path, Toast.LENGTH_SHORT).show();
    		videoView.setVideoPath(path);
        	videoView.start();
    	}
    }
    
    
    @Override
	public void onCompletion(MediaPlayer mp) {
		playNextVideo();
	}
	
  
}