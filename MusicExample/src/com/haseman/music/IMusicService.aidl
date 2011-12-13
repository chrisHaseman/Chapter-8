package com.haseman.music;

interface IMusicService
{
	void stop();
    void play();
	void setDataSource(in long id);
	String getSongTitle();
	boolean isPlaying();
}