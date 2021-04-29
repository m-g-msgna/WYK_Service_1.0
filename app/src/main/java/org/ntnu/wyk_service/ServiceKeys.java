package org.ntnu.wyk_service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class ServiceKeys {
    private Context context;
    private KeyPair keyPair;
    private String TAG = "ServiceKeys";

    //Initialize the class with the application context
    public ServiceKeys(Context c){
        super();
        this.context = c;
    }

    //Generate key pairs and store them
    public void Generate() {
        //Set the key specs and generate the keys
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC);
            ECGenParameterSpec keySpec = new ECGenParameterSpec("secp256k1");
            keyPairGenerator.initialize(keySpec, new SecureRandom());

            this.keyPair = keyPairGenerator.generateKeyPair();

        } catch (Exception e){
            Log.w( TAG, e.getMessage() );
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("PublicKey", Base64.encodeToString( keyPair.getPublic().getEncoded(), Base64.NO_WRAP ));
        editor.putString("PrivateKey", Base64.encodeToString( keyPair.getPrivate().getEncoded(), Base64.NO_WRAP ));
        editor.putBoolean("KeyGenerated", true);
        editor.apply();
    }

    /**
     * Sign input data using the service private key.
     * @param data
     */
    public byte[] Sign(String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(this.keyPair.getPrivate());

            signature.update(data.getBytes("UTF-8"));
            return signature.sign();

        } catch (Exception e) {
            Log.w( TAG, e.getMessage() );
        }

        return null;
    }


    /**
     * Verifies signed server message.
     * @param serverKey
     * @param signedData
     * @param plainMessage
     * @return
     * @throws Exception
     */
    public boolean VerifyServerSignature(PublicKey serverKey, byte[] signedData, String plainMessage) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(serverKey);

        signature.update(plainMessage.getBytes("UTF-8"));
        return signature.verify(signedData);
    }

    /**
     * Get public key from base64 string in SharedPreferences.
     * @return
     */
    public PublicKey PublicKey() {
        PublicKey publicKey = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pubKey = sharedPreferences.getString("PublicKey", "");

        try {
            byte[] data = Base64.decode(pubKey, Base64.NO_WRAP);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);

            KeyFactory factory = KeyFactory.getInstance("EC");
            publicKey = factory.generatePublic(spec);

        } catch (GeneralSecurityException e) {
            Log.w( TAG, e.getMessage() );
        }

        return publicKey;
    }

    /**
     * Generate private key from base64 string in sharedpreferences
     * @return
     */
    private PrivateKey getPrivateKey() {
        PrivateKey privateKey = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String priKey = sharedPreferences.getString("PrivateKey", "");

        try {
            byte[] data = Base64.decode(priKey, Base64.NO_WRAP);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);

            KeyFactory factory = KeyFactory.getInstance("EC");
            privateKey = factory.generatePrivate(spec);

        } catch (GeneralSecurityException e) {
            Log.w( TAG, e.getMessage() );
        }

        return privateKey;
    }
}
