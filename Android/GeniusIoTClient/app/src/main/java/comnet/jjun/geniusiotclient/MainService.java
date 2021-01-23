package comnet.jjun.geniusiotclient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import comnet.jjun.geniusiotclient.BluetoothLeManager.BluetoothLeManager;
import comnet.jjun.geniusiotclient.MySQL.MYSQL_UpdateDevice;
import comnet.jjun.geniusiotclient.MySQL.MySQL_Connection;
import comnet.jjun.geniusiotclient.Protocol.Command;
import comnet.jjun.geniusiotclient.Protocol.Device;
import comnet.jjun.geniusiotclient.Protocol.GeniusProtocol;
import comnet.jjun.geniusiotclient.Protocol.Packet;
import comnet.jjun.geniusiotclient.TCP_Client.TCP_Connection;

/**
 * Created by comm on 2018-08-08.
 */

public class MainService extends Service {
    private static final String TAG = "MainService";

    public IBinder binder = new MainBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MainBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    private MySQL_Connection db_conn;
    private TCP_Connection server_conn;
    private IoTDevice iot;
    private Command command;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate Service");
        setFilter();
        initializeObject();
    }

    private void initializeObject(){
        Log.d(TAG,"Initialize Object");
        iot = (IoTDevice)this.getApplication();
        command = new Command();
        getDeviceFromDB();

        server_conn = new TCP_Connection(this,10000);
        server_conn.connectToServer();

    }

    public void getDeviceFromDB(){
        db_conn = new MySQL_Connection(this,this,iot);
        db_conn.start();
    }


    private void connectBluetoothAllDevice(){
        for(int i=0; i<iot.getAllDevice().size(); i++){
            final ArrayList<Device> temp = iot.getAllDevice().get(i);
            for(int j=0; j<temp.size(); j++){
                final BluetoothLeManager thisBle = temp.get(j).getBle();

                if(thisBle.getState() != BluetoothLeManager.STATE_CONNECTED) {
                    Log.d(TAG,"Bluetooth Connect : " + temp.get(j).getDevice_address());
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
                            thisBle.connect();
//                        }
//                    }).start();

//                    if (temp.get(j).getDeviceType() == GeniusProtocol.TEMP || temp.get(j).getDeviceType() == GeniusProtocol.GAS || temp.get(j).getDeviceType() == GeniusProtocol.BATH) {
//                        temp.get(j).getBle().setCharacteristicNotification(true);
//                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void disconnectBluetoothAllDevice(){
        for(int i=0; i<iot.getAllDevice().size(); i++){
            ArrayList<Device> temp = iot.getAllDevice().get(i);
            for(int j=0; j<temp.size(); j++){
                temp.get(j).getBle().disconnect();
                temp.get(j).getBle().destory();
                temp.get(j).setBle(null);
                try{
                    Thread.sleep(50);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectBluetoothAllDevice();

        server_conn.disconnect();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(IoTDevice.FINISH_GET_DEVICE)){
                Log.d(TAG,"Finished get device");
                connectBluetoothAllDevice();
            }
            else if(action.equals(IoTDevice.BLUETOOTH_CONNECT)){
                String address =  intent.getStringExtra("address");
                Log.d(TAG,"Finished to create BLE Connection with " + address);
//                iot.getAllDevice().get(IoTDevice.LED_DEVICE).get(0).getBle().write("Hello".getBytes());
            }
            else if(action.equals(IoTDevice.BLUETOOTH_RECEIVED_DATA)){
                int device_id = intent.getIntExtra("id",-1);
                int device_type;
                if(device_id == -1)
                    return;
                Log.d(TAG,"Bluetooth Received -> from " + device_id);

                byte[] data = intent.getByteArrayExtra(IoTDevice.BLUETOOTH_RECEIVED_DATA);
                byte[] s_packet = new byte[4];
                Device device;
                //test Device type
                if((device = iot.getDeviceWithID(GeniusProtocol.LED,device_id) )!= null) {
                    device_type = GeniusProtocol.LED;
                }
                else if((device = iot.getDeviceWithID(GeniusProtocol.WINDOW,device_id) )!= null) {
                    device_type = GeniusProtocol.WINDOW;
                }
                else if((device = iot.getDeviceWithID(0,device_id) )!= null) {
                    Log.d(TAG,"This device : " +device.getDeviceType());
                    device_type = device.getDeviceType();
                }
//                if(iot.getDeviceWithID(GeniusProtocol.GAS,device_id) != null) {
//                    Log.d(TAG,"This device : GAS");
//                    device_type = GeniusProtocol.GAS;
//                }
                else {
                    Log.d(TAG,"Unknown device type");
                    return;
                }

                if(device_type == GeniusProtocol.BATH){
                    getDeviceFromDB();
                }

                s_packet[0] = (byte)device_id;
                s_packet[1] = (byte)device_type;
                s_packet[2] = data[0];
                s_packet[3] = data[1];
                Log.d(TAG,"value : " + ((int)s_packet[2] & 0xff ));
                server_conn.send(Command.request_send_BLE_data(s_packet));
            }
            else if(action.equals(IoTDevice.TCP_RECEIVED_DATA)){
                Packet packet = command.readPacketFromServer(intent.getByteArrayExtra(IoTDevice.TCP_RECEIVED_DATA));
                Log.d(TAG,"TCP Command : " + packet.getCommand());
                executeCommand(packet);

            }
            else if(action.equals(IoTDevice.REQUEST_UPDATE_DEVICE)){
                int id = intent.getIntExtra("ID",-1);
                int type = intent.getIntExtra("TYPE",-1);
                int[] value = intent.getIntArrayExtra("VALUE");
                if(id != -1 && type != -1 && value != null){
                    Log.d(TAG,"UPDATE DEVICE FROM Controller");
                    if(value[0] != 0 && value[1] != 0) {
                        Thread update = new MYSQL_UpdateDevice(MainService.this, MainService.this, id, type, value);
                        update.start();
                    }
                    Device device = iot.getDeviceWithID(type,id);
                    if(device == null){
                        Log.d(TAG,"Cannot found \"id="+id+"\" device");
                        return;
                    }

                    updateBleDevice(device);
                    sendBroadcast(new Intent(IoTDevice.UPDATE_DEVICE));
                }
            }
        }
    };

    private void updateBleDevice(Device device){
        byte[] writeData = {(byte) device.getvalue()[0], (byte) device.getvalue()[1]};
        device.getBle().write(writeData);
        Log.d(TAG,"write");
    }

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(IoTDevice.FINISH_GET_DEVICE);
        filter.addAction(IoTDevice.BLUETOOTH_CONNECT);
        filter.addAction(IoTDevice.TCP_RECEIVED_DATA);
        filter.addAction(IoTDevice.BLUETOOTH_RECEIVED_DATA);
        filter.addAction(IoTDevice.REQUEST_UPDATE_DEVICE);

        registerReceiver(mReceiver,filter);
    }

    private void executeCommand(Packet packet){
        int request = packet.getCommand();
        switch(request){
            case GeniusProtocol.UPDATE_DEVICE :
//                db_conn = new MySQL_Connection(this,this,iot);
//                db_conn.start();
                for(int i=0; i<packet.getParameter().length; i++){
                    Log.d(TAG,i + " : " + ((int)packet.getParameter()[i] & 0xff));
                }
                int device_id = ((int)packet.getParameter()[0] & 0xff);
                int device_type = ((int)packet.getParameter()[2] & 0xff);
                int v1 = ((int)packet.getParameter()[4] & 0xff);
                int v2 = ((int)packet.getParameter()[6] & 0xff);
                Device thisDevice = iot.getDeviceWithID(device_type,device_id);
                int[] new_v = {v1, v2};
                if(thisDevice != null) {
                    thisDevice.setValue(new_v);
                    updateBleDevice(thisDevice);
//                    byte[] writeData = {(byte) v1, (byte) v2};
//                    thisDevice.getBle().write(writeData);
//                    Log.d(TAG,"write");
                }
                Intent intent = new Intent(IoTDevice.UPDATE_DEVICE);
                int[] update = {device_id,device_type,v1,v2};
                intent.putExtra(IoTDevice.UPDATE_DEVICE,update);
                sendBroadcast(intent);
                break;

            case GeniusProtocol.ADD_DEVICE :
                Log.d(TAG,"ADD Device");
                int id = (int)packet.getParameter()[0] & 0xff;
                int type = (int)packet.getParameter()[2] & 0xff;
                String data = new String(packet.getParameter());
                data = data.substring(4,data.length()-1);
                String[] subInfo = data.split("%");
                Log.d(TAG,"device id : " + id);
                Log.d(TAG,"device type : " + type);
                Log.d(TAG,"device name : " + subInfo[0]);
                Log.d(TAG,"device addr : " + subInfo[1]);
                String[] btInfo = {subInfo[0],subInfo[1]};
                iot.addDevice(new Device(id,type,subInfo[0],subInfo[1],100,120,new BluetoothLeManager(MainService.this,MainService.this,btInfo,id)));
                iot.getDeviceWithID(type,id).getBle().connect();
                sendBroadcast(new Intent(IoTDevice.FINISHED_ADD_DEVICE));
                break;

            case GeniusProtocol.REMOVE_DEVICE:
                int remove_id = ((int)packet.getParameter()[0] & 0Xff);
                Log.d(TAG,"Remove Device -> " + remove_id);
                if(iot.removeDeviceWithID(remove_id)){
                    Intent f_removeintent = new Intent(IoTDevice.FINISHED_REMOVE_DEVICE);
                    sendBroadcast(f_removeintent);
                }
                break;

            case GeniusProtocol.BLE_DATA :
                Log.d(TAG,"Updated BLE data");
                break;

        }
    }
}
