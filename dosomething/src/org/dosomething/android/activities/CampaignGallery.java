package org.dosomething.android.activities;

import org.dosomething.android.R;

import android.os.Bundle;
import roboguice.activity.RoboActivity;

public class CampaignGallery extends RoboActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_gallery);
    }
	
}
