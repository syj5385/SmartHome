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
import java.util.ArrayList;
import java.util.HashMap;

import comnet.jjun.geniusiotclient.BluetoothLeManager.BluetoothLeManager;
import comnet.jjun.geniusiotclient.IoTDevice;
import comnet.jjun.geniusiotclient.MainService;
import comnet.jjun.geniusiotclient.Protocol.Device;
import comnet.jjun.geniusiotclient.Protocol.GeniusProtocol;

/**
 * Created by comm on 2018-08-07.
 */

public class MySQL_Connection extends Thread{

    private static final String TAG = "MYSQL_CONNECTION";
    private String result;
    private String url = "http://155.230.15.88/mysql_select.php?table=";
    private IoTDevice iot;
    private MainService service;
    private Context context;

    public MySQL_Connection(MainService service,Context context, IoTDevice iot) {
        this.iot = iot;
        this.context = context;
        this.service = service;
        iot.removeAllDevice();
    }

    @Override
    public void run() {
        super.run();
        final String led_output = request(url+"LED");
        parseJSON(led_output);
        Log.d(TAG,led_output);

        final String window_output = request(url+ "Windows");
        parseJSON(window_output);

        final String etc_output = request(url+ "TEMP");
        parseJSON(etc_output);

        final String gas_output = request(url+ "GAS");
        parseJSON(gas_output);

        final String door_output = request(url + "Door");
        parseJSON(door_output);

        final String bath_output = request(url +"BATH");
        parseJSON(bath_output);

        Log.d(TAG,"SendBroadcast");
        service.sendBroadcast(new Intent(IoTDevice.FINISH_GET_DEVICE));
    }

    private void parseJSON(String result){
        int id, type,v1, v2;
        String name, address;

        try{
            JSONObject root = new JSONObject(result);
            JSONArray ja = root.getJSONArray("result");
            HashMap<Integer, ArrayList<Device>> current = iot.getAllDevice();

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
                int key = -1;
                if (type == GeniusProtocol.LED) {
                    key = IoTDevice.LED_DEVICE;
                }
                else if(type == GeniusProtocol.WINDOW){
                    key = IoTDevice.WINDOW_DEVICE;
                }
                else{
                    key = IoTDevice.ETC_DEVICE;
                }
                ArrayList<Device> picked = current.get(key);
                int j=0;
                for(j=0; j<picked.size(); j++){
                    if(picked.get(j).getDeviceId() == id){
                        break;
                    }
                }
                Log.d(TAG,"j : " + j);
                if(j == picked.size()) {
                    Log.d(TAG,"New Device adding");
                    iot.addDevice(new Device(id, type, name, address, v1, v2, new BluetoothLeManager(service, context, btInfo, id)));
                }
                else{

                }

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
