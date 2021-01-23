package jjun.geniusiot.PublicRSS;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by jjun on 2018. 5. 22..
 */

public class LocationService {

    private static final String TAG = "LocationService";
    public static final String LOCATION_CURRENT = "jjun.geniusiot.LOCATION_CURRENT";

    private Context context;
    private LocationManager manager;
    private Double latitude, longitude, altitude;
    private GPSListener gpsListener;

    public LocationService(Context context) {
        this.context = context;
        manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        gpsListener = new GPSListener();
        long minTime = 10000; // 1시간에 한번씩 업데이트 요청
        float minDistance = 0;
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime,minDistance,gpsListener);
        }catch (SecurityException e){
            Log.e(TAG,"permission is denied");
            e.printStackTrace();
        }
    }


    private class GPSListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();

            Log.d(TAG,"Latitude : " + latitude +"\tLongitude : " + longitude + "\taltitude : " + altitude);
            Intent gpsIntent = new Intent(LOCATION_CURRENT);
            double[] gpsInfo = {latitude,longitude,altitude};
            gpsIntent.putExtra(LOCATION_CURRENT,gpsInfo);
            context.sendBroadcast(gpsIntent);

            manager.removeUpdates(gpsListener);
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    public Double getAltitude(){
        return altitude;
    }

    public List<Address> getAddress(double lat, double lon){
        String address = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> list = null;

        try{
            list = geocoder.getFromLocation(lat,lon,1);
        }catch (IOException e){
            e.printStackTrace();
        }

        if(list == null){
            Log.d(TAG,"Failed to get Address");
//            Intent intent = new Intent(ProcessXmlTask.XML_EXCEPTION);
//            context.sendBroadcast(intent);
            return null;
        }

        return list;

    }
}
