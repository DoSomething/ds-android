package org.dosomething.android.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.reminders.ReminderManager;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SignUp extends AbstractWebForm {

    private static final String CAMPAIGN = "campaign";

    private WebForm webForm;

    @Override
    public  String getPageName() {
        return "sign-up";
    }

    protected int getContentViewResourceId() {
        return R.layout.sign_up;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        webForm = campaign.getSignUp();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected WebForm getWebForm() {
        return webForm;
    }

    @Override
    protected void onSubmitSuccess() {
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);

        DSDao dao = new DSDao(this);

        UserCampaign uc = new UserCampaign.UserCampaignCVBuilder()
                .campaignId(campaign.getId())
                .uid(new UserContext(this).getUserUid())
                .campaignName(campaign.getName())
                .dateEnds(campaign.getEndDate().getTime() / 1000) // times needs to be in seconds
                .dateSignedUp(Calendar.getInstance().getTimeInMillis() / 1000)
                .dateStarts(campaign.getStartDate().getTime() / 1000)
                .build();
        dao.setSignedUp(uc);

        // Check if this campaign has a report back challenge
        boolean canReportBack = false;

        // If so, then set to trigger a notification to remind them later to report back
        if (canReportBack) {

            Date endDate = campaign.getEndDate();
            Calendar c = Calendar.getInstance();
            Date todayDate = c.getTime();

            // Set notification to trigger a week before end of campaign at 5pm
            c.setTime(endDate);
            c.add(Calendar.DAY_OF_MONTH, -7);
            c.set(Calendar.HOUR_OF_DAY, 18);
            c.set(Calendar.MINUTE, 0);
            c.add(Calendar.SECOND, 5);

            long alarmTime = c.getTimeInMillis();

            // Only trigger alarm if it'll be set after today
            if (c.after(todayDate)) {
                PendingIntent sender = ReminderManager.createReportBackReminder(this, campaign.getId(),
                        campaign.getName());
                ReminderManager.scheduleReminder(this, alarmTime, sender);
            }
        }

        // Flurry Analytics event tracking
        HashMap<String, String> param = new HashMap<String, String>();
        param.put(CAMPAIGN, campaign.getName());
        Analytics.logEvent("sign-up-submit", param);

        // Google Analytics event tracking
        Analytics.logEvent("sign-up", "normal-sign-up", campaign.getName());

        startActivity(CampaignShare.getIntentForSignedUp(this, campaign));
        finish();
    }

    public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
        Intent answer = new Intent(context, SignUp.class);
        answer.putExtra(CAMPAIGN, campaign);
        return answer;
    }

}
