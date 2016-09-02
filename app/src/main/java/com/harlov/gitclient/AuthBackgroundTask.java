package com.harlov.gitclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class AuthBackgroundTask extends AsyncTask<String, Void, Void> {
    String APIUrl;
    Context context;
    Activity activity;
    String responseMessage = "";

    public AuthBackgroundTask(Context context){
        this.context = context;
        this.activity = (Activity) context;
    }

    @Override
    protected Void doInBackground(String... params) {
        String method = params[0];

        if (method.equals("login")){
            APIUrl = "https://api.github.com/authorizations";
            try {
                String username = params[1];
                String password = params[2];
                String authorization = username + ":" + password;
                byte[] encodedBytes = Base64.encode(authorization.getBytes(), Base64.DEFAULT);
                String authString = "Basic " + new String(encodedBytes);

                URL url = new URL(APIUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("Authorization", authString);

                JSONArray scopesArray = new JSONArray();
                scopesArray.put("public_repo");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("scopes", scopesArray);
                jsonObject.put("note", "Git client demo");

                OutputStream outputStream= httpURLConnection.getOutputStream();
                outputStream.write(jsonObject.toString().getBytes("UTF-8"));
                outputStream.close();

                int responseCode = 0;
                try {
                    responseCode = httpURLConnection.getResponseCode();
                } catch (IOException e) {
                     responseCode = httpURLConnection.getResponseCode();
                }
                String responseText = httpURLConnection.getResponseMessage();

                /*Log.d("pusha", String.valueOf(responseCode));
                Log.d("pusha", responseText);*/

                StringBuilder stringBuilder = null;
                InputStream inputStream;

                if(responseCode == 401){
                    inputStream = httpURLConnection.getErrorStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    stringBuilder = new StringBuilder();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line + "\n");
                    }
                    String jsonString = stringBuilder.toString();
                    JSONObject jObject = new JSONObject(jsonString);
                    responseMessage = jObject.getString("message");//"Bad credentials"
                } else if (responseCode == 201){
                    responseMessage = httpURLConnection.getResponseMessage();//"Created"
                    inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    stringBuilder = new StringBuilder();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line + "\n");
                    }
                    String jsonString = stringBuilder.toString();
                    JSONObject jObject = new JSONObject(jsonString);
                    String token = jObject.getString("token");
                    String gitId = jObject.getString("id");
                    //Log.d("pusha", "TOKEN : " + token);
                    PrefUtils.saveToPrefs(context, PrefUtils.PREFS_LOGIN_USERNAME_KEY, username);
                    PrefUtils.saveToPrefs(context, PrefUtils.PREFS_LOGIN_TOKEN_KEY, token);
                    PrefUtils.saveToPrefs(context, PrefUtils.PREFS_GIT_ID_KEY, gitId);
                    PrefUtils.saveToPrefs(context, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, password);
                } else if (responseCode == 403){
                    inputStream = httpURLConnection.getErrorStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    stringBuilder = new StringBuilder();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line + "\n");
                    }
                    String jsonString = stringBuilder.toString();
                    JSONObject jObject = new JSONObject(jsonString);
                    String message = jObject.getString("message");
                    if (message.equals("Maximum number of login attempts exceeded. Please try again later.")){
                        responseMessage = "MaxLogin";
                    }
                }

                httpURLConnection.disconnect();
                //Log.d("pusha", stringBuilder.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (method.equals("check_login")){
            String username = params[1];
            String token = params[2];
            String urlAPI = "https://api.github.com/user";

            //Log.d("pusha", "Inside check_login " + username + ":" + token);
            String authorization = username + ":" + token;
            byte[] encodedBytes = Base64.encode(authorization.getBytes(), Base64.DEFAULT);
            String authString = "Basic " + new String(encodedBytes);

            try {
                URL url = new URL(urlAPI);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Authorization", authString);

                int responseCode = 0;
                try {
                    responseCode = httpURLConnection.getResponseCode();
                } catch (IOException e) {
                    responseCode = httpURLConnection.getResponseCode();
                }
                String responseText = httpURLConnection.getResponseMessage();

                /*Log.d("pusha", String.valueOf(responseCode));
                Log.d("pusha", responseText);*/

                if (responseCode == 200){
                    responseMessage = httpURLConnection.getResponseMessage();//"OK"
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (method.equals("logout")) {
            String username = params[1];
            String token = params[2];
            String password = params[3];
            String gitId = params[4];

            String urlAPI = "https://api.github.com/authorizations/" + gitId;
            /*Log.d("pusha", "Inside logout " + username + ":" + token);
            Log.d("pusha", "Inside logout: gitId=" + gitId + ", password=" + password);*/

            String authorization = username + ":" + password;
            byte[] encodedBytes = Base64.encode(authorization.getBytes(), Base64.DEFAULT);
            String authString = "Basic " + new String(encodedBytes);

            try {
                URL url = new URL(urlAPI);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("DELETE");
                httpURLConnection.setRequestProperty("Authorization", authString);

                int responseCode = 0;
                try {
                    responseCode = httpURLConnection.getResponseCode();
                } catch (IOException e) {
                    responseCode = httpURLConnection.getResponseCode();
                }
                String responseText = httpURLConnection.getResponseMessage();

                /*Log.d("pusha", String.valueOf(responseCode));
                Log.d("pusha", responseText);*/

                if (responseCode == 204){
                    responseMessage = httpURLConnection.getResponseMessage();//"No Content"
                }

                InputStream inputStream;
                if (responseCode >= 400)
                    inputStream = httpURLConnection.getErrorStream();
                else inputStream = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line + "\n");
                }

                //Log.d("pusha", stringBuilder.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        final EditText passwordEditText = (EditText) activity.findViewById(R.id.passwordEditText);

        if (responseMessage.equals("Bad credentials")){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Login");
            builder.setMessage("Wrong username or password");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    passwordEditText.setText("");
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (responseMessage.equals("Created")){
            Intent launchHomeActivity = new Intent(context, HomeActivity.class);
            context.startActivity(launchHomeActivity);
            activity.finish();
        } else if (responseMessage.equals("OK")){
            //Log.d("splash", "Credentials are OK - Starting Home Activity");
            Intent launchHomeActivity = new Intent(context, HomeActivity.class);
            context.startActivity(launchHomeActivity);
            activity.finish();
        } else if (responseMessage.equals("MaxLogin")){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Login");
            builder.setMessage("Maximum number of login attempts exceeded. Please try again later.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    passwordEditText.setText("");
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (responseMessage.equals("No Content")){
            PrefUtils.deleteFromPrefs(context, PrefUtils.PREFS_LOGIN_USERNAME_KEY);
            PrefUtils.deleteFromPrefs(context, PrefUtils.PREFS_LOGIN_TOKEN_KEY);
            PrefUtils.deleteFromPrefs(context, PrefUtils.PREFS_GIT_ID_KEY);
            PrefUtils.deleteFromPrefs(context, PrefUtils.PREFS_LOGIN_PASSWORD_KEY);

            //Log.d("splash", "Prefs are deleted - Starting Login Activity");
            Intent launchLoginActivity = new Intent(context, LoginActivity.class);
            context.startActivity(launchLoginActivity);
            activity.finish();
        }
    }
}
