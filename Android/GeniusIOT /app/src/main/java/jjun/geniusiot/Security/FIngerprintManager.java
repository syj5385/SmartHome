package jjun.geniusiot.Security;

import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FIngerprintManager {

    private FingerprintManager fingerprintManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;

    private static String KEY_NAME;

    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    private Context context;
    private Activity activity;

    public FIngerprintManager(Activity activity, Context context) {
        super();
        this.activity = activity;
        this.context = context;

        KEY_NAME = String.valueOf((int)Math.random() * 1000 + 1000);

        fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);

        generateKey();
        createKey(KEY_NAME,true);
        if(cipherInit()){
            cryptoObject = new FingerprintManager.CryptoObject(cipher);
            FingerPrintHandler helper = new FingerPrintHandler(context);
            helper.startAuth(fingerprintManager,cryptoObject);
        }

    }

    private void generateKey(){
        try{
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        }catch (NoSuchAlgorithmException | NoSuchProviderException e){
//            e.printStackTrace();
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try{
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME
                    ,KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
            ).build());
            Log.d("FingerPrint","Success to generate Key");

        }catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e){
            throw new RuntimeException(e);
        }
    }

    public boolean cipherInit(){
        try{
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        }catch (NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new RuntimeException("Failed to get Cipher",e);
        }

        try{
            keyStore.load(null);
            SecretKey key = (SecretKey)keyStore.getKey(KEY_NAME,null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        }
        catch (KeyPermanentlyInvalidatedException e){
            return false;
        }
        catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e){
            throw new RuntimeException("Failed to init Cipher",e);
        }
    }

    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {

        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint

        // for your flow. Use of keys is necessary if you need to know if the set of

        // enrolled fingerprints has changed.

        try {

            keyStore.load(null);

            // Set the alias of the entry in Android KeyStore where the key will appear

            // and the constrains (purposes) in the constructor of the Builder



            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,

                    KeyProperties.PURPOSE_ENCRYPT |

                            KeyProperties.PURPOSE_DECRYPT)

                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    // Require the user to authenticate with a fingerprint to authorize every use

                    // of the key

                    .setUserAuthenticationRequired(true)

                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);



            // This is a workaround to avoid crashes on devices whose API level is < 24

            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only

            // visible on API level +24.

            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but

            // which isn't available yet.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);

            }

            keyGenerator.init(builder.build());

            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException

                | CertificateException | IOException e) {

            throw new RuntimeException(e);

        }

    }

}
