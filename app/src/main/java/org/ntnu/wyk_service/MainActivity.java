package org.ntnu.wyk_service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //private boolean isWYKRunning = false;
    private Button buttonStart, buttonStop;
    private EditText nameText;

    private Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //nameText = (EditText)findViewById(R.id.fullName);

        buttonStart = (Button)findViewById(R.id.btnStart);
        buttonStop = (Button)findViewById(R.id.btnStop);

        //Check Permissions
        if(!utils.checkPermissions(this, Constants.PERMISSIONS.permissions[0])) {
            utils.requestPermission(MainActivity.this,
                    Constants.PERMISSIONS.permissions,
                    Constants.PERMISSIONS.REQUEST_RUNTIME_PERMISSION);
        }

    }

    public void startWYKService(View view){
        Intent startIntent = new Intent(this, WYKService.class);
        //startIntent.setAction(Constants.ACTION.START_SERVICE);
        //startIntent.putExtra("nameExtra", nameText.getText().toString());

        startIntent.putExtra("nameExtra", getString(R.string.running_notification));
        startService(startIntent);
    }

    public void stopWYKService(View view){
        Intent stopIntent = new Intent(MainActivity.this, WYKService.class);
        //stopIntent.setAction(Constants.ACTION.STOP_SERVICE);
        stopService(stopIntent);
    }


    /**
     * Check if required runtime permissions are granted.
     * @param permsRequestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch (permsRequestCode) {
            case Constants.PERMISSIONS.REQUEST_RUNTIME_PERMISSION: {
                if( grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Update or
                    // Register
                } else {
                    Toast.makeText(this, "Please grant WYK Service the permission to proceed.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
