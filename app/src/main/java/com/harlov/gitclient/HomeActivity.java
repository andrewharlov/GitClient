package com.harlov.gitclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reps_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.home_activity_title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reps_list_logout_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.logout){

            String loggedInUsername = PrefUtils.getPromPrefs(HomeActivity.this,
                    PrefUtils.PREFS_LOGIN_USERNAME_KEY, "defaultUsername");

            String loggedInToken = PrefUtils.getPromPrefs(HomeActivity.this,
                    PrefUtils.PREFS_LOGIN_TOKEN_KEY, "defaultToken");

            String loggedInPassword = PrefUtils.getPromPrefs(HomeActivity.this,
                    PrefUtils.PREFS_LOGIN_PASSWORD_KEY, "defaultPassword");

            String loggedInGitId = PrefUtils.getPromPrefs(HomeActivity.this,
                    PrefUtils.PREFS_GIT_ID_KEY, "defaultGitId");

            if (!loggedInUsername.equals("defaultUsername") &&
                    !loggedInToken.equals("defaultToken") && !loggedInPassword.equals("defaultPassword")
                    && !loggedInGitId.equals("defaultGitId")){
                AuthBackgroundTask authBackgroundTask = new AuthBackgroundTask(HomeActivity.this);
                authBackgroundTask.execute("logout", loggedInUsername, loggedInToken,
                        loggedInPassword, loggedInGitId);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
