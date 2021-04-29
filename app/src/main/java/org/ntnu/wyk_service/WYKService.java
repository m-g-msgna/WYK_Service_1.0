package org.ntnu.wyk_service;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class WYKService extends Service {

    private String TAG = "WYKService";
    //private Utils utils = new Utils();
    private Contacts contacts;
    private String hash = null;
    private boolean appInitialized = false;
    private boolean authentication_result = false;

    private int uid;
    private boolean keyGenerated;
    private ServiceKeys serviceKeys;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.w(TAG, "WYK Service Created.");
        contacts = new Contacts(this);
        serviceKeys = new ServiceKeys(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        uid = sharedPreferences.getInt( getString(R.string.uid), 0 );
        keyGenerated = sharedPreferences.getBoolean("KeyGenerated", false);

        //Local method variables
        final ContactsObserver contactsObserver = new ContactsObserver(this);

        //Run initialization on a separate worker thread not to block the GUI
        Thread initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Check if app already has keys
                //TODO: Generate key pairs here.

                //Check if user already has his uid saved locally
                if ( uid != 0) {
                    appInitialized = true;
                }

                //Check if the app is already initialized
                if ( !appInitialized ) {
                    InitializeService();
                }
            }
        });
        initThread.start();

        //Register ContactsList Observer
        getApplicationContext().getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                contactsObserver
        );
    }

    /**
     * On service start.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Add parsing actions <Start, Stop, Authenticate >
        String nameExtra = intent.getStringExtra("nameExtra");

        final String action = intent.getAction();

        //Separate Thread for Authenticate, Register service
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (action != null) {
                    switch (action) {
                        case Constants.ACTION.REGISTER_APPLICATION:
                            //RegisterApp();
                            break;
                        case Constants.ACTION.AUTHENTICATE_USER:
                            //Run authentication routine
                            authenticateUser();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        thread.start();

        startForeground(1, createNotification(nameExtra));

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        Log.w(TAG, "WYK Service Destroyed");
        //Toast.makeText(this, "WYK Service Destroyed!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Function creates a notification for the WYK Service.
     * @param notificationMessage
     * @return Notification
     */
    private Notification createNotification(String notificationMessage){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        return  new NotificationCompat.Builder(this, App.CHANNEL_ID)
                //.setContentTitle("WYK Service")
                .setContentText(notificationMessage)
                .setColor(getResources().getColor(R.color.darkBlue)) //Color.argb(255, 2, 70, 147))
                .setSmallIcon(R.drawable.ic_logo_icon)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void InitializeService(){
        //Local variables
        final String __contacts_hash;

        //TODO: Send the service public key to the remote server here.
        //Implement initialization process in full here.

        //Compute hash
        __contacts_hash = contacts.computeHash();

        //Upload to remote service
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonLoginObj = new JSONObject();

        try {
            jsonLoginObj.put( "hash", __contacts_hash );
            jsonBody.put( "login_info", jsonLoginObj );
            //jsonBody = new JSONObject(initialize_json_string);
        } catch (JSONException e) {
            Log.w( TAG, e.getMessage() );
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                Constants.WYK_SERVER.IP + Constants.WYK_SERVER.ENDPOINTS[0],
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //On Success
                        Log.w( TAG, response.toString() );

                        //Initialize SharedPreference with the received UID of the user.
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        try {
                            editor.putInt(getString(R.string.uid), response.getInt("user_id"));
                            editor.putString(getString(R.string.hash), __contacts_hash);
                            editor.apply();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Failed to initialize server.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                        Toast.makeText(getApplicationContext(), "Remote server successfully initialized", Toast.LENGTH_SHORT).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //On Error
                        Log.w( TAG, error.toString() );
                        Toast.makeText(getApplicationContext(), "Failed to initialize server.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);

    }

    /**
     * Authenticate the user based on the computed hash value.
     * @return
     */
    private void authenticateUser() {
        int UID;
        String __hash;

        //final boolean result = false;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        UID = sharedPreferences.getInt("UID", 0);
        __hash = sharedPreferences.getString("HASH", null);

        if( UID != 0 && __hash != null ){
            //Prepare the authentication JSON object
            JSONObject jsonAuthBody = new JSONObject();
            JSONObject jsonLoginObj = new JSONObject();
            try {
                jsonLoginObj.put( "hash", __hash );
                jsonAuthBody.put("user_id", UID);
                jsonAuthBody.put( "login_info", jsonLoginObj );
            } catch (JSONException e) {
                Log.w( TAG, e.getMessage() );
            }

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    Constants.WYK_SERVER.IP + Constants.WYK_SERVER.ENDPOINTS[2],
                    jsonAuthBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //On Success
                            Log.w(TAG, response.toString());

                            try {
                                /*if (response.getInt("server_code") == 1) {
                                    authentication_result = true;
                                    //result = true;
                                }*/
                                Intent authenticationBroadcast = new Intent();
                                authenticationBroadcast.setAction(Constants.ACTION.AUTHENTICATE_USER);
                                authenticationBroadcast.putExtra("AUTHENTICATED", response.getInt("server_code") );
                                authenticationBroadcast.putExtra("UID", response.getInt("user_id"));
                                authenticationBroadcast.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                authenticationBroadcast.setComponent( new ComponentName("org.ntnu.wykdemoapp",
                                        "org.ntnu.wykdemoapp.WYKAuthenticationReceiver") );
                                sendBroadcast(authenticationBroadcast);

                            } catch (JSONException e) {
                                //Exception on parsing json object response.
                                Log.w( TAG, e.getMessage() );
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //On Error
                            Log.w( TAG, error.toString() );
                        }
                    }
            );
            requestQueue.add(jsonObjectRequest);
        }

        //return authentication_result;
    }
}
