package org.dosomething.android.activities;

import org.dosomething.android.R;

import android.os.Bundle;
import roboguice.activity.RoboActivity;

public class CampaignHowTo extends RoboActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_how_to);
    }
	
}