package jjun.geniusiot.PublicRSS;

import android.location.Address;
import android.util.Log;

/**
 * Created by jjun on 2018. 5. 21..
 */

public class MyCity {

    public static String getCityNameWithCode(int code){
        String cityname = "..." ;
        switch(code){
            case XMLHandler.NAM :
                cityname = "대구 남구";
                break;

            case XMLHandler.DALSEO :
                cityname = "대구 달서구";
                break;

            case XMLHandler.DALSEONG :
                cityname = "대구 달성군";
                break;

            case XMLHandler.DONG :
                cityname = "대구 동구";
                break;

            case XMLHandler.BUK :
                cityname = "대구 북구";
                break;

            case XMLHandler.SEO :
                cityname = "대구 서구";
                break;

            case XMLHandler.SUSEONG :
                cityname = "대구 수성구";
                break;

            case XMLHandler.JOONG :
                cityname = "대구 중구";
                break;

            default :
                cityname = "...";
        }

        return cityname;
    }

    public static final int LEVEL1 = 0; // veryGood
    public static final int LEVEL2 = 1;
    public static final int LEVEL3 = 2;
    public static final int LEVEL4 = 3;
    public static final int LEVEL5 = 4;
    public static final int LEVEL6 = 5;
    public static final int LEVEL7 = 6;
    public static final int LEVEL8 = 7; // veryBad

    public static String[] getCurrentStateString(int topic, float value){
        switch(topic){
            case XMLHandler.SO2VALUE :
                return getSO2StateString(value);

            case XMLHandler.COVALUE :
                return getCOStateString(value);

            case XMLHandler.O3VALUE :
                return getO3StateString(value);

            case XMLHandler.NO2VALUE :
                return getNO2StateString(value);

            case XMLHandler.PM10VALUE :
                return getPM10StateString(value);

            case XMLHandler.PM25VALUE :
                return getPM25StateString(value);

            default :
                return null;
        }
    }

    private static String[] getPM10StateString(float value){
        if(value >= 0 && value <= 15){
            String[] result = {"0","최고"};
            return result;
        }
        else if(value >= 16 && value <= 30){
            String[] result = {"1","좋음"};
            return result;
        }
        else if(value >= 31 && value <= 40){
            String[] result = {"2","양호"};
            return result;
        }
        else if(value >= 41 && value <= 50){
            String[] result = {"3","보통"};
            return result;
        }
        else if(value >= 51 && value <= 75){
            String[] result = {"4","나쁨"};
            return result;
        }
        else if(value >= 76 && value <= 100){
            String[] result = {"5","상당히 나쁨"};
            return result;
        }
        else if(value >= 101 && value <= 150){
            String[] result = {"6","매우 나쁨"};
            return result;
        }
        else if(value >= 150){
            String[] result = {"7","최악"};
            return result;
        }
        else{
            String[] result = {"-1","모름"};
            return result;
        }
    }

    private static String[] getPM25StateString(float value){
        if(value >= 0 && value <= 8){
            String[] result = {"0","최고"};
            return result;
        }
        else if(value >= 9 && value <= 15){
            String[] result = {"1","좋음"};
            return result;
        }
        else if(value >= 16 && value <= 20){
            String[] result = {"2","양호"};
            return result;
        }
        else if(value >= 21 && value <= 25){
            String[] result = {"3","보통"};
            return result;
        }
        else if(value >= 26 && value <= 37){
            String[] result = {"4","나쁨"};
            return result;
        }
        else if(value >= 38 && value <= 50){
            String[] result = {"5","상당히 나쁨"};
            return result;
        }
        else if(value >= 51 && value <= 75){
            String[] result = {"6","매우 나쁨"};
            return result;
        }
        else if(value >= 76){
            String[] result = {"7","최악"};
            return result;
        }
        else{
            String[] result = {"-1","모름"};
            return result;
        }
    }

    private static String[] getO3StateString(float value){
        if(value >= 0 && value <= 0.02){
            String[] result = {"0","최고"};
            return result;
        }
        else if(value >= 0.02 && value < 0.03){
            String[] result = {"1","좋음"};
            return result;
        }
        else if(value >= 0.03 && value < 0.06){
            String[] result = {"2","양호"};
            return result;
        }
        else if(value >= 0.06 && value < 0.09){
            String[] result = {"3","보통"};
            return result;
        }
        else if(value >= 0.09 && value < 0.12){
            String[] result = {"4","나쁨"};
            return result;
        }
        else if(value >= 0.12 && value < 0.15){
            String[] result = {"5","상당히 나쁨"};
            return result;
        }
        else if(value >= 0.15 && value < 0.38){
            String[] result = {"6","매우 나쁨"};
            return result;
        }
        else if(value >= 0.38){
            String[] result = {"7","최악"};
            return result;
        }
        else{
            String[] result = {"-1","모름"};
            return result;
        }
    }

