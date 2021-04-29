package org.ntnu.wyk_service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class ContactsObserver extends ContentObserver {

    private String TAG = "ContactsObserver";
    private Context context;
    private Contacts contacts;

    public ContactsObserver(Context c){
        super(null);
        this.context = c;
    }

    @Override
    public void onChange(boolean selfChange){
        //Routine on change of observed URis
        updateHash();
    }

    //Update should be here.
    private void updateHash() {
        final String __new_hash;
        contacts = new Contacts(context);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Get uid from shared preferences
        int uid = sharedPreferences.getInt(context.getString(R.string.uid), 0);

        //Compute new hash value when ever the contatcs list is updated.
        __new_hash = contacts.computeHash();

        //Upload to remote service
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonLoginObj = new JSONObject();

        try {
            jsonLoginObj.put("hash", __new_hash);
            jsonBody.put("user_id", uid);
            jsonBody.put("login_info", jsonLoginObj);
        } catch (JSONException e) {
            Log.w( TAG, e.getMessage() );
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                Constants.WYK_SERVER.IP + Constants.WYK_SERVER.ENDPOINTS[1],
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //On Success
                        Log.w( TAG, response.toString() );
                        //Update the local hash too
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("HASH", __new_hash);
                        editor.apply();
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
}
