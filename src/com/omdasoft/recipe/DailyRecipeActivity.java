package com.omdasoft.recipe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.yihabits.recipe.R;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DailyRecipeActivity extends Activity {
	private String domain = "204.236.144.237";
//	private String mainUrl = "http://" + domain + ":8080/go/go2/?d=mow&l=zh&p=";
	private String categoryUrl = "http://" + domain + ":8080/go/recipe_category_zh.html";
	private String favoriteUrl = "http://" + domain + ":8080/go/go2/?d=fa&i=uid&p=/get";
	private String uid;
	private WebView mainWeb;
	private String LOGTAG = "DailyRecipeActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// full screen
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);
		
		//deal with uid
		String uid = getUid();
		if (!("".equals(uid))) {
			this.uid = uid;
		}else{
			this.uid = "n/a";
		}
		
		// ad initialization
		// Create the adView
		AdView adView = new AdView(this, AdSize.BANNER, "a14dedd436cb99a");
		// Lookup your LinearLayout assuming it��s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.ad_layout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());
		
		mainWeb = (WebView) findViewById(R.id.mainWebView);
		mainWeb.getSettings().setJavaScriptEnabled(true);
		mainWeb.setWebViewClient(new WebViewClient() {
			@Override
			public void onLoadResource(WebView view, String url) {
				if(url.indexOf(domain) > 0 || url.indexOf("google") > 0){
					view.getSettings().setJavaScriptEnabled(true);
				}else{
					view.getSettings().setJavaScriptEnabled(false);
				}
				super.onLoadResource(view, url);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.contains(domain) && url.contains("&p=") && (!url.contains("&i=") )){
					url = url.replace("&p=", "&i=" + DailyRecipeActivity.this.uid + "&p=");
					view.loadUrl(url);
					return true;
				}else{
					return false;
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, "");
			}
		}); 

		try {
				mainWeb.loadUrl(categoryUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCoder, KeyEvent event) {
		int action = event.getAction();
	    int keyCode = event.getKeyCode();
	        switch (keyCode) {
	        case KeyEvent.KEYCODE_VOLUME_UP:
	            if (action == KeyEvent.ACTION_DOWN) {
	               mainWeb.pageUp(false);
	               return true;
	            }
	            
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	            if (action == KeyEvent.ACTION_DOWN) {
	            	mainWeb.pageDown(false);
	            	return true;
	            }
	        case KeyEvent.KEYCODE_BACK:
	        	if(mainWeb.canGoBack()){
	        		mainWeb.goBack();
	        		return true;
	        	}else{
	        		this.finish();
	        	}
	        default:
	            return false;
	        }
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  // We do nothing here. We're only handling this to keep orientation
	  // or keyboard hiding from causing the WebView activity to restart.
	}

	@Override
	protected void onStart() {
		super.onStart();

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
				// judge if jump to release notes
				String sureKey = getString(R.string.pref_release_show);
				boolean releaseflag = prefs.getBoolean(sureKey, true);
				String url = "";
				if (releaseflag) {
					url = getString(R.string.pref_release_note);
					// don't show release note for this version ever
					savePrefRelease(false);

				} else {
					// go to last visit url
					sureKey = getString(R.string.pref_last_page);
					url = prefs.getString(sureKey, categoryUrl);
				}
				mainWeb.loadUrl(url);


	}

	@Override
	protected void onStop() {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		String sureKey = getString(R.string.pref_last_page);
		editor.putString(sureKey, mainWeb.getUrl());
		editor.commit();

		super.onStop();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_home:
			mainWeb.loadUrl(categoryUrl);
			return true;
		case R.id.menu_favorite:
			mainWeb.loadUrl(favoriteUrl.replace("uid", this.uid));
			return true;
		case R.id.menu_help:
			// popup the about window
			mainWeb.loadUrl("file:///android_asset/help.html");
			return true;
		case R.id.menu_quit:
			this.finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void savePrefRelease(boolean flag) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		String sureKey = getString(R.string.pref_release_show);
		editor.putBoolean(sureKey, flag);
		editor.commit();
	}
	
	public void toastMsg(final String msg){
		runOnUiThread(new Runnable() {

	        @Override
	        public void run() {
	            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	        }
	    });
	}

	public String getUid(){
		return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
	}

}