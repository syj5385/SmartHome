package f6.iot_project.FileManagement;

import android.database.sqlite.SQLiteClosable;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by comm on 2018-04-26.
 */

public class DirectoryManager extends SQLiteClosable{
    private static final String TAG = "DirectoryManager";



    public static boolean mkdir(String path, String dirname){
        Log.d(TAG,"Directory Name : " + path + "/" + dirname);
        String dirPath = path + "/" + dirname;
        File file = new File(dirPath);
        if(file.mkdirs()){
            Log.d(TAG, "Success to create Directory");
            return true;
        }
        else{
            Log.d(TAG,"Failed : This directory may be already exist");
            return false;
        }
    }

    @Override
    protected void onAllReferencesReleased() {

    }
}