    private static String[] getNO2StateString(float value){
        if(value >= 0 && value < 0.02){
            String[] result = {"0","최고"};
            return result;
        }
        else if(value >= 0.02 && value < 0.03){
            String[] result = {"1","좋음"};
            return result;
        }
        else if(value >= 0.03 && value < 0.05){
            String[] result = {"2","양호"};
            return result;
        }
        else if(value >= 0.05 && value < 0.06){
            String[] result = {"3","보통"};
            return result;
        }
        else if(value >= 0.06 && value < 0.13){
            String[] result = {"4","나쁨"};
            return result;
        }
        else if(value >= 0.13 && value < 0.2){
            String[] result = {"5","상당히 나쁨"};
            return result;
        }
        else if(value >= 0.2 && value < 1.1){
            String[] result = {"6","매우 나쁨"};
            return result;
        }
        else if(value >= 1.1){
            String[] result = {"7","최악"};
            return result;
        }
        else{
            String[] result = {"-1","모름"};
            return result;
        }
    }

    private static String[] getCOStateString(float value){
        if(value >= 0 && value < 1){
            String[] result = {"0","최고"};
            return result;
        }
        else if(value >= 1 && value < 2){
            String[] result = {"1","좋음"};
            return result;
        }
        else if(value >= 2 && value < 5.5){
            String[] result = {"2","양호"};
            return result;
        }
        else if(value >= 5.5 && value < 9){
            String[] result = {"3","보통"};
            return result;
        }
        else if(value >= 9 && value < 12){
            String[] result = {"4","나쁨"};
            return result;
        }
        else if(value >= 12 && value < 15){
            String[] result = {"5","상당히 나쁨"};
            return result;
        }
        else if(value >= 15 && value < 32){
            String[] result = {"6","매우 나쁨"};
            return result;
        }
        else if(value >= 32){
            String[] result = {"7","최악"};
            return result;
        }
        else{
            String[] result = {"-1","모름"};
            return result;
        }
    }

    private static String[] getSO2StateString(float value){
        if(value >= 0 && value <= 0.01){
            String[] result = {"0","최고"};
            return result;
        }
        else if(value >= 0.01 && value < 0.02){
            String[] result = {"1","좋음"};
            return result;
        }
        else if(value >= 0.02 && value < 0.04){
            String[] result = {"2","양호"};
            return result;
        }
        else if(value >= 0.04 && value < 0.05){
            String[] result = {"3","보통"};
            return result;
        }
        else if(value >= 0.05 && value < 0.1){
            String[] result = {"4","나쁨"};
            return result;
        }
        else if(value >= 0.1 && value < 0.15){
            String[] result = {"5","상당히 나쁨"};
            return result;
        }
        else if(value >= 0.15 && value < 0.6){
            String[] result = {"6","매우 나쁨"};
            return result;
        }
        else if(value >= 0.6){
            String[] result = {"7","최악"};
            return result;
        }
        else{
            String[] result = {"-1","모름"};
            return result;
        }
    }

    public static int getCityCode(Address address){
        String cityname = address.getLocality();
        Log.d("MyCity","city : " + cityname);

        String subLocality = address.getSubLocality();
        Log.d("MyCity","city : " + cityname);
        Log.d("MyCity","sub city : " + subLocality);
        Log.d("call",address.getAddressLine(0));
        Log.d("call",address.getSubLocality());
//        if(cityname.equals("대구광역시")){
            int cityCode = -1;
            if(subLocality.equals("남구")) {
                return XMLHandler.NAM;
            }
            else if(subLocality.equals("달서구")){
                return XMLHandler.DALSEO;
            }

            else if(subLocality.equals("달성군")){
                return XMLHandler.DALSEONG;
            }

            else if(subLocality.equals("동구")){
                return XMLHandler.DONG;
            }

            else if(subLocality.equals("북구")){
                return XMLHandler.BUK;
            }

            else if(subLocality.equals("서구")){
                return XMLHandler.SEO;
            }

            else if(subLocality.equals("수성구")){
                return XMLHandler.SUSEONG;
            }

            else if(subLocality.equals("중구")){
                return XMLHandler.JOONG;
            }
            else{
                return -1;
            }

      //  }
       // else
        //    return -1;


    }
}
