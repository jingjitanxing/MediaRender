package com.geniusgithub.mediarender;


import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class BaseActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		WindowManager.LayoutParams attrs = getWindow().getAttributes();

		attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		getWindow().setAttributes(attrs);

		RenderApplication.onCatchError(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		RenderApplication.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		RenderApplication.onResume(this);
	}

}
