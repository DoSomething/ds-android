package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractFetchCampaignsTask;
import org.dosomething.android.transfer.Campaign;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Campaigns extends AbstractActivity {
	
	//private static final String TAG = "Campaigns";
	private static final int REQ_LOGIN_FOR_PROFILE = 112;
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	@Inject private Cache cache;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
	protected String getPageName() {
		return "campaigns";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaigns);
        
        actionBar.setHomeAction(new EmptyAction());
        
        actionBar.addAction(profileButtonAction);
        
        fetchCampaigns();
    }
	
	private final Action profileButtonAction = new Action(){

		@Override
		public int getDrawable() {
			return R.drawable.action_bar_profile;
		}

		@Override
		public void performAction(View view) {
			Context context = getApplicationContext();
			if(new UserContext(context).isLoggedIn()){
				startActivity(Profile.getIntent(context));
			}else{
				startActivityForResult(new Intent(context, Login.class), REQ_LOGIN_FOR_PROFILE);
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQ_LOGIN_FOR_PROFILE && resultCode == RESULT_OK){
			if(new UserContext(this).isLoggedIn()){
				startActivity(Profile.getIntent(getApplicationContext()));
			}
		}
	}
	
	private void fetchCampaigns(){
		new MyTask().execute();
	}
	
	public static Action getHomeAction(Context context){
		return new IntentAction(context, new Intent(context, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), R.drawable.action_bar_home_logo);
	}

	private final OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int position,
				long id) {
			Campaign campaign = (Campaign) list.getAdapter().getItem(position);
			
			startActivity(org.dosomething.android.activities.Campaign.getIntent(getApplicationContext(), campaign));
		}
	};
	
	private class EmptyAction extends AbstractAction {

		public EmptyAction() {
			super(R.drawable.action_bar_home_logo);
		}

		@Override
		public void performAction(View view) { /* ignore*/ }
	}
	
	private class MyTask extends AbstractFetchCampaignsTask {

		public MyTask() {
			super(userContext, cache, actionBar);
		}

		@Override
		protected void onSuccess() {
			list.setOnItemClickListener(itemClickListener);
			list.setAdapter(new MyAdapter(getApplicationContext(), getCampaigns()));
		}

		@Override
		protected void onError() {
			Toast.makeText(Campaigns.this, "Unable to list campaigns", Toast.LENGTH_LONG).show();
		}

	}

	private class MyAdapter extends ArrayAdapter<Campaign> {

		public MyAdapter(Context context, List<Campaign> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.campaign_row, null);
			}
			
			Campaign campaign = getItem(position);
			
			v.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
			
			ImageView imageView = (ImageView) v.findViewById(R.id.image);
			
			imageLoader.displayImage(campaign.getLogoUrl(), imageView);

			return v;
		}
		
	}
	
}
