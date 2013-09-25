package org.dosomething.android.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import org.dosomething.android.R;

import java.util.Arrays;

public class LoginFragment extends Fragment {
	
	private UiLifecycleHelper uiHelper;
	
	private LoginButton fbLoginButton;
	
	private Login loginActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
		
	    // In case any session is lingering with token info, clear it to start fresh
	    Session session = Session.getActiveSession();
    	if (session != null) {
    		session.closeAndClearTokenInformation();
    	}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// This fragment is responsible for displaying the Login layout content
		// instead of the owning Activity.
	    View view = inflater.inflate(R.layout.login, container, false);

        // Set custom typeface for the buttons
        Typeface typefaceDin = Typeface.createFromAsset(getActivity().getAssets(), "DINComp-CondBold.ttf");
        Button loginButton = (Button)view.findViewById(R.id.loginButton);
        loginButton.setTypeface(typefaceDin, Typeface.BOLD);
        Button signUpButton = (Button)view.findViewById(R.id.signUpButton);
        signUpButton.setTypeface(typefaceDin, Typeface.BOLD);
	    
	    // Cache reference to owning Activity
	    loginActivity = (Login) getActivity();
	    
	    // Set Fragment to handle login button action and set FB permissions we want
	    fbLoginButton = (LoginButton) view.findViewById(R.id.facebookLoginButton);
	    fbLoginButton.setFragment(this);
	    fbLoginButton.setReadPermissions(Arrays.asList("email", "user_birthday"));

	    return view;
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null && (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		// If session state has changed to a logged in state, execute Facebook
		// login to DoSomething backend.
	    if (state.isOpened()) {
			String fbAccessToken = session.getAccessToken();
			if (fbAccessToken != null && fbAccessToken.length() > 0) {
				loginActivity.dsFacebookLogin(fbAccessToken);
			}
	    }
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
}
