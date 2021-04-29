package org.ntnu.wyk_service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class Contacts {
    private String TAG = "Contacts";
    private Context context;
    private Utils utils = new Utils();

    public Contacts(Context c){
        super();

        this.context = c;
    }

    /**
     * Computes the hash value of the entire contacts list database [Only computed once at the start of the service]
     * @return
     */
    public String computeHash(){
        //Re compute the hash value of the contacts list
        Log.w(TAG, "computeContactsHash");

        String __contact_id, __contact_name, __contact_phone_number, __contact_last_updated, __last_time_contacted, __times_contacted;
        StringBuffer output =  new StringBuffer();

        //ContactsContract.CommonDataKinds.Phone.
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);


        if( cursor.getCount() > 0 ) {
            while ( cursor.moveToNext() ) {
                __contact_id = cursor.getString(cursor.getColumnIndex(Constants.CONTACTS._ID));
                __contact_name = cursor.getString(cursor.getColumnIndex(Constants.CONTACTS.DISPLAY_NAME));
                __contact_last_updated = cursor.getString( cursor.getColumnIndex(Constants.CONTACTS.CONTACT_LAST_UPDATED) );
                __last_time_contacted = cursor.getString( cursor.getColumnIndex(Constants.CONTACTS.LAST_TIME_CONTACTED) );
                __times_contacted = cursor.getString( cursor.getColumnIndex(Constants.CONTACTS.TIMES_CONTACTED) );

                //For debuging purposes only!
                /*output.append("\n ID :" + __contact_id);
                output.append("\n Name: " + __contact_name);
                output.append("\n Contact Last Updated:  " + __contact_last_updated);
                output.append("\n Last Time Contacts: " + __last_time_contacted);
                output.append("\n Times Contacted: " + __times_contacted);*/

                output.append(__contact_id);
                output.append(__contact_name);
                output.append(__contact_last_updated);
                output.append(__last_time_contacted);
                output.append(__times_contacted);

                //Check if contact has phone number?
                if( Integer.parseInt( cursor.getString(cursor.getColumnIndex(Constants.CONTACTS.HAS_PHONE_NUMBER)) ) > 0 ) {
                    Cursor phone_cursor = contentResolver.query( Constants.CONTACTS.PHONE_CONTENT_URI, null,
                            Constants.CONTACTS.PHONE_CONTACT_ID + " = ?", new String[]{ __contact_id }, null);

                    while ( phone_cursor.moveToNext() ) {
                        __contact_phone_number = phone_cursor.getString( phone_cursor.getColumnIndex(Constants.CONTACTS.PHONE_NUMBER) );
                        //output.append("\n Phone Number: " + __contact_phone_number);
                        output.append(__contact_phone_number);
                    }
                }
                //System.out.println(output.toString());
            }

            return utils.computeHash(output.toString());

        } else {
            return null;
        }

    }
}
