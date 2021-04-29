package org.ntnu.wyk_service;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public boolean checkPermissions(Context context, String permission){
        if( ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED ) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermission(Activity activity, String[] permissions, int code) {
        if( ContextCompat.checkSelfPermission(activity, permissions[0]) !=
            PackageManager.PERMISSION_GRANTED){
            if( ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permissions[0] ) ) {

            } else {
                ActivityCompat.requestPermissions(activity, permissions, code);
            }
        }
    }

    private String HextoString(byte[] input){
        StringBuffer output = new StringBuffer();
        for(int i = 0; i < input.length; i++) {
            output.append( Integer.toHexString(0XFF & input[i] ) );
        }
        return output.toString();
    }

    /**
     *
     * @param input
     * @return
     */
    private String toBase64String(byte[] input){
        return Base64.encodeToString(input, Base64.DEFAULT );
    }

    /**
     * Compute hash value (SHA-256) over a given string.
     * @param input
     * @return
     */
    public String computeHash(String input){
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            //Handle exception here
        }

        messageDigest.reset();

        //return HextoString( messageDigest.digest(input.getBytes()) );

        return toBase64String( messageDigest.digest( input.getBytes() ) );

        //check if Android API is less that 19.
        /*if( Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ){
            return new String(messageDigest.digest(input.getBytes()));
        } else {
            return new String(messageDigest.digest(input.getBytes()), StandardCharsets.UTF_8);
        }*/
    }
}
