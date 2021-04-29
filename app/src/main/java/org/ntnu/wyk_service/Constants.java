package org.ntnu.wyk_service;

import android.Manifest;
import android.net.Uri;
import android.provider.ContactsContract;

public class Constants {
    public interface ACTION {
        public static String START_SERVICE = "org.ntnu.wyk_service.START_SERVICE";
        public static String STOP_SERVICE = "org.ntnu.wyk_service.STOP_SERVICE";

        public static String UPDATE_AUTHENTICATION_VALUE = "com.ntnu.wyk_service.UPDATE";
        public static String AUTHENTICATE_USER = "org.ntnu.wyk_service.AUTHENTICATE";
        public static String REGISTER_APPLICATION = "org.ntnu.wyk_service.REGISTER_APPLICATION";
    }
    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public interface PERMISSIONS {
        public static int REQUEST_RUNTIME_PERMISSION = 123;
        public static String[] permissions = {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED
        };
    }

    public interface CONTACTS {
        public static Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

        public static String _ID = ContactsContract.Contacts._ID;
        public static String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        public static String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        public static String TIMES_CONTACTED = ContactsContract.Contacts.TIMES_CONTACTED;
        public static String LAST_TIME_CONTACTED = ContactsContract.Contacts.LAST_TIME_CONTACTED;
        public static String CONTACT_LAST_UPDATED = ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP;

        public static Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        public static String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        public static String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    }

    public interface WYK_SERVER {
        public static String IP = "http://10.24.100.190:9090";      //This is the ip of my laptop
        public static String[] ENDPOINTS = {
                "/wyk/api/v1/user/initialize",
                "/wyk/api/v1/user/update",
                "/wyk/api/v1/user/authenticate"
        };
        public static String[] METHODS = {
                "POST",
                "PUT",
                "GET"
        };
    }

    public interface KEYS {
        public static String ALIAS = "WYKKeys";
    }
}
