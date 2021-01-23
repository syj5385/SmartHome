package comnet.jjun.geniusiotclient.MySQL;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import comnet.jjun.geniusiotclient.BluetoothLeManager.BluetoothLeManager;
import comnet.jjun.geniusiotclient.IoTDevice;
import comnet.jjun.geniusiotclient.MainService;
import comnet.jjun.geniusiotclient.Protocol.Device;
import comnet.jjun.geniusiotclient.Protocol.GeniusProtocol;

public class MYSQL_UpdateDevice extends Thread{

    private static final String TAG = "MYSQL_CONNECTION";
    private String result;
    private String url = "http://155.230.15.88:11113/mysql_update.php?";
    private IoTDevice iot;
    private MainService service;
    private Context context;
    private int id;
    private int type;
    private int[] value;

    public MYSQL_UpdateDevice(MainService service,Context context, int id, int type,  int[] value) {
        this.iot = iot;
        this.context = context;
        this.service = service;
        this.id = id;
        this.type = type;
        this.value = value;
    }

    @Override
    public void run() {
        super.run();
        String table = getDeviceTable(type);
        if(table == null)
            return;

        url += "table="+table;
        url += "&id="+id;
        url += "&value1="+value[0];
        url += "&value2="+value[1];

        String result = request(url);
        if(result.contains("Failed")){
            service.sendBroadcast(new Intent(IoTDevice.FAILED_UPDATE_DEVICE));
            Log.d(TAG,"failed to update device using php web server");
        }
        else if(result.contains("Success")){
            service.sendBroadcast(new Intent(IoTDevice.FINISHED_UPDATE_DEVICE));
            Log.d(TAG,"Success to update device using php web server");
        }
        else{
            Log.d(TAG,"Unknown Type result");
        }

    }

    private String getDeviceTable(int type){
        switch(type){
            case GeniusProtocol.LED :
                return "LED";

            case GeniusProtocol.WINDOW :
                return "Windows";


            case GeniusProtocol.DOOR :
                return "Door";

            default :
                return null;
        }
    };

    private void parseJSON(String result){
        int id, type,v1, v2;
        String name, address;

        try{
            JSONObject root = new JSONObject(result);
            JSONArray ja = root.getJSONArray("result");
            for(int i=0 ; i<ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                id = Integer.parseInt(jo.getString("id"));
                type = Integer.parseInt(jo.getString("type"));
                name = jo.getString("name");
                address = jo.getString("address");
                v1 = Integer.parseInt(jo.getString("v1"));
                v2 = Integer.parseInt(jo.getString("v2"));
                Log.d(TAG,"Device : name->"+name + ",address->" + address);
                String[] btInfo = {name, address};
                iot.addDevice(new Device(id,type,name,address,v1,v2,new BluetoothLeManager(service,context,btInfo,id)));
            }

        }catch (JSONException e){
            e.printStackTrace();
            Log.d(TAG,"JSON Parsing Exception");
        }

    }

    private String request(String urlStr){
        StringBuilder output = new StringBuilder();
        try{
            URL url_db = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url_db.openConnection();
            if(conn != null){
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                int resCode = conn.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line == null){
                            break;
                        }
                        output.append(line + "\n");
                    }
                    reader.close();
                    conn.disconnect();
                }

            }
        }
        catch (MalformedURLException e){
            e.printStackTrace();
            Log.d(TAG,"MalformedURLException");
        }
        catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"IOException");
        }

        return output.toString();
    }


}

