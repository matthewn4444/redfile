package com.jobmineplus.mobile.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jobmineplus.mobile.R;
import com.jobmineplus.mobile.widgets.JbmnplsHttpClient;
import com.jobmineplus.mobile.widgets.JbmnplsHttpClient.LOGGED;

public class HomeActivity extends LoggedInActivityBase implements OnClickListener{
    protected int[] buttonLayouts = {
            R.id.apps_button,
            R.id.shortlist_button,
            R.id.interviews_button,
            R.id.settings_button
    };
    private boolean prevEnabledInterviewCheck = false;

    private static final String PREFIX_PATH = "com.jobmineplus.mobile";
    private static final int RESULT_FROM_SETTINGS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        connectUI();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        Intent passedIntent = getIntent();
        if (passedIntent != null && passedIntent.hasExtra("username")) {
            String username = passedIntent.getStringExtra("username");
            String password = passedIntent.getStringExtra("password");
            if (isReallyOnline()) {
                new LoginTask().execute(username, password);
            } else {
                setOnlineMode(false);
                client.setLoginCredentials(username, password);
            }
        }
    }

    protected void connectUI() {
        Button button;
        for (int i = 0; i < buttonLayouts.length; i++) {
            button = (Button) findViewById(buttonLayouts[i]);
            button.setOnClickListener(this);
        }
    }

    public boolean goToActivityForResult(String activityName) {
        Class<?> name = null;
        try {
            name = Class.forName(PREFIX_PATH + activityName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        Intent in = new Intent(this, name);
        startActivityForResult(in, RESULT_FROM_SETTINGS);
        return true;
    }

    public boolean goToActivity(String activityName) {
        Class<?> name = null;
        try {
            name = Class.forName(PREFIX_PATH + activityName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        Intent in = new Intent(this, name);
        startActivity(in);
        return true;
    }

    public void onClick(View arg0) {
        Button button = (Button) arg0;
        String name = button.getText().toString();
        if (name.equals("Settings")) {
            prevEnabledInterviewCheck = preferences.getBoolean("settingsEnableInterCheck", false);
            goToActivityForResult(".activities.jbmnpls." + name);
        } else {
            goToActivity(".activities.jbmnpls." + name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_FROM_SETTINGS:
                // Check the differences coming back from settings
                if (preferences.getBoolean("settingsEnableInterCheck", false) != prevEnabledInterviewCheck) {
                    if (!prevEnabledInterviewCheck) {   // Enable it
                        startInterviewsAlarm();
                    } else {
                        cancelInterviewsAlarm();
                    }
                }
                break;
        }
    }

    protected final class LoginTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Do not allow this login to be aborted
            client.canAbort(false);
            JbmnplsHttpClient.LOGGED result = client.login(params[0], params[1]);
            client.canAbort(true);
            if (result != LOGGED.IN) {
                throw new IllegalStateException("Prior logins credentials do not work or isOnline() does not work");
            }
            return null;
        }

    }
}