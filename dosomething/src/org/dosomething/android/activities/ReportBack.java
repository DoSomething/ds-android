package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReportBack extends AbstractWebForm {
	
	private static final String CAMPAIGN = "campaign";
	
	private WebForm webForm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
		webForm = campaign.getReportBack();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected WebForm getWebForm() {
		return webForm;
	}
	
	protected int getContentViewResourceId() {
		return R.layout.report_back;
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, ReportBack.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
}