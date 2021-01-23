package jjun.geniusiot.PublicRSS;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by jjun on 2018. 5. 21..
 */
public class ProcessXmlTask extends AsyncTask<String,Void, Void> {
    private static final String TAG = "ProcessXmlTask";
    public static final String XML_EXCEPTION = "jjun.geniusiot.XML_EXCEPTION";

    private Context context;
    private XMLHandler myHandler ;

    public ProcessXmlTask(Context context, XMLHandler handler) {
        this.context = context;
        this.myHandler = handler;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            URL rssURL = new URL(strings[0]);
            SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
            SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
            XMLReader myXMLReader = mySAXParser.getXMLReader();
            myXMLReader.setContentHandler(myHandler);
            InputSource myInputSource = new InputSource(rssURL.openStream());
            myXMLReader.parse(myInputSource);


        } catch (MalformedURLException e1) {
            Log.e(TAG,"MalFormURLException");
            context.sendBroadcast(new Intent(Broadcast.MALFORMEDURLEXCEPTION));
            e1.printStackTrace();
            context.sendBroadcast(new Intent(XML_EXCEPTION));
        } catch (ParserConfigurationException e2) {
            Log.e(TAG,"ParserConfigurationException");
            context.sendBroadcast(new Intent(Broadcast.PARSERCONFIGURATIONEXCEPTION));
            e2.printStackTrace();
            context.sendBroadcast(new Intent(XML_EXCEPTION));
        } catch (SAXException e3) {
            Log.e(TAG,"SAXException");
            context.sendBroadcast(new Intent(Broadcast.SAXEXCEPTION));
            e3.printStackTrace();
            context.sendBroadcast(new Intent(XML_EXCEPTION));
        } catch (IOException e4) {
            Log.e(TAG,"IOException");
            context.sendBroadcast(new Intent());
            context.sendBroadcast(new Intent(Broadcast.IOEXCEPTION));
            e4.printStackTrace();
            context.sendBroadcast(new Intent(XML_EXCEPTION));
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}