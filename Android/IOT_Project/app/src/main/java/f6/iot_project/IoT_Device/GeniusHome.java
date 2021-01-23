package f6.iot_project.IoT_Device;

import android.app.Application;

import java.util.ArrayList;

/**
 * Created by comm on 2018-04-23.
 */

public class GeniusHome extends Application{

    private ArrayList<Integer> isControlled = new ArrayList<>();

    public void addControlledDevice(int id){
        isControlled.add(id);
    }

    public void removeControlledDevice(int id){
        for(int i=0; i<isControlled.size(); i++){
            if(isControlled.get(i) == id){
                isControlled.remove(i);
            }
        }
    }

    public ArrayList<Integer> getWidget_list(){
        return isControlled;
    }

    public int getCount(){
        return isControlled.size();
    }


    private int notificationLoop = 3000;

    public void setNotificationLoop(int notificationLoop){
        this.notificationLoop = notificationLoop;
    }

    public int getNotificationLoop(){
        return notificationLoop;
    }

    private int notificationNewDevice = 600000;

    public void setNotificationNewDevice(int notificationNewDevice){
        this.notificationNewDevice = notificationNewDevice;
    }

    public int getNotificationNewDevice(){
        return notificationNewDevice;
    }

    private boolean isNotification = false;

    public boolean isNotification(){
        return isNotification;
    }

    public void setIsControlled(boolean isNotification) {
        this.isNotification = isNotification;
    }

    private ArrayList<Device> selectedDevice = new ArrayList<>();

    public void setSelectedDevice(ArrayList<Device> list){
        this.selectedDevice = list;
    }

    public ArrayList<Device> getSelectedDevice(){
        return selectedDevice;
    }

    private ArrayList<Device> readDevice = new ArrayList<>();

    public void setReadDevice(ArrayList<Device> list){
        this.readDevice = list;
    }

    public ArrayList<Device> getReadDevice(){
        return readDevice;
    }



}
