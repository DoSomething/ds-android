package org.dosomething.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Session;
import com.google.inject.Inject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.fragments.LoginFragment;
import org.dosomething.android.tasks.AbstractWebserviceTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Login extends AbstractFragmentActivity {

    private static final int REQ_SIGN_UP = 112;

    private LoginFragment loginFragment;

    @Inject private UserContext userContext;

    private DSLoginTask dsLoginTask;

    @Override
    protected String getPageName() {
        return "Login";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, loginFragment).commit();
        }
        else {
            // Or set the fragment from restored state info
            loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }
    }

    /**
     * Execute task for normal DoSomething login with username/email and password
     */
    public void logIn() {
        EditText username = (EditText)findViewById(R.id.username);
        EditText password = (EditText)findViewById(R.id.password);

        String user = username.getText().toString();
        String pw = password.getText().toString();

        dsLoginTask = new DSLoginTask(user, pw);
        dsLoginTask.execute();
    }

    /**
     * Get handle to DSLoginTask. Useful for unit tests.
     */
    public AbstractWebserviceTask getDSLoginTask() {
        return dsLoginTask;
    }

    private void goToCampaigns() {
        startActivity(Campaigns.getIntent(this, DSConstants.CAMPAIGNS_TAB.DOING_IT));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SIGN_UP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /**
     * Provide Intent for other activities to open this activity
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, Login.class);
    }

    public static void logout(Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(activity.getString(R.string.logout_confirm))
                .setPositiveButton(activity.getString(R.string.yes_upper), new OnLogoutClickListener(activity))
                .setNegativeButton(activity.getString(R.string.no_upper), null)
                .create()
                .show();
    }

    public static class OnLogoutClickListener implements OnClickListener {
        private Activity activity;

        public OnLogoutClickListener(Activity a) {
            activity = a;
        }

        public void onClick(DialogInterface dialog, int which) {
            // Close Facebook session and clear token info if any
            Session session = Session.getActiveSession();
            if (session != null) {
                session.closeAndClearTokenInformation();
            }

            new UserContext(activity).clear();
            activity.startActivity(new Intent(activity, Welcome.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            activity.finish();
        }
    }

    private class DSLoginTask extends AbstractWebserviceTask {

        private String username;
        private String password;

        private boolean loginSuccess;

        private ProgressDialog pd;

        public DSLoginTask(String username, String password) {
            super(userContext);
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(Login.this, null, getString(R.string.logging_in));

            // Clear the UserContext of any data that could be lingering (ex: session cookie info)
            userContext.clear();
        }

        @Override
        protected void onSuccess() {

            if (loginSuccess) {
                // Track login with analytics - Flurry Analytics
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("ds-login", "login-success");
                Analytics.logEvent(getPageName(), param);

                // and Google Analytics
                Analytics.logEvent("login", "ds-login", "success");

                goToCampaigns();
            }
            else {
                Toast.makeText(Login.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onFinish() {
            try {
                pd.dismiss();
            }
            catch (IllegalArgumentException e) {
                // Catching error if progress dialog is dismissed after activity ends
            }
        }

        @Override
        protected void onError(Exception e) {

            new AlertDialog.Builder(Login.this)
                    .setMessage(getString(R.string.log_in_failed))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_upper), null)
                    .create()
                    .show();
        }

        @Override
        protected void doWebOperation() throws Exception {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));

            WebserviceResponse response = doPost(DSConstants.API_URL_LOGIN, params);

            if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
                loginSuccess = false;
            }
            else {
                userContext.updateWithUserObject(response.getBodyAsJSONObject());

                loginSuccess = true;
            }
        }
    }
}