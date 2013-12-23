package org.dosomething.android.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.ICampaignSectionData;

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

    // Button to mark this step as being completed
    @InjectView(R.id.btn_did_this) private Button mButtonDidThis;

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

        // Set style and behavior for the Did This button
        mButtonDidThis.setTypeface(typefaceBold);
        mButtonDidThis.setOnClickListener(this);

        Activity activity = getActivity();
        DSDao dsDao = new DSDao(activity);
        UserContext userContext = new UserContext(activity);
        boolean isStepComplete = dsDao.isCampaignStepComplete(userContext.getUserUid(), mCampaign.getId(), STEP_NUMBER);
        mButtonDidThis.setEnabled(!isStepComplete);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_did_this:
                // Mark this step as being completed
                Activity activity = getActivity();
                DSDao dsDao = new DSDao(activity);
                UserContext userContext = new UserContext(activity);
                dsDao.setCampaignStepCompleted(userContext.getUserUid(), mCampaign.getId(), STEP_NUMBER);

                // Disable button
                view.setEnabled(false);
                break;
        }
    }
}
