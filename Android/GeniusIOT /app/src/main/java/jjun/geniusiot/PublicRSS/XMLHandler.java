package jjun.geniusiot.PublicRSS;

import android.os.Handler;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

/**
 * Created by jjun on 2018. 5. 21..
 */

public class XMLHandler extends DefaultHandler {
    private static final String TAG = "XMLHandler";
    private Handler mHandler;
    public static final int XML_TIME = 101;
    public static final int XML_RESULT = 100;
    public static final int XML_EXCEPTION = 102;

    private String researchTime = "";

    private HashMap<Integer,HashMap<Integer,Float>> cityHash = new HashMap<>();


    //Hash Index
    public static final int NOTHING = -1;
    public static final int NAM = 0;
    public static final int DALSEO = 1;
    public static final int DALSEONG = 2;
    public static final int DONG =3;
    public static final int BUK = 4;
    public static final int SEO = 5;
    public static final int SUSEONG = 6;
    public static final int JOONG = 7;

    //cityAir Hash
    public static final int SO2VALUE = 0;
    public static final int COVALUE = 1;
    public static final int O3VALUE = 2;
    public static final int NO2VALUE = 3;
    public static final int PM10VALUE = 4;
    public static final int PM25VALUE = 5;

    private boolean isSenddataTime = false;
    private int currentCity = -1;

    // current state
    int readState = 0;
    final int stateUnknown = 0;
    final int reading = 1;
    final int stateValue = 2;

    String strElement = "";

    public XMLHandler(Handler mHandler) {
        super();
        this.mHandler = mHandler;
    }

    @Override
    public void startDocument() throws SAXException {
        Log.d(TAG,"Start Document");
    }

    @Override
    public void endDocument() throws SAXException {
        Log.d(TAG,"End Document");
        mHandler.obtainMessage(XML_RESULT, cityHash).sendToTarget();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if(localName.equals("cityName")  || localName.equals("so2Value") || localName.equals("dataTime")
                || localName.equals("coValue") || localName.equals("o3Value") ||
                localName.equals("no2Value") || localName.equals("pm10Value") || localName.equals("pm25Value")) {
            readState = reading;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(localName.equals("cityName")){
            currentCity = getCityCode(strElement);
            Log.d(TAG,"This City : " + strElement + "\tcity Code : " + currentCity);
        }
        else if(localName.equals("dataTime")){
            if(!isSenddataTime){
                mHandler.obtainMessage(XML_TIME,strElement).sendToTarget();
                isSenddataTime = true;
            }
        }
        else {
            int localCode = getLocalNameCode(localName);
            if(localCode == -1){
//                Log.d(TAG,"Unknown value");
                return;
            }

            float value;
            try{
                value = Float.parseFloat(strElement);

            }catch (NumberFormatException e){
                value = -1;
            }

            Log.d(TAG,"localCode : " + localCode + "\t->  "  + strElement);
            HashMap<Integer,Float> temp = cityHash.get(currentCity);
            temp.put(localCode,value);
            cityHash.put(currentCity,temp);
        }
        strElement = "";
        readState = stateUnknown;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String strCharacters = new String(ch,start,length);
        if(readState == reading ){
            strElement += strCharacters;
        }

    }

    private int getCityCode(String cityName){
        int cityCode = NOTHING;
//        Log.d(TAG,"This city : " + cityName);

        if(cityName.equals("남구")){
            cityCode = NAM;
        }
        else if(cityName.equals("달서구")){
            cityCode = DALSEO;
        }
        else if(cityName.equals("달성군")){
            cityCode = DALSEONG;
        }
        else if(cityName.equals("동구")) {
            cityCode = DONG;
        }
        else if(cityName.equals("북구")){
            cityCode = BUK;
        }
        else if(cityName.equals("서구")){
            cityCode = SEO;
        }
        else if(cityName.equals("수성구")){
            cityCode = SUSEONG;
        }
        else if(cityName.equals("중구")){
            cityCode = JOONG;
        }

        if(cityCode != NOTHING){
            HashMap<Integer,Float> cityAir = new HashMap<Integer, Float>(){
                @Override
                public Float put(Integer key, Float value) {
                    Log.d(TAG,"Key : " + key + "\tvalue : " + value);
                    return super.put(key, value);
                }
            };
            cityHash.put(cityCode,cityAir);
            Log.d(TAG,"current City Size : " + cityHash.size());
        }
        return cityCode;
    }

    private int getLocalNameCode(String localName){
        if (localName.equals("so2Value")) {
            return SO2VALUE;
        }
        if (localName.equals("coValue")) {
            return COVALUE;
        }
        if (localName.equals("o3Value")) {
            return O3VALUE;
        }
        if (localName.equals("no2Value")) {
            return NO2VALUE;
        }
        if (localName.equals("pm10Value")) {
            return PM10VALUE;
        }
        if (localName.equals("pm25Value")) {
            return PM25VALUE;
        }
        else{
            return NOTHING;
        }
    }
}