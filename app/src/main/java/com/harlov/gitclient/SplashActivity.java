package com.harlov.gitclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String loggedInUsername = PrefUtils.getPromPrefs(SplashActivity.this,
                PrefUtils.PREFS_LOGIN_USERNAME_KEY, "defaultUsername");

        if (loggedInUsername.equals("defaultUsername")){
            Intent launchLoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            Log.d("splash", "Starting Login Activity");
            startActivity(launchLoginActivity);
            finish();
        } else {
            String loggedInToken = PrefUtils.getPromPrefs(SplashActivity.this,
                    PrefUtils.PREFS_LOGIN_TOKEN_KEY, "defaultToken");

            if (!loggedInToken.equals("defaultToken")){
                AuthBackgroundTask authBackgroundTask =
                        new AuthBackgroundTask(SplashActivity.this);
                authBackgroundTask.execute("check_login", loggedInUsername, loggedInToken);
            }
        }
    }
}
