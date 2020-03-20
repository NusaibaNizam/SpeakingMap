package org.beginners.saran.map;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DirecctionActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    TextToSpeech myTTS;
    SpeechRecognizer mySpeechRecognizer;
    boolean isMapCheck=false;
    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/";
    private DirectionService service;
    GoogleMap map;
    GoogleMapOptions options;
    private String origin;
    private String destination=null;
    double lat, lng,deslat=181, deslng;
    LatLng latLng;
    ArrayList<MyItem> clusterItems = new ArrayList<>();
    ClusterManager<MyItem> clusterManager;
    Location lastLocation;
    TextView durTV;
    TextView desTV;
    EditText fromET;
    EditText toET;
    Address desAddress,originAddress;
    boolean mapCheck;
    SupportMapFragment mapFragment;
    Intent intent;
    static final int ORIGIN_REQUEST=2;
    static final int DES_REQUEST=3;
    static final int WAY_REQUEST=4;
    int totalRoutes;
    int route=0;
    String urlString;
    Fragment currentFragment;
    LatLngBounds.Builder builder;
    private FusedLocationProviderClient client;
    Place place;
    BottomNavigationView bottomNavigationView;
    String mode="walking";
    Button nextRoute;
    FragmentTransaction ft;
    FragmentManager fragmentManager;
    ArrayList<Instruction> instructions=new ArrayList<>();
    InstructionFragment instructionFragment;
    boolean start;
    double dLat;
    double dLng;
    private boolean speak;
    private Handler handler;
    private Runnable myRunnable;
    private String res;
    private boolean sayNext;
    private int index;
    private Address address;
    int mAzimuth;
    SensorManager mSensorManager;
    Sensor mRotationV,mAccelerometer,mMagnetometer;
    float[] mMat=new float[9];
    float[] orientation= new float[9];
    float[] mLastAccelerometer=new float[3];
    float[] mLastMagnetometer=new float[3];
    boolean hasSensor=false,hasSensor2=false,mLastAccelerometerSet=false,mLastMagnetometerSet=false;
    private boolean noSensor=false;
    private String facingWay;
    private String north;
    private String south;
    private String west;
    private String east;
    private String towards;
    private String towardsD;
    private String lessTowards;
    private String lessTowardsD;
    private LocationManager locationManager;
    private boolean check;
    private double newlat;
    private double newlng;
    private LatLng newlatLng;
    private Address newaddress;
    private String adressLine;
    int timer=0;
    boolean changeLoc=false;
    private boolean phoneNumber;
    private boolean lowSaid=false;
    private int batteryLevel;
    private boolean yes=false;
    private SharedPreferences shref;
    private ArrayList<Contacts> emergencyArrayList;
    private boolean smsPhoneNumber;
    private String emLocation;
    private String phoneString0;
    private boolean numeric;

    private boolean lowSaid2;
    private BroadcastReceiver batteryReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            batteryLevel=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            if(batteryLevel<=10&&!lowSaid2 &&lowSaid){
                boolean speakingEnd;

                speak("Emergency! Your battery is critically low! Battery level "+batteryLevel+"% !");

                do {
                    speakingEnd = myTTS.isSpeaking();
                } while (speakingEnd);
                lowSaid2=true;
            }
            else if (batteryLevel<=25&&!lowSaid){

                boolean speakingEnd;

                speak("Low battery! Battery level "+batteryLevel+"% !");

                do {
                    speakingEnd = myTTS.isSpeaking();
                } while (speakingEnd);

                if(ContextCompat.checkSelfPermission(DirecctionActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

                    if (emergencyArrayList != null) {
                        for (Contacts contacts : emergencyArrayList) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String location;
                                    if (address == null) {
                                        location = "latitude:" + lat + " longitude: " + lng + ".";
                                    } else {
                                        location = address.getAddressLine(0) + ". latitude:" + lat + " longitude: " + lng + ".";

                                    }
                                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                    sendMSG(context, contacts,null, "Help my battery is low! " + batteryLevel + "%! My location is " + location,timestamp.toString());
                                }
                            }, 30000);
                        }
                    }
                }

                lowSaid=true;


            } else if(batteryLevel>25 && lowSaid){
                lowSaid=false;
                lowSaid2=false;
            }
        }
    };

    public void sendMSG(Context context, @Nullable Contacts contacts, @Nullable String phoneString, String text, String id){



        String SENT = id+"_SMS_SENT";
        String DELIVERED = id+"_SMS_DELIVERED";

        SmsManager smsManager=SmsManager.getDefault();
        ArrayList<String> messages = smsManager.divideMessage(text);
        ArrayList<PendingIntent> listOfIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> listOfDIntents = new ArrayList<PendingIntent>();

        for (int i=0; i < messages.size(); i++){
            Intent sentIntent= new Intent(SENT);
            sentIntent.putExtra("SMS_SENT_ID",id);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0,sentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            listOfIntents.add(pi);
            Intent deliverIntent=new Intent(DELIVERED);
            deliverIntent.putExtra("SMS_DELIVERED_ID",id);
            PendingIntent di=PendingIntent.getBroadcast(this, 0, deliverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            listOfDIntents.add(di);
        }

// ---when the SMS has been sent---
        context.registerReceiver(
                new BroadcastReceiver()
                {
                    private boolean speakingEnd;

                    @Override
                    public void onReceive(Context arg0,Intent arg1)
                    {
                        switch(getResultCode())
                        {
                            case Activity.RESULT_OK:

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                if(contacts!=null){
                                    speak("Emergency SMS sent to "+contacts.getName());
                                }else {
                                    speak("Emergency SMS sent to "+phoneString);
                                }
                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                arg0.unregisterReceiver(this);
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                if(contacts!=null){
                                    speak("Error Emergency SMS not sent to "+contacts.getName());
                                }else {
                                    speak("Error Emergency SMS not sent to "+phoneString);
                                }
                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                arg0.unregisterReceiver(this);
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                if(contacts!=null){
                                    speak("Error Emergency SMS not sent to "+contacts.getName());
                                }else {
                                    speak("Error Emergency SMS not sent to "+phoneString);
                                }
                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                arg0.unregisterReceiver(this);
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                if(contacts!=null){
                                    speak("Error Emergency SMS not sent to "+contacts.getName());
                                }else {
                                    speak("Error Emergency SMS not sent to "+phoneString);
                                }

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                arg0.unregisterReceiver(this);
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                if(contacts!=null){
                                    speak("Error Emergency SMS not sent to "+contacts.getName());
                                }else {
                                    speak("Error Emergency SMS not sent to "+phoneString);
                                }
                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                arg0.unregisterReceiver(this);
                                break;
                        }
                    }
                }, new IntentFilter(SENT));
        // ---when the SMS has been delivered---
        context.registerReceiver(
                new BroadcastReceiver()
                {

                    private boolean speakingEnd;

                    @Override
                    public void onReceive(Context arg0,Intent arg1)
                    {
                        switch(getResultCode())
                        {
                            case Activity.RESULT_OK:

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                if(contacts!=null){
                                    speak("Emergency SMS Delivered to "+contacts.getName());
                                }else {
                                    speak("Emergency SMS Delivered to "+phoneString);
                                }
                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                arg0.unregisterReceiver(this);
                                break;
                            case Activity.RESULT_CANCELED:

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                if(contacts!=null){
                                    speak("Error Emergency SMS not Delivered to "+contacts.getName());
                                }else {
                                    speak("Error Emergency SMS not Delivered to "+phoneString);
                                }
                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                arg0.unregisterReceiver(this);
                                break;
                        }
                    }
                }, new IntentFilter(DELIVERED));
        if(contacts!=null)
            smsManager.sendMultipartTextMessage(contacts.getPhoneNumber(), null, messages, listOfIntents,listOfDIntents);
        else smsManager.sendMultipartTextMessage(contacts.getPhoneNumber(), null, messages, listOfIntents,listOfDIntents);


    }


    @Override
    protected void onPause() {
        super.onPause();
        myTTS.shutdown();

        try {
            this.unregisterReceiver(this.batteryReceiver);
        }catch (IllegalArgumentException e){}
        stopSensorManager();
    }

    private void initializeTTS() {
        myTTS=new TextToSpeech(DirecctionActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0){
                    Toast.makeText(DirecctionActivity.this,"Sorry! No Engines Available For Voice Facilities",Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    myTTS.setLanguage(Locale.ENGLISH);
                    boolean speakingEnd;
                    if(noSensor){
                        speak("Warning! This Device Doesn't Support Compass To Track Which Way You Are Facing! We Request You To Go Back ,If You Are Visually Impaired! It is not Safe With out The Sensors");
                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                    }
                    if(batteryLevel<=25&& batteryLevel!=0)
                        speak("low battery! battery level "+batteryLevel+"%!"+" loading directions");
                    else
                        speak("loading directions");

                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);

                }
            }
        });
    }
    private void initializeSpeechRecognizer() {
        if(SpeechRecognizer.isRecognitionAvailable(this)){
            mySpeechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {
                    List<String> myResults=results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(myResults.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }




    private void processResult(String s) {
        s=s.toLowerCase();
        if(s.indexOf("which way am")!=-1){
            boolean speakingEnd;

            speak("You Are Facing "+facingWay);
            speakingEnd = myTTS.isSpeaking();
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
        } else if(s.indexOf("which way is")!=-1){

            whichWayIs(s);
        }else  if(s.indexOf("call")!=-1){

            if(s.indexOf("emergency")!=-1){
                if(emergencyArrayList!=null){
                    phoneString0=null;
                    if(s.indexOf("1")!=-1|| s.indexOf("one")!=-1){

                        phoneString0=emergencyArrayList.get(0).getPhoneNumber();

                    }else if(s.indexOf("2")!=-1|| s.indexOf("two")!=-1){
                        if(emergencyArrayList.size()>=2){
                            phoneString0=emergencyArrayList.get(0).getPhoneNumber();

                        }else {
                            speak("Emergency 2 is not set, to set emergency number say open emergency window." +
                                    " For this you will need the help of a visually able person");
                        }
                    } else if(s.indexOf("3")!=-1|| s.indexOf("three")!=-1){
                        if(emergencyArrayList.size()>=3){
                            phoneString0=emergencyArrayList.get(0).getPhoneNumber();
                        }else {
                            speak("Emergency 3 is not set, to set emergency number say open emergency window." +
                                    " For this you will need the help of a visually able person");
                        }
                    } else {
                        speak("you can have 3 emergency numbers only!");
                    }
                    if(phoneString0!=null) {
                        call(phoneString0);
                    }
                } else {
                    speak("No emergency contact saved , to set emergency number say open emergency window." +
                            " For this you will need the help of a visually able person");
                }
            } else {
                phoneNumber=true;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        phoneNumber=false;
                    }
                }, 60000);
                speak("what is the number?");
            }


        } else if(s.indexOf("send emergency text") != -1){
            if (ContextCompat.checkSelfPermission(DirecctionActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                speak("You did not give permission for SMS");
            }else {
                int i = 0;
                Pattern p = Pattern.compile("emergency");
                Matcher m = p.matcher( s );
                while (m.find()) {
                    i++;
                }
                if (address == null) {
                    emLocation = "latitude:" + lat + " longitude: " + lng + ".";
                } else {
                    emLocation = address.getAddressLine(0) + ". latitude:" + lat + " longitude: " + lng + ".";

                }
                if(i>=2) {
                    if (emergencyArrayList != null) {

                        boolean speakingEnd;
                        speak("Sending");

                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                        speakingEnd =myTTS.isSpeaking();
                        for (Contacts contacts : emergencyArrayList) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                    sendMSG(DirecctionActivity.this, contacts,null, "Help I am in an emergency! My battery level is " + batteryLevel + "%! My location is " + emLocation,timestamp.toString());

                                }
                            }, 30000);
                        }
                    }  else if(emergencyArrayList==null){

                        boolean speakingEnd;
                        do{
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                        speak("No emergency contact saved , to set emergency number say open emergency window." +
                                " For this you will need the help of a visually able person");

                        speakingEnd =myTTS.isSpeaking();
                        do{
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                    }
                } else if(i>=2){

                    if (emergencyArrayList != null) {

                        boolean speakingEnd;
                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                        speak("Sending");
                        speakingEnd =myTTS.isSpeaking();
                        do{
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                        for (Contacts contacts : emergencyArrayList) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                    sendMSG(DirecctionActivity.this, contacts,null, "Help I am in an emergency! My battery level is " + batteryLevel + "%! My location is " + emLocation,timestamp.toString());

                                }
                            }, 30000);
                        }
                    } else {
                        speak("No Emergency contact saved , to set emergency number say open emergency window." +
                                " For this you will need the help of a visually able person");
                    }
                } else {
                    smsPhoneNumber=true;

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            smsPhoneNumber=false;
                        }
                    }, 60000);
                    speak("what is the number");
                }
            }

        } else if(s.indexOf("open emergency window")!=-1){
            speak(" For this you will need the help of a visually able person. Do you want to proceed?");
            yes=true;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    yes=false;
                }
            }, 60000);

        } else if(yes && s.indexOf("yes")!=-1){
            yes=false;
            Intent intent1=new Intent(DirecctionActivity.this,EmergencyContactActivity.class);
            intent1.putExtra("lowSaid",lowSaid);
            intent1.putExtra("lowSaid2",lowSaid2);
            intent1.putExtra("desLat", deslat);
            intent1.putExtra("desLng", deslng);
            intent1.putExtra("name", toET.getText().toString());
            intent1.putExtra("speak", true);
            if(intent.getBooleanExtra("start",false)){
                intent1.putExtra("startLat",intent.getDoubleExtra("startLat",0));
                intent1.putExtra("startLng",intent.getDoubleExtra("startLng",0));
                intent1.putExtra("startName",intent.getStringArrayExtra("startName"));
                intent1.putExtra("start",true);
            }
            finish();
            startActivity(intent1);
        }else if(yes && s.indexOf("no")!=-1){
            yes=false;
        }
        else if(s.indexOf("all done")!=-1){
            Intent intent=new Intent(DirecctionActivity.this,MapActivity.class);
            intent.putExtra("lowSaid",lowSaid);
            intent.putExtra("lowSaid2",lowSaid2);
            intent.putExtra("batteryLevel",batteryLevel);
            startActivity(intent);

        } else if (s.indexOf("then")!=-1){
            boolean speakingEnd;
            if(!sayNext){
                speak("No more instructions left");
                speakingEnd = myTTS.isSpeaking();
                do {
                    speakingEnd = myTTS.isSpeaking();
                } while (speakingEnd);
            }else {
                if(index<instructions.size()){
                    speak(Html.fromHtml(instructions.get(index).getInstruction()).toString());
                    speakingEnd = myTTS.isSpeaking();
                    do {

                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    index++;
                    check=true;
                } else {
                    sayNext=false;
                    check=false;
                    speak("instructions complete");
                    speakingEnd = myTTS.isSpeaking();
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                }
            }
        }else if(s.indexOf("where am")!=-1){
            if(newaddress!=null||address!=null){
                if(newaddress!=null)
                    speak(newaddress.getAddressLine(0));
                else if(adressLine!=null) {
                    speak(adressLine);
                } else if(changeLoc) {
                    speak("cannot process current address. your starting address was, "+address.getAddressLine(0));
                } else {
                    speak(address.getAddressLine(0));
                }
            } else {
                speak("no Address set");
            }
        } else if(s.indexOf("help")!=-1 || s.indexOf("Help")!=-1){


            speak("For your current location say, where am I. ");
            boolean speakingEnd =myTTS.isSpeaking();
            do{
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);

            if (Build.VERSION.SDK_INT >= 21) {
                myTTS.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
            } else {
                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
            }
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);


            speak("For next instruction say, then");
            speakingEnd =myTTS.isSpeaking();
            do{
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);

            if (Build.VERSION.SDK_INT >= 21) {
                myTTS.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
            } else {
                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
            }
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            speak("To know which way you are facing, say, which way am i facing");
            speakingEnd =myTTS.isSpeaking();
            do{
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            if (Build.VERSION.SDK_INT >= 21) {
                myTTS.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
            } else {
                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
            }
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            speak("To know which way is north or south or east or west, say which way is, then the direction, like, which way is south");
            speakingEnd =myTTS.isSpeaking();
            do{
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            if (Build.VERSION.SDK_INT >= 21) {
                myTTS.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
            } else {
                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
            }
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);


            speak("to get directions or nearby locations first say all done, then follow instructions");
            speakingEnd =myTTS.isSpeaking();
            do{
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);


            if (Build.VERSION.SDK_INT >= 21) {
                myTTS.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
            } else {
                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
            }
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            if (Build.VERSION.SDK_INT >= 21) {
                myTTS.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
            } else {
                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
            }
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            speak("To call a emergency number say, call emergency then the emergency number. Like call emergency 1. " +
                    "To send text to emergency contacts, say send emergency text to emergency");
            speakingEnd =myTTS.isSpeaking();
            do{
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            if (Build.VERSION.SDK_INT >= 21) {
                myTTS.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
            } else {
                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
            }
            do {
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            speak("To add emergency contacts, say, open emergency window. For this you will need the help of a visually able person. " +
                    "To call someone else say call, To text someone else say, send emergency text");
            speakingEnd =myTTS.isSpeaking();
            do{
                speakingEnd = myTTS.isSpeaking();
            } while (speakingEnd);
            speak("to exit long press and say exit after the beep");
        } else if(s.indexOf("exit")!=-1){
            finish();
            System.exit(0);
        } else if(phoneNumber||smsPhoneNumber){
            phoneString0=s;


            phoneString0 = phoneString0.replace("zero", "0");
            phoneString0 = phoneString0.replace("one", "1");
            phoneString0 = phoneString0.replace("two", "2");
            phoneString0 = phoneString0.replace("three", "3");
            phoneString0 = phoneString0.replace("four", "4");
            phoneString0 = phoneString0.replace("five", "5");
            phoneString0 = phoneString0.replace("six", "6");
            phoneString0 = phoneString0.replace("seven", "7");
            phoneString0 = phoneString0.replace("eight", "8");
            phoneString0 = phoneString0.replace("nine", "9");
            phoneString0 = phoneString0.replace("plus", "+");
            phoneString0 = phoneString0.replace(" ", "");
            phoneString0 = phoneString0.replace(".", "");
            phoneString0 = phoneString0.replace(",", "");
            phoneString0 = phoneString0.replace("!", "");
            String phoneString1= phoneString0.replace("plus", "");
            numeric = true;
            try {
                long num = Long.parseLong(phoneString1);
            } catch (NumberFormatException e) {
                numeric = false;
            }

            if(!numeric){
                speak("sorry! I don't get that. please repeat phone number");
            } else {
                if(phoneNumber) {
                    phoneNumber = false;
                    smsPhoneNumber = false;
                    call(phoneString0);
                } else if(smsPhoneNumber){
                    phoneNumber = false;
                    smsPhoneNumber = false;

                    boolean speakingEnd;
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    speak("Sending");
                    speakingEnd =myTTS.isSpeaking();
                    do{
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                            sendMSG(DirecctionActivity.this,null,phoneString0,"Help I am in an emergency! My battery level is " + batteryLevel + "%! My location is " + emLocation,timestamp.toString());

                        }
                    }, 30000);
                }
            }
        }
        else {
            speak("Sorry! I didn't get that! For detailed instruction long press and say help after the beep");
        }
    }

    private void whichWayIs(String s) {
        boolean speakingEnd;

        if(!(facingWay.equals("NorthWest")||facingWay.equals("SouthWest")||
                facingWay.equals("SouthEast")||facingWay.equals("NorthEast"))){
            if(s.indexOf("south")!=-1){
                speak("South is at your "+south);
            }else if(s.indexOf("north")!=-1){
                speak("North is at your "+north);
            }else if(s.indexOf("west")!=-1){
                speak( "West is at your "+west);
            }else if(s.indexOf("east")!=-1){
                speak( "East is at your "+east);
            }

        } else {
            if(s.indexOf("south")!=-1 && south!=null){
                speak("You Are Facing " + facingWay+" South is a little towards your "+south);
            }else if(s.indexOf("north")!=-1 && north!=null){
                speak("You Are Facing " + facingWay+" North is a little towards your "+north);
            }else if(s.indexOf("west")!=-1 && west!=null){
                speak("You Are Facing " + facingWay+" West is a little towards your "+west);
            }else if(s.indexOf("east")!=-1 && east!=null){
                speak("You Are Facing " + facingWay+" East is a little towards your "+east);
            }else if(s.indexOf("south")!=-1||s.indexOf("north")!=-1||s.indexOf("west")!=-1||s.indexOf("east")!=-1) {

                speak("Sorry! You Are Facing " + facingWay + ". Closer to " + towards + ". Please! at First turn a little towards "
                        + towardsD + ", to face " + towards + ", or, towards " + lessTowardsD + ", to face " + lessTowards);
            } else {
                speak("Sorry! I don't get that");
            }
        }

        speakingEnd = myTTS.isSpeaking();
        do {
            speakingEnd = myTTS.isSpeaking();
        } while (speakingEnd);
    }

    private void speak(String s) {
        if(Build.VERSION.SDK_INT>=21){
            myTTS.speak(s,TextToSpeech.QUEUE_FLUSH,null,null);
        }else {
            myTTS.speak(s,TextToSpeech.QUEUE_FLUSH,null);
        }
    }



    private void nextInstruction() {
        myTTS.speak("for next instructions long press on map an say then after the beep",TextToSpeech.QUEUE_ADD,null);
        boolean speakingEnd = myTTS.isSpeaking();
        do {
            speakingEnd = myTTS.isSpeaking();
        } while (speakingEnd);
        myTTS.speak("for details say 'help'",TextToSpeech.QUEUE_ADD,null);
        index=2;

        sayNext=true;

        check=true;
        DirecctionActivity.this.registerReceiver(DirecctionActivity.this.batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            this.unregisterReceiver(this.batteryReceiver);
        }catch (IllegalArgumentException e){}
    }
    private void call(String phoneString0) {


        String dial = "tel:" + phoneString0;

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(dial));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        PackageManager packageManager = DirecctionActivity.this.getPackageManager();
        List activities = packageManager.queryIntentActivities(callIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (int j = 0; j < activities.size(); j++) {

            if (activities.get(j).toString().toLowerCase().contains("com.android.phone")) {
                callIntent.setPackage("com.android.phone");
            } else if (activities.get(j).toString().toLowerCase().contains("call")) {
                String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);
                callIntent.setPackage(pack);
            }
        }
        if (ContextCompat.checkSelfPermission(DirecctionActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            speak("Please! grant call permission to call a number from your settings");
        } else {
            startActivity(callIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direcction);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        shref = this.getSharedPreferences("Emergency", Context.MODE_PRIVATE);
        String emergency = "emergency";
        Gson gson = new Gson();
        String response=shref.getString(emergency , "");
        if(!response.equals("")) {
            emergencyArrayList = gson.fromJson(response,
                    new TypeToken<List<Contacts>>() {
                    }.getType());
        }

        durTV=findViewById(R.id.DurTV);
        desTV=findViewById(R.id.desTV);
        fromET=findViewById(R.id.formET);
        toET=findViewById(R.id.toET);
        bottomNavigationView=findViewById(R.id.navigation);
        nextRoute=findViewById(R.id.nextRoute);
        nextRoute.setEnabled(false);

        mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        starSensorManager();


        //Create retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(DirectionService.class);


        //Map fragment
        options = new GoogleMapOptions();
        options.zoomControlsEnabled(true).compassEnabled(true).mapType(GoogleMap.MAP_TYPE_TERRAIN);
        mapFragment = SupportMapFragment.newInstance(options);
        fragmentManager=getSupportFragmentManager();
        ft = fragmentManager.beginTransaction().replace(R.id.mapContainer, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);

        //get last location on create
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, MapActivity.PERMISSION_REQUEST);
            return;
        }
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    lastLocation = task.getResult();
                    lat=lastLocation.getLatitude();
                    lng=lastLocation.getLongitude();
                }
            }
        });
        //get itent
        intent=getIntent();
        mapCheck=intent.getBooleanExtra("map",false);
        start=intent.getBooleanExtra("start",false);
        speak=intent.getBooleanExtra("speak",false);
        lowSaid=intent.getBooleanExtra("lowSaid",false);
        lowSaid2=intent.getBooleanExtra("lowSaid2",false);
        batteryLevel=intent.getIntExtra("batteryLevel",0);
        if(speak){
            mode="walking";
            bottomNavigationView.setSelectedItemId(R.id.navigation_walk);
        }
    }


    private void navItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.navigation_Bus:
            {
                mode="driving";
                if(currentFragment!=mapFragment){

                    ft=fragmentManager.beginTransaction().replace(R.id.mapContainer,mapFragment);
                    ft.commit();
                    mapFragment.getMapAsync(this);
                }

                fromET.setEnabled(true);
                toET.setEnabled(true);
                nextRoute.setEnabled(true);
                nextRoute.setVisibility(View.VISIBLE);
                map.clear();
                route=0;
                getDirections();
            }break;
            case R.id.navigation_walk:
            {
                mode="walking";
                if(currentFragment!=mapFragment){

                    ft=fragmentManager.beginTransaction().replace(R.id.mapContainer,mapFragment);
                    ft.commit();
                    mapFragment.getMapAsync(this);
                }


                fromET.setEnabled(true);
                toET.setEnabled(true);
                nextRoute.setEnabled(true);
                nextRoute.setVisibility(View.VISIBLE);
                map.clear();
                route=0;
                getDirections();
            }break;
            case R.id.navigation_Instruction:
            {
                instructionFragment=InstructionFragment.newInstance(instructions);
                ft=fragmentManager.beginTransaction().replace(R.id.mapContainer,instructionFragment);
                ft.commit();
                fromET.setEnabled(false);
                toET.setEnabled(false);
                nextRoute.setEnabled(false);
                nextRoute.setVisibility(View.INVISIBLE);
            }break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        //cluster manager
        clusterManager = new ClusterManager<MyItem>(DirecctionActivity.this, googleMap);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.clearItems();

        //set my location enabled
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, MapActivity.PERMISSION_REQUEST);
            return;
        }
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()), 15));
                return false;
            }
        });


        //last location on map ready
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    lastLocation = task.getResult();
                    if(!isMapCheck) {
                        lat = lastLocation.getLatitude();
                        lng = lastLocation.getLongitude();
                        latLng = new LatLng(lat, lng);
                    }
                    //add marker on origin
                    address = getAddress(lat, lng);



                }
            }
        });

        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                if(check&&distance(location.getLatitude(),location.getLongitude(),instructions.get(index-1).getEndLat(),
                        instructions.get(index-1).getEndLng())<=1.6){

                    speak("You are very near the end of instruction "+(index-1));
                    check=false;

                }
                if(timer!=60){
                    timer++;
                }
                if(timer==60&&(lat!=location.getLatitude()||lng!=location.getLongitude())) {
                    changeLoc=true;
                    lastLocation = location;
                    newlat = location.getLatitude();
                    newlng = location.getLongitude();
                    newlatLng = new LatLng(newlat, newlng);
                    newaddress = getAddress(newlat, newlng);
                    if(newaddress!=null)
                        adressLine=newaddress.getAddressLine(0);
                    if(adressLine!=null)
                        Toast.makeText(DirecctionActivity.this,adressLine,Toast.LENGTH_SHORT).show();
                    timer=0;


                } else if(timer==60){
                    timer=0;
                }

            }
            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                // TODO Auto-generated method stub
            }
        });




        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                currentFragment = fragmentManager.findFragmentById(R.id.mapContainer);
                navItemSelected(item);
                return true;
            }
        });
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                currentFragment = fragmentManager.findFragmentById(R.id.mapContainer);
                navItemSelected(item);
            }
        });

        //value from intent
        deslat=intent.getDoubleExtra("desLat",0);
        deslng=intent.getDoubleExtra("desLng",0);
        if(mapCheck&&!isMapCheck){
            //string origin
            origin="" + lat + "," + lng;
            originAddress=getAddress(lat,lng);
            desAddress=getAddress(deslat,deslng);
            destination="" + deslat + "," + deslng;
            if (originAddress != null && desAddress != null) {
                fromET.setText(originAddress.getLocality() + ", " + originAddress.getAdminArea());
                toET.setText(intent.getStringExtra("name") + ", " + desAddress.getAdminArea());
            } else {
                fromET.setText("Your Location");
                toET.setText(intent.getStringExtra("name"));
            }

        }
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                Intent intent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                mySpeechRecognizer.startListening(intent);
            }
        });
        if(mapCheck){

            map.clear();

            //call getDirection method
            route=0;
            if(speak){
                mode="walking";
            }
            getDirections();
        }

        //Disable Map Toolbar:
        initializeTTS();

        initializeSpeechRecognizer();
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.map = googleMap;


    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist=dist*1000;
        return (dist);
    }



    //get Address
    public Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);
        } catch (Exception e){}
        return null;
    }

    //get Direction
    private void getDirections() {

        String key= getString(R.string.google_direction_api);
        urlString= String.format("json?origin=%s&destination=%s&mode=%s&alternatives=true&key=%s",
                origin,destination,mode,key);
        dLat=lat;
        dLng=lng;
        if(start){
            dLat=intent.getDoubleExtra("startLat",lat);
            dLng=intent.getDoubleExtra("startLng",lng);
            origin="" + dLat + "," + dLng;
            fromET.setText(intent.getStringExtra("startName"));
            urlString= String.format("json?origin=%s&destination=%s&mode=%s&alternatives=true&key=%s",
                    origin,destination,mode,key);
            Log.e("derUrl",BASE_URL+urlString);
        }
        //manage Markers
        if(instructions!=null){
            instructions.clear();
        }
        map.clear();
        clusterItems.clear();
        clusterManager.clearItems();
        clusterItems.add(new MyItem(dLat,dLng));
        clusterItems.add(new MyItem(deslat,deslng));
        clusterManager.addItems(clusterItems);
        clusterManager.cluster();
        builder = new LatLngBounds.Builder();
        //get Response
        Call<DirectionResponse> directionResponseCall = service.getDirections(urlString);
        directionResponseCall.enqueue(new Callback<DirectionResponse>() {

            boolean success=false;
            @Override
            public void onResponse(Call<DirectionResponse> call, Response<DirectionResponse> response) {
                if(response.isSuccessful()){
                    DirectionResponse directionResponse = response.body();
                    totalRoutes = directionResponse.getRoutes().size();

                    if(directionResponse.getRoutes().size()!=0) {
                        List<DirectionResponse.Step> steps = directionResponse.getRoutes().get(route).getLegs().get(0).getSteps();
                        desTV.setText(directionResponse.getRoutes().get(route).getLegs().get(0).getDistance().getText());
                        durTV.setText(directionResponse.getRoutes().get(route).getLegs().get(0).getDuration().getText());
                        if(start){
                            instructions.add(new Instruction(intent.getStringExtra("startName"),dLat,dLng));
                        }else instructions.add(new Instruction(fromET.getText().toString(),dLat,dLng,steps.get(0).getStartLocation().getLat(),steps.get(0).getStartLocation().getLng()));

                        //manage Origin
                        LatLng start1 = new LatLng(dLat, dLng);
                        double endLat1 = steps.get(0).getStartLocation().getLat();
                        double endLng1 = steps.get(0).getStartLocation().getLng();
                        LatLng end1 = new LatLng(endLat1, endLng1);


                        //add polyline from origin

                        Polyline polyline1 = map.addPolyline(new PolylineOptions().add(start1).add(end1));
                        polyline1.setColor(R.color.skyBlue);
                        for (DirectionResponse.Step step : steps) {
                            //get steps start point
                            double startLat = step.getStartLocation().getLat();
                            double startLng = step.getStartLocation().getLng();
                            LatLng start = new LatLng(startLat, startLng);

                            //add points in builder
                            builder.include(start);

                            //get end points
                            double endLat = step.getEndLocation().getLat();
                            double endLng = step.getEndLocation().getLng();
                            LatLng end = new LatLng(endLat, endLng);

                            //add points in builder
                            builder.include(end);


                            //add polylines
                            Polyline polyline = map.addPolyline(new PolylineOptions().add(start).add(end));
                            polyline.setColor(R.color.skyBlue);

                            instructions.add(new Instruction(step.getHtmlInstructions(),startLat,startLng,endLat,endLng));
                        }

                        //manage Des
                        LatLng start2 = new LatLng(deslat, deslng);
                        double endLat2 = steps.get(steps.size() - 1).getEndLocation().getLat();
                        double endLng2 = steps.get(steps.size() - 1).getEndLocation().getLng();
                        LatLng end2 = new LatLng(endLat2, endLng2);
                        //add des polyline
                        Polyline polyline2 = map.addPolyline(new PolylineOptions().add(end2).add(start2));
                        polyline2.setColor(R.color.skyBlue);

                        instructions.add(new Instruction(toET.getText().toString(),deslat,deslng));

                        //include start and end point in builder

                        builder.include(new LatLng(directionResponse.getRoutes().get(route).getBounds().getNortheast().getLat() + .1
                                , directionResponse.getRoutes().get(route).getBounds().getNortheast().getLng() + .1));

                        builder.include(new LatLng(directionResponse.getRoutes().get(route).getBounds().getSouthwest().getLat() - .1
                                , directionResponse.getRoutes().get(route).getBounds().getSouthwest().getLng() - .1));




                    }

                    else {
                        Toast.makeText(DirecctionActivity.this,"No routes",Toast.LENGTH_SHORT).show();
                        desTV.setText("-");
                        durTV.setText("-");
                    }
                    builder.include(new LatLng(dLat-.1, dLng-.1));
                    builder.include(new LatLng(deslat+.1, dLng+.1));
                    builder.include(new LatLng(deslat-.1, deslng-.1));
                    builder.include(new LatLng(deslat+.1, deslng+.1));

                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),30));
                    try {
                        if (directionResponse.getRoutes().get(route).getLegs().get(0).getDistance().getValue() <= 2500) {
                            map.setMinZoomPreference(15);
                        } else map.setMinZoomPreference(0);
                    }catch (Exception e){}
                    Button waypoint=findViewById(R.id.wayPoints);
                    if(totalRoutes<=1){
                        nextRoute.setEnabled(false);
                        nextRoute.setVisibility(View.INVISIBLE);
                        waypoint.setEnabled(false);
                        waypoint.setVisibility(View.INVISIBLE);
                    }
                    else{
                        nextRoute.setEnabled(true);
                        nextRoute.setVisibility(View.VISIBLE);
                        waypoint.setEnabled(true);
                        waypoint.setVisibility(View.VISIBLE);
                    }
                    start=false;
                    success=true;
                }
                else {

                    builder.include(new LatLng(lat-.1, lng-.1));
                    builder.include(new LatLng(lat+.1, lng+.1));
                    builder.include(new LatLng(deslat-.1, deslng-.1));
                    builder.include(new LatLng(deslat+.1, deslng+.1));
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));

                    Toast.makeText(DirecctionActivity.this,response.message(),Toast.LENGTH_SHORT).show();
                    res=response.message();
                    desTV.setText("-");
                    durTV.setText("-");
                    start=false;
                    success=false;
                }

                if(speak){
                    speak=false;
                    handler = new Handler();
                    final boolean finalSuccess = success;

                    if(finalSuccess){

                        myRunnable= new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                speak=false;

                                boolean speakingEnd = myTTS.isSpeaking();

                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                speak(Html.fromHtml(instructions.get(1).getInstruction()).toString());
                                if (Build.VERSION.SDK_INT >= 21) {
                                    myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                                } else {
                                    myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
                                }
                                do {
                                    speakingEnd = myTTS.isSpeaking();
                                } while (speakingEnd);
                                nextInstruction();
                            }
                        };
                        handler.postDelayed(myRunnable, 3000);

                    }else {
                        speak=false;
                        speak(res);
                    }
                }
            }

            @Override
            public void onFailure(Call<DirectionResponse> call, Throwable t) {
                Toast.makeText(DirecctionActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                builder.include(new LatLng(lat-.1, lng-.1));
                builder.include(new LatLng(lat+.1, lng+.1));
                builder.include(new LatLng(deslat-.1, deslng-.1));
                builder.include(new LatLng(deslat+.1, deslng+.1));
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
                desTV.setText("-");
                durTV.setText("-");
                start=false;
                if(speak){
                    speak=false;
                    speak(t.getMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mapCheck){
            Intent intent1=new Intent(DirecctionActivity.this,MapActivity.class);
            intent.putExtra("lowSaid",lowSaid);
            intent.putExtra("lowSaid2",lowSaid2);
            intent.putExtra("batteryLevel",batteryLevel);
            startActivity(intent1);
        }
        super.onBackPressed();
    }

    //auto complete search
    public void setDes(View view) {
        isMapCheck=true;
        int request=0;

        //check editText id
        switch (view.getId()){
            case R.id.toET:
                request=DES_REQUEST;
                break;
            case R.id.formET:
                request=ORIGIN_REQUEST;
                break;
        }


        try {
            //auto complete search
            Address address = getAddress(lat, lng);
            List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID,
                    com.google.android.libraries.places.api.model.Place.Field.NAME,com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                    com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                    .build(DirecctionActivity.this);
            startActivityForResult(intent,request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get auto complete search result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){

            //change origin
            case ORIGIN_REQUEST:
                if (resultCode == RESULT_OK) {
                    place = Autocomplete.getPlaceFromIntent(data);
                    double desLat = place.getLatLng().latitude;
                    double desLng = place.getLatLng().longitude;
                    lat=desLat;
                    lng=desLng;
                    fromET.setText(place.getAddress());
                    originAddress=getAddress(desLat,desLng);
                    origin = "" + desLat + "," + desLng;
                    map.clear();
                    route=0;
                    getDirections();

                }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    // TODO: Handle the error.
                    Log.i("",status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;

            //change des
            case DES_REQUEST:
                if (resultCode == RESULT_OK) {
                    place = Autocomplete.getPlaceFromIntent(data);
                    double desLat = place.getLatLng().latitude;
                    double desLng = place.getLatLng().longitude;
                    deslat=desLat;
                    deslng=desLng;
                    desAddress=getAddress(desLat,desLng);
                    toET.setText(place.getAddress());
                    destination = "" + desLat + "," + desLng;
                    map.clear();
                    route=0;
                    getDirections();

                }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    // TODO: Handle the error.
                    Log.i("",status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case WAY_REQUEST:
                if (resultCode == RESULT_OK) {
                    place = Autocomplete.getPlaceFromIntent(data);

                    if(mode.contains("&waypoints=")){
                        mode=mode+"|"+place.getAddress();
                    }else mode=mode+"&waypoints="+place.getAddress();
                    map.clear();
                    route=0;
                    getDirections();

                }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    // TODO: Handle the error.
                    Log.i("",status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //change route
    public void nextRoute(View view) {
        if(route<totalRoutes-1){
            route++;
        }
        else {
            route=0;
        }
        map.clear();
        getDirections();
    }

    public void addWaypoints(View view) {
        int request=WAY_REQUEST;
        try {
            //auto complete search
            Address address = getAddress(lat, lng);
            List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID,
                    com.google.android.libraries.places.api.model.Place.Field.NAME,com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                    com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                    .build(DirecctionActivity.this);
            startActivityForResult(intent,request);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void starSensorManager() {
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)==null){

            if(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)==null
                    ||
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)==null){


                noSensor=true;
            } else {
                mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                hasSensor=mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_UI);
                hasSensor2=mSensorManager.registerListener(this,mMagnetometer,SensorManager.SENSOR_DELAY_UI);
            }
        } else {
            mRotationV=mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            hasSensor=mSensorManager.registerListener(this,mRotationV,SensorManager.SENSOR_DELAY_UI);

        }
    }

    public void stopSensorManager() {
        if(hasSensor && hasSensor2){
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        } else if(hasSensor){
            mSensorManager.unregisterListener(this,mRotationV);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        starSensorManager();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()== Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(mMat,event.values);
            mAzimuth= (int) ((Math.toDegrees(SensorManager.getOrientation(mMat,orientation)[0])+360)%360);
        }
        if(event.sensor.getType()== Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values,0,mLastAccelerometer,0,event.values.length);
            mLastAccelerometerSet=true;
        }
        if (event.sensor.getType()== Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values,0,mLastMagnetometer,0,event.values.length);
            mLastMagnetometerSet=true;
        }
        if(mLastAccelerometerSet && mLastMagnetometerSet){
            SensorManager.getRotationMatrix(mMat,null,mLastAccelerometer,mLastMagnetometer);
            mAzimuth= (int) ((Math.toDegrees(SensorManager.getOrientation(mMat,orientation)[0])+360)%360);
        }
        mAzimuth=Math.round(mAzimuth);
        if(mAzimuth>=340 || mAzimuth<=20){
            facingWay="North";
            north="front";
            south="back";
            west="left";
            east="right";
        } else if(mAzimuth<340 && mAzimuth>290){
            facingWay="NorthWest";
            north="right";
            south=null;
            west="left";
            east=null;
            if((360-mAzimuth)>=(mAzimuth-270)){
                towards="west";
                towardsD=west;
                lessTowards="north";
                lessTowardsD=north;
            }else {
                towards="north";
                towardsD=north;
                lessTowards="west";
                lessTowardsD=west;
            }
        } else if(mAzimuth<=290 && mAzimuth>250){
            facingWay="West";
            north="right";
            south="left";
            west="front";
            east="back";
        } else if(mAzimuth<=250 && mAzimuth>200){
            facingWay="SouthWest";
            north=null;
            south="left";
            west="right";
            east=null;
            if((270-mAzimuth)>(mAzimuth-180)){
                towards="south";
                towardsD=south;
                lessTowards="west";
                lessTowardsD=west;
            } else {
                towards="west";
                towardsD=west;
                lessTowards="south";
                lessTowardsD=south;
            }
        } else if(mAzimuth<=200 && mAzimuth>160){
            facingWay="South";
            north="back";
            south="front";
            west="right";
            east="left";
        } else if(mAzimuth<=160 && mAzimuth>110){
            facingWay="SouthEast";
            north=null;
            south="right";
            west=null;
            east="left";
            if((180-mAzimuth)>(mAzimuth-90)){
                towards="East";
                towardsD=east;
                lessTowards="South";
                lessTowardsD=south;
            } else {
                towards="South";
                towardsD=south;
                lessTowards="East";
                lessTowardsD=east;
            }
        } else if(mAzimuth<=110 && mAzimuth>70){
            facingWay="East";
            north="left";
            south="right";
            west="back";
            east="front";
        } else if(mAzimuth<=70 && mAzimuth>20){
            facingWay="NorthEast";
            north="left";
            south=null;
            west=null;
            east="right";
            if((90-mAzimuth)>(mAzimuth-0)){
                towards="North";
                towardsD=north;
                lessTowards="East";
                lessTowardsD=east;
            } else {
                towards="East";
                towardsD=east;
                lessTowards="North";
                lessTowardsD=north;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

