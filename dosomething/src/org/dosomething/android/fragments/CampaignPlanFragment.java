package org.dosomething.android.fragments;

import android.app.Activity;
import android.app.PendingIntent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.reminders.ReminderManager;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.ICampaignSectionData;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * Campaign sub-page providing info to prepare for a campaign.
 */
public class CampaignPlanFragment extends AbstractCampaignFragment implements View.OnClickListener {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();
    private final int STEP_NUMBER = 1;

    @Inject @Named("ProximaNova-Bold") private Typeface typefaceBold;
    @Inject @Named("ProximaNova-Reg") private Typeface typefaceReg;

    // Interface to the SharedPreferences
    @Inject private DSPreferences dsPrefs;

    // Button to mark this step as being completed
    @InjectView(R.id.btn_did_this) private Button mButtonDidThis;

    // Button to setup a reminder
    @InjectView(R.id.btn_remind_me) private Button mButtonRemindMe;

    // TextView to show when user will be reminded
    @InjectView(R.id.reminder_text) private TextView mTextReminder;

    // Layout container for dynamic page content
    @InjectView(R.id.content) private LinearLayout mContentLayout;

    // Campaign data
    private Campaign mCampaign;

    @Override
    public String getFragmentName() {
        return "Plan";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_plan, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        mCampaign = (Campaign)args.getSerializable(CAMPAIGN);

        // Populate content container
        List<ICampaignSectionData> data = mCampaign.getPlanData();
        Iterator<ICampaignSectionData> iter = data.iterator();
        while (iter.hasNext()) {
            ICampaignSectionData sectionData = iter.next();
            sectionData.addToView(getActivity(), mContentLayout);
        }

        // Set style for UI elements
        mButtonDidThis.setTypeface(typefaceBold);
        mButtonRemindMe.setTypeface(typefaceBold);
        mTextReminder.setTypeface(typefaceReg);

        // Setup button click listeners
        mButtonDidThis.setOnClickListener(this);
        mButtonRemindMe.setOnClickListener(this);

        // Determine if the "Did This" button should be enabled
        Activity activity = getActivity();
        DSDao dsDao = new DSDao(activity);
        UserContext userContext = new UserContext(activity);
        boolean isStepComplete = dsDao.isCampaignStepComplete(userContext.getUserUid(), mCampaign.getId(), STEP_NUMBER);
        mButtonDidThis.setEnabled(!isStepComplete);

        // Update reminder text to inform when a reminder is set for
        String campaignStepName = getString(R.string.reminder_campaign_step_plan);
        if (dsPrefs.isStepReminderSet(mCampaign.getId(), campaignStepName)) {

            long alarmTime = dsPrefs.getStepReminder(mCampaign.getId(), campaignStepName);
            if (alarmTime > 0) {
                Date date = new Date(alarmTime);
                DateFormat df = DateFormat.getDateInstance();
                df.setTimeZone(Calendar.getInstance().getTimeZone());
                String strDate = df.format(date);
                String strReminder = getString(R.string.reminder_campaign_step_isset, strDate);

                mTextReminder.setText(strReminder);
                mTextReminder.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        String campaignId = mCampaign.getId();
        String campaignName = mCampaign.getName();
        String campaignStepName = getString(R.string.reminder_campaign_step_plan);

        PendingIntent reminderIntent = ReminderManager.createCampaignStepReminder(getActivity(),
                campaignId, campaignName, campaignStepName);

        switch (id) {
            case R.id.btn_did_this:
                // Mark this step as being completed
                Activity activity = getActivity();
                DSDao dsDao = new DSDao(activity);
                UserContext userContext = new UserContext(activity);
                dsDao.setCampaignStepCompleted(userContext.getUserUid(), campaignId, STEP_NUMBER);

                // Remove the reminder from the alarm service
                ReminderManager.clearReminder(getActivity(), reminderIntent);

                // Remove the reminder set for this step
                dsPrefs.clearStepReminder(campaignId, campaignStepName);

                // Disable button
                view.setEnabled(false);

                // Remove any reminder text
                mTextReminder.setText(null);
                mTextReminder.setVisibility(View.GONE);
                break;
            case R.id.btn_remind_me:
                // Create a reminder to send to the user in 3 days at 10am
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 3);
                cal.set(Calendar.HOUR_OF_DAY, 10);
                cal.set(Calendar.MINUTE, 0);
                long alarmTime = cal.getTimeInMillis();

                // Set a reminder to the system
                ReminderManager.scheduleReminder(getActivity(), alarmTime, reminderIntent);

                // Save the reminder to SharedPreferences
                dsPrefs.setStepReminder(campaignId, campaignName, campaignStepName, alarmTime);

                // Update the text to show when the user will be reminded
                Date date = new Date(alarmTime);
                DateFormat df = DateFormat.getDateInstance();
                df.setTimeZone(cal.getTimeZone());
                String strDate = df.format(date);
                String strReminder = getString(R.string.reminder_campaign_step_when, strDate);

                mTextReminder.setText(strReminder);
                mTextReminder.setVisibility(View.VISIBLE);
                break;
        }
    }
}
