package org.dosomething.android.activities;

import org.dosomething.android.analytics.Analytics;

import roboguice.activity.RoboActivity;

public abstract class AbstractActivity extends RoboActivity {
	
	protected abstract String getPageName();
	
	public void onStart() {
	   super.onStart();
	   Analytics.startSession(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        Analytics.logPageView(this, this.getPageName());
	}
	
	public void onStop() {
	   super.onStop();
	   Analytics.endSession(this);
	}
	
}
