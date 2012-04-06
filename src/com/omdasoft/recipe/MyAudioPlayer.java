package com.omdasoft.recipe;

import java.io.File;
import net.yihabits.recipe.R;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class MyAudioPlayer extends Thread {
	private Context c;
	private Thread blinker;
	private File file;

	public MyAudioPlayer (Context c, File file) {
	    this.c = c;
	    this.file = file;
	}

	public void go () {
	    blinker = this;
	    if(!blinker.isAlive()) {
	        blinker.start();
	    }
	}

	public void end () {
	    Thread waiter = blinker;
	    blinker = null;
	    if (waiter != null)
	        waiter.interrupt ();
	}

	public void run () {
	    MediaPlayer ap = MediaPlayer.create(c, Uri.fromFile(file));
	    int duration = ap.getDuration();
	    long startTime = System.currentTimeMillis();
	    ap.start();
	    try {
	        Thread thisThread = Thread.currentThread();
	        while (this.blinker == thisThread && System.currentTimeMillis() - startTime < duration) {           
	            Thread.sleep (500);  // interval between checks (in ms)
	        }
	        ap.stop ();
	        ap.release ();
	        ap = null;
	    } catch (InterruptedException e) {
	        Log.d("AUDIO-PLAYER", "INTERRUPTED EXCEPTION");
	        ap.stop ();
	        ap.release();
	        ap = null;
	    }
	    }
	}
