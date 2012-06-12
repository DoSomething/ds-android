package org.dosomething.android.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Campaign extends AbstractActivity {

	private static final String CAMPAIGN = "campaign";

	private static final int REQ_LOGIN_FOR_SIGN_UP = 111;

	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;

	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.image) private ImageView imgLogo;
	@InjectView(R.id.background) private ImageView imgBackground;
	@InjectView(R.id.image_container) private LinearLayout llImageContainer;
	@InjectView(R.id.dates) private TextView txtDates;
	@InjectView(R.id.teaser) private TextView txtTeaser;
	@InjectView(R.id.actions) private Button btnActions;
	@InjectView(R.id.howTo) private Button btnHowTo;
	@InjectView(R.id.gallery) private Button btnGallery;
	@InjectView(R.id.prizes) private Button btnPrizes;
	@InjectView(R.id.resources) private Button btnResources;
	@InjectView(R.id.faq) private Button btnFaq;
	@InjectView(R.id.sign_up) private Button btnSignUp;
	@InjectView(R.id.frmVideo) private FrameLayout frmVideo;
	@InjectView(R.id.imgVideoThumb) private ImageView imgVideoThumb;
	@InjectView(R.id.imgThumb) private ImageView imgThumb;

	private org.dosomething.android.transfer.Campaign campaign;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.campaign);

		actionBar.addAction(Campaigns.getHomeAction(this));

		campaign = (org.dosomething.android.transfer.Campaign) getIntent().getSerializableExtra(CAMPAIGN);

		actionBar.setTitle(campaign.getName());

		txtDates.setText(formatDateRange(campaign));
		txtDates.setTypeface(headerTypeface, Typeface.BOLD);
		
		txtTeaser.setText(campaign.getTeaser());

		llImageContainer.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
		if(!nullOrEmpty(campaign.getBackgroundUrl())) {
			imageLoader.displayImage(campaign.getBackgroundUrl(), imgBackground);
		}
		imageLoader.displayImage(campaign.getLogoUrl(), imgLogo);
		
		btnSignUp.setTypeface(headerTypeface, Typeface.BOLD);
		btnActions.setTypeface(headerTypeface, Typeface.BOLD);
		
		btnHowTo.setTypeface(headerTypeface, Typeface.BOLD);
		if(!nullOrEmpty(campaign.getHowTos())){
			btnHowTo.setVisibility(Button.VISIBLE);
		}

		btnGallery.setTypeface(headerTypeface, Typeface.BOLD);
		if(campaign.getGallery() != null){
			btnGallery.setVisibility(Button.VISIBLE);
		}
		
		btnPrizes.setTypeface(headerTypeface, Typeface.BOLD);
		if(campaign.getPrize() != null){
			btnPrizes.setVisibility(Button.VISIBLE);
		}

		btnResources.setTypeface(headerTypeface, Typeface.BOLD);
		if(!nullOrEmpty(campaign.getResources())){
			btnResources.setVisibility(Button.VISIBLE);
		}

		btnFaq.setTypeface(headerTypeface, Typeface.BOLD);
		if(!nullOrEmpty(campaign.getFaqs())){
			btnFaq.setVisibility(Button.VISIBLE);
		}

		if(!nullOrEmpty(campaign.getVideoThumbnail()) && !nullOrEmpty(campaign.getVideoUrl())){
			imageLoader.displayImage(campaign.getVideoThumbnail(), imgVideoThumb);
			frmVideo.setVisibility(ImageView.VISIBLE);
		}else if(!nullOrEmpty(campaign.getImage())){
			imageLoader.displayImage(campaign.getImage(), imgThumb);
			imgThumb.setVisibility(ImageView.VISIBLE);
		}

		// onResume is called next
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Analytics.logCampaignPageView(this, this.getPageName(), campaign);
	}
	
	public void playVideo(View v){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(campaign.getVideoUrl())));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(userContext.isLoggedIn()){
			UserCampaign userCampaign = new MyDAO(this).findUserCampaign(userContext.getUserUid(), campaign.getId());
			if(userCampaign != null){
				btnSignUp.setEnabled(false);
				btnSignUp.setText(R.string.campaign_sign_up_button_already_signed_up);
				
				btnActions.setVisibility(Button.VISIBLE);
			}
		}
	}

	private static final boolean nullOrEmpty(List<?> list){
		return list == null || list.isEmpty();
	}
	
	private static boolean nullOrEmpty(String str){
		return str == null || str.trim().length() == 0;
	}

	public void actions(View v){
		startActivity(CampaignActions.getIntent(this, campaign));
	}
	
	public void howTo(View v){
		startActivity(CampaignHowTo.getIntent(this, campaign));
	}

	public void gallery(View v){
		startActivity(CampaignGallery.getIntent(this, campaign));
	}

	public void prizes(View v){
		startActivity(CampaignPrizes.getIntent(this, campaign));
	}

	public void resources(View v){
		startActivity(CampaignResources.getIntent(this, campaign));
	}

	public void faq(View v){
		startActivity(CampaignFAQ.getIntent(this, campaign));
	}

	private String formatDateRange(org.dosomething.android.transfer.Campaign campaign){
		SimpleDateFormat mf = new SimpleDateFormat("MMMMM");

		Calendar scal = Calendar.getInstance();
		scal.setTime(campaign.getStartDate());

		Calendar ecal = Calendar.getInstance();
		ecal.setTime(campaign.getEndDate());

		int sday = scal.get(Calendar.DAY_OF_MONTH);
		int eday = ecal.get(Calendar.DAY_OF_MONTH);

		return mf.format(campaign.getStartDate()) + " " + sday + getOrdinalFor(sday)
				+ "-" + mf.format(campaign.getEndDate()) + " " + eday + getOrdinalFor(eday);
	}

	private static String getOrdinalFor(int value) {
		int mod = value % 10;
		
		switch (mod) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	public void signUp(View v){
		String uid = new UserContext(this).getUserUid();
		
		if(uid != null){
			startActivity(SignUp.getIntent(this, campaign));
		}else{
			startActivityForResult(new Intent(this, Login.class), REQ_LOGIN_FOR_SIGN_UP);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQ_LOGIN_FOR_SIGN_UP && resultCode == RESULT_OK){
			if(userContext.isLoggedIn()){
				startActivity(SignUp.getIntent(this, campaign));
			}
		}
	}

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, Campaign.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	@Override
	protected String getPageName() {
		return "campaign";
	}

}
