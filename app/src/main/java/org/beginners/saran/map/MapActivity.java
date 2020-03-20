package org.beginners.saran.map;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterManager;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesAdapter.ClickListener {
    private static final String TAG = "kk";
    TextToSpeech myTTS;
    SpeechRecognizer mySpeechRecognizer;
    private static final int GEO_FENCE_CODE = 10;
    private static final int GEOFENCE_PENDIND_CODE = 111;
    GoogleMap map;
    Address address;
    GoogleMapOptions options;
    public static final int PERMISSION_REQUEST = 1;
    ArrayList<MyItem> clusterItems = new ArrayList<>();
    ArrayList<MyItem> nearByItems = new ArrayList<>();
    ClusterManager<MyItem> clusterManager;
    private FusedLocationProviderClient client;
    private Location lastLocation;
    double lat, lng;
    LatLng latLng;
    Place place;
    private String origin;
    private String destination;
    private final int AUTOCOMPLTE_REQUEST = 2;
    SearchBox search;
    FloatingActionButton direction;
    //DrawerLayout drawer;
    ImageView Drawer;
    TypeFilter typeFilter;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";
    NearByPlacesService service;
    String urlString;
    double nearByLat;
    double nearByLng;
    String type;
    double desLng;
    double desLat;
    View bottomSheetHeaderColor;
    String key;
    private BottomSheetBehavior mBottomSheetBehavior;
    List<NearByPlacesResponse.Result> places;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    String nextPage;
    int responceCode;
    String nearByString;
    boolean remove;
    boolean dir;
    AlertDialog.Builder alert;
    MyItem item;
    private boolean nearBySpeach;
    private Address address1;
    private PlacesClient placesClient;
    private boolean placeBool=false;
    LocationManager locationManager;
    private boolean numeric;
    private String phoneString0;
    private boolean phoneNumber=false;
    private boolean sim;
    private int batteryLevel;
    boolean lowSaid=false;
    private SharedPreferences shref;
    private SharedPreferences prefs;
    private ArrayList<Contacts> emergencyArrayList;
    private boolean yes=false;

    private boolean smsPhoneNumber=false;
    private String emLocation;

    private int sizeS;
    private int sizeD;
    private boolean notSent=false;
    private boolean notDelivered=false;

    private boolean confirm=false;
    private String desName;

    private double desIntentLat;

    private boolean lowSaid2=false;
    private double desIntentLng;
    private BroadcastReceiver batteryReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            batteryLevel=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            if(batteryLevel<=10&&!lowSaid2 &&lowSaid){
                boolean speakingEnd;

                speak("Your battery is critically low! Battery level "+batteryLevel+"% !");

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

                if(ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {


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


            } else if(batteryLevel>25 && (lowSaid||lowSaid2)){
                lowSaid=false;
                lowSaid2=false;
            }
        }
    };
    private String listSmall="";
    private String longList="";

    public void sendMSG(Context context, @Nullable Contacts contacts,@Nullable String phoneString, String text, String id){



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
        else smsManager.sendMultipartTextMessage(phoneString, null, messages, listOfIntents,listOfDIntents);


    }
    @Override
    protected void onResume() {
        super.onResume();
        shref = this.getSharedPreferences("Emergency", Context.MODE_PRIVATE);
        String emergency = "emergency";
        Gson gson = new Gson();
        String response=shref.getString(emergency , "");
        if(!response.equals("")) {
            emergencyArrayList = gson.fromJson(response,
                    new TypeToken<List<Contacts>>() {
                    }.getType());
        }

        initializeTTS();
    }

    private void initializeTTS() {
        myTTS=new TextToSpeech(MapActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0){
                    Toast.makeText(MapActivity.this,"Sorry! No Engines Available For Voice Facilities",Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    myTTS.setLanguage(Locale.ENGLISH);

                    boolean speakingEnd;
                    if (!prefs.getBoolean("firstTime", false)) {
                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                        speak("hello! how can I help you?" +
                                " To ask me anything long press on map and ask your question after the beep." +
                                " For detailed instruction long press and say help after the beep");


                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);

                        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                            speak("Permission for SMS not granted! please grant permission from phone settings, to send emergency SMS in emergency cases!");
                            speakingEnd = myTTS.isSpeaking();
                            do {
                                speakingEnd = myTTS.isSpeaking();

                            } while (speakingEnd);
                        }
                        if(ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                            speak("Give permission to call, for emergency calls! please! select a default sim for SMS and calls. For direct voice controlled call and text commands, else you will have to choose the sim manually every time. Add emergency contacts for safety from dialog shown on screen now");
                            speakingEnd = myTTS.isSpeaking();
                            do {
                                speakingEnd = myTTS.isSpeaking();
                            } while (speakingEnd);
                        }else {
                            speak("please! select a default sim for SMS and calls. For direct voice controlled call and text commands, else you will have to choose the sim manually every time. Add emergency contacts for safety from dialog shown on screen now");
                            speakingEnd = myTTS.isSpeaking();
                            do {
                                speakingEnd = myTTS.isSpeaking();

                            } while (speakingEnd);
                        }
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("firstTime", true);
                        editor.commit();
                        lowSaid=false;

                        AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
                        alert.setMessage("Add emergency contacts for safety");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(MapActivity.this,EmergencyContactActivity.class);
                                intent.putExtra("lowSaid",lowSaid);
                                intent.putExtra("lowSaid2",lowSaid2);
                                intent.putExtra("map",true);
                                finish();
                                startActivity(intent);
                                dialog.cancel();
                            }
                        });
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alert.show();
                    } else {
                        if(placeBool){
                            placeBool=false;
                        }else{
                            speak("Long press on map and ask your question after the beep. ");

                            do {
                                speakingEnd = myTTS.isSpeaking();
                            } while (speakingEnd);

                            if (Build.VERSION.SDK_INT >= 21) {
                                myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                            } else {
                                myTTS.playSilence(500, TextToSpeech.QUEUE_ADD, null);
                            }
                            do {
                                speakingEnd = myTTS.isSpeaking();
                                lowSaid=false;
                            } while (speakingEnd);

                        }


                    }
                }
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MapActivity.this.registerReceiver(MapActivity.this.batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
        }, 60000);


    }



    private void speak(String s) {
        if(Build.VERSION.SDK_INT>=21){
            myTTS.speak(s,TextToSpeech.QUEUE_FLUSH,null,null);
        }else {
            myTTS.speak(s,TextToSpeech.QUEUE_FLUSH,null);
        }
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
        if(s.indexOf("where am")!=-1){
            if(address!=null){
                speak(address.getAddressLine(0));
            }else {
                speak("Address is not set");
            }
        }
        else  if(s.indexOf("call")!=-1){

            if(s.indexOf("emergency")!=-1){
                if(emergencyArrayList!=null){
                    phoneString0=null;
                    if(s.indexOf("1")!=-1|| s.indexOf("one")!=-1){

                        phoneString0=emergencyArrayList.get(0).getPhoneNumber();

                    }else if(s.indexOf("2")!=-1|| s.indexOf("two")!=-1){
                        if(emergencyArrayList.size()>=2){
                            phoneString0=emergencyArrayList.get(1).getPhoneNumber();

                        }else {
                            speak("Emergency 2 is not set, to set emergency number say open emergency window." +
                                    " For this you will need the help of a visually able person");
                        }
                    } else if(s.indexOf("3")!=-1|| s.indexOf("three")!=-1){
                        if(emergencyArrayList.size()>=3){
                            phoneString0=emergencyArrayList.get(2).getPhoneNumber();
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


        }
        else if(s.indexOf("open emergency window")!=-1){
            speak(" For this you will need the help of a visually able person. Do you want to proceed?");
            yes=true;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    yes=false;
                }
            }, 60000);

        }
        else if(yes && s.indexOf("yes")!=-1){
            yes=false;
            Intent intent=new Intent(MapActivity.this,EmergencyContactActivity.class);
            intent.putExtra("lowSaid",lowSaid);
            intent.putExtra("lowSaid2",lowSaid2);
            intent.putExtra("map",true);
            finish();
            startActivity(intent);
        }
        else if(yes && s.indexOf("no")!=-1){
            yes=false;
        }
        else if(confirm&&(s.indexOf("yes")!=-1)||s.indexOf("confirm")!=-1||s.indexOf("confirmed")!=-1){
            confirm=false;
            Intent intent = new Intent(MapActivity.this, DirecctionActivity.class);
            intent.putExtra("startLat",address.getLatitude());
            intent.putExtra("startLng",address.getLongitude());
            intent.putExtra("startName",address.getLocality());
            intent.putExtra("start",true);
            intent.putExtra("desLat", desIntentLat);
            intent.putExtra("desLng", desIntentLng);
            intent.putExtra("name", desName);
            intent.putExtra("speak", true);
            intent.putExtra("map", true);
            intent.putExtra("lowSaid",lowSaid);
            intent.putExtra("lowSaid2",lowSaid2);
            intent.putExtra("batteryLevel",batteryLevel);
            finish();
            startActivity(intent);
        }
        else if(confirm&&(s.indexOf("no")!=-1)||s.indexOf("deny")!=-1||s.indexOf("denied")!=-1){
            confirm=false;
        }
        else if(s.indexOf("near by") !=-1 || s.indexOf("nearby") !=-1) {



            if(!(s.indexOf("restaurant") != -1 || s.indexOf("restaurants") != -1) && !(s.indexOf("hotels") != -1 || s.indexOf("hotel") != -1) &&
                    !(s.indexOf("gas station") != -1 || s.indexOf("gas stations") != -1) && !(s.indexOf("ATM") != -1 || s.indexOf("ATMs") != -1 || s.indexOf("atm") != -1 || s.indexOf("atms") != -1) &&
                    !(s.indexOf("pharmacy") != -1 || s.indexOf("pharmacies") != -1) && !(s.indexOf("supermarket") != -1 || s.indexOf("supermarkets") != -1)){


                speak("Sorry! I didn't get that.");


            } else{
                if (nearByLat == 181) {
                    nearByLat = lat;
                    nearByLng = lng;
                    if (address != null) {
                        nearByString = address.getAdminArea();
                    }
                }
                if (s.indexOf("restaurant") != -1 || s.indexOf("restaurants") != -1) {
                    speak("searching for nearby restaurants");
                    boolean speakingEnd =myTTS.isSpeaking();
                    do{
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);

                    if (Build.VERSION.SDK_INT >= 21) {
                        myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                    } else {
                        myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
                    }
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    type = "restaurant";


                } else if (s.indexOf("hotels") != -1 || s.indexOf("hotel") != -1) {
                    speak("searching for nearby hotels");
                    boolean speakingEnd =myTTS.isSpeaking();
                    do{
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);

                    if (Build.VERSION.SDK_INT >= 21) {
                        myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                    } else {
                        myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
                    }
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    type = "lodging";


                } else if (s.indexOf("gas station") != -1 || s.indexOf("gas stations") != -1) {
                    speak("searching for nearby gas stations");
                    boolean speakingEnd =myTTS.isSpeaking();
                    do{
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    if (Build.VERSION.SDK_INT >= 21) {
                        myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                    } else {
                        myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
                    }
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    type = "gas_station";


                } else if (s.indexOf("ATM") != -1 || s.indexOf("ATMs") != -1 || s.indexOf("atm") != -1 || s.indexOf("atms") != -1) {
                    speak("searching for nearby ATMs");
                    boolean speakingEnd =myTTS.isSpeaking();
                    do{
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    if (Build.VERSION.SDK_INT >= 21) {
                        myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                    } else {
                        myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
                    }
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    type = "atm";


                } else if (s.indexOf("pharmacy") != -1 || s.indexOf("pharmacies") != -1) {
                    speak("searching for nearby pharmacies");
                    boolean speakingEnd =myTTS.isSpeaking();
                    do{
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    if (Build.VERSION.SDK_INT >= 21) {
                        myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                    } else {
                        myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
                    }
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    type = "pharmacy";


                } else if (s.indexOf("supermarket") != -1 || s.indexOf("supermarkets") != -1) {
                    speak("searching for nearby supermarkets");
                    boolean speakingEnd =myTTS.isSpeaking();
                    do{
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    if (Build.VERSION.SDK_INT >= 21) {
                        myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
                    } else {
                        myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
                    }
                    do {
                        speakingEnd = myTTS.isSpeaking();
                    } while (speakingEnd);
                    type = "supermarket";


                }
                urlString=String.format("json?location=%f,%f&rankby=distance&type=%s&key=%s",nearByLat,nearByLng,type,key);
                nearBySpeach = true;
                getNearByPlaces();
            }


        }
        else if(s.indexOf("instructions for place ") != -1){
            int index=s.indexOf("instructions for place ");
            int placeIndex;
            String x="instructions for place ";
            String placeName=s.substring(index+x.length());
            Toast.makeText(this,placeName,Toast.LENGTH_SHORT).show();
            try {
                placeIndex = Integer.parseInt(placeName)-1;
                boolean error = true;
                if(placeIndex<=places.size()){
                    error=false;
                    Intent intent=new Intent(MapActivity.this,DirecctionActivity.class);
                    intent.putExtra("startLat",nearByLat);
                    intent.putExtra("startLng",nearByLng);
                    intent.putExtra("startName",nearByString);
                    intent.putExtra("start",true);
                    intent.putExtra("desLat",places.get(placeIndex).getGeometry().getLocation().getLat());
                    intent.putExtra("desLng",places.get(placeIndex).getGeometry().getLocation().getLng());
                    intent.putExtra("name",places.get(placeIndex).getName().toString());
                    intent.putExtra("map",true);
                    intent.putExtra("speak",true);
                    intent.putExtra("lowSaid",lowSaid);
                    intent.putExtra("lowSaid2",lowSaid2);
                    intent.putExtra("batteryLevel",batteryLevel);
                    finish();
                    startActivity(intent);
                }
                if(error) {
                    speak("Sorry! I didn't get that!");
                }

            }catch (NumberFormatException e){
                String[]array=new String[60];
                boolean error = true;
                array=getResources().getStringArray(R.array.number);
                for(int i=0; i<places.size();i++){
                    if( array[i].equals(placeName)){
                        placeIndex=i;
                        error=false;
                        Intent intent=new Intent(MapActivity.this,DirecctionActivity.class);
                        intent.putExtra("startLat",nearByLat);
                        intent.putExtra("startLng",nearByLng);
                        intent.putExtra("startName",nearByString);
                        intent.putExtra("start",true);
                        intent.putExtra("desLat",places.get(placeIndex).getGeometry().getLocation().getLat());
                        intent.putExtra("desLng",places.get(placeIndex).getGeometry().getLocation().getLng());
                        intent.putExtra("name",places.get(placeIndex).getName().toString());
                        intent.putExtra("map",true);
                        intent.putExtra("speak",true);
                        intent.putExtra("lowSaid",lowSaid);
                        intent.putExtra("lowSaid2",lowSaid2);
                        intent.putExtra("batteryLevel",batteryLevel);
                        finish();
                        startActivity(intent);
                    }
                }

                if(error) {
                    speak("Sorry! I didn't get that!");
                }
            }
        }
        else if(s.indexOf("send emergency text") != -1){
            if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                speak("You did not give permission for SMS");
            }else {
                if (address == null) {
                    emLocation = "latitude:" + lat + " longitude: " + lng + ".";
                } else {
                    emLocation = address.getAddressLine(0) + ". latitude:" + lat + " longitude: " + lng + ".";

                }
                if(s.indexOf("emergency contact")!=-1) {
                    if (emergencyArrayList != null) {

                        boolean speakingEnd;
                        speak("Sending");

                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                        speakingEnd = myTTS.isSpeaking();
                        for (Contacts contacts : emergencyArrayList) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                    sendMSG(MapActivity.this, contacts, null, "Help I am in an emergency! My battery level is " + batteryLevel + "%! My location is " + emLocation, timestamp.toString());

                                }
                            }, 30000);
                        }
                    } else if (emergencyArrayList == null) {

                        boolean speakingEnd;
                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                        speak("No emergency contact saved , to set emergency number say open emergency window." +
                                " For this you will need the help of a visually able person");

                        speakingEnd = myTTS.isSpeaking();
                        do {
                            speakingEnd = myTTS.isSpeaking();
                        } while (speakingEnd);
                    }
                }
                else {
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
        }
        else if(s.indexOf("direction for ") != -1 ){
            int index=s.indexOf("direction for ");
            String x="direction for ";
            String placeName=s.substring(index+x.length());
            findLatLng(placeName);

        }
        else if(s.indexOf("help")!=-1 || s.indexOf("Help")!=-1){


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


            speak("For directions say direction for then just the desired location only. nothing more than that, like direction for mirpur");
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


            speak("For nearby locations just say nearby, then the type of location like nearby pharmacies, ");
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
            speak("we provide nearby restaurants, gas stations, hotels, ATMs, pharmacies, supermarkets.");
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
            speak("To call a emergency number say, call emergency then the emergency number. Like call emergency 1. " +
                    " To send text to emergency contacts, say send emergency text to emergency");
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
        }
        else if(s.indexOf("more")!=-1){
            morePlaces();
        } else if(s.indexOf("exit")!=-1){
            finish();
            System.exit(0);
        }
        else if(phoneNumber||smsPhoneNumber){
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
                            sendMSG(MapActivity.this,null,phoneString0,"Help I am in an emergency! My battery level is " + batteryLevel + "%! My location is " + emLocation,timestamp.toString());

                        }
                    }, 30000);
                }
            }
        }
        else {
            speak("Sorry! I didn't get that! For detailed instruction long press and say help after the beep");
        }
    }

    private void call(String phoneString0) {


        String dial = "tel:" + phoneString0;

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(dial));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        PackageManager packageManager = MapActivity.this.getPackageManager();
        List activities = packageManager.queryIntentActivities(callIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (int j = 0; j < activities.size(); j++) {

            if (activities.get(j).toString().toLowerCase().contains("com.android.phone")) {
                callIntent.setPackage("com.android.phone");
            } else if (activities.get(j).toString().toLowerCase().contains("call")) {
                String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);
                callIntent.setPackage(pack);
            }
        }
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            speak("Please! grant call permission to call a number from your settings");
        } else {
            startActivity(callIntent);
        }
    }
    public void speakSpeech(String speech) {

        HashMap<String, String> myHash = new HashMap<String, String>();

        myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "done");

        String[] splitspeech = speech.split("   . ");

        for (int i = 0; i < splitspeech.length; i++) {

            if (i == 0) { // Use for the first splited text to flush on audio stream

                myTTS.speak(splitspeech[i].toString().trim(),TextToSpeech.QUEUE_FLUSH, myHash);

            } else { // add the new test on previous then play the TTS

                myTTS.speak(splitspeech[i].toString().trim(), TextToSpeech.QUEUE_ADD,myHash);
            }

            myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
        }
    }
    private void morePlaces() {
        boolean speakingEnd;
        speakingEnd = myTTS.isSpeaking();

        speakSpeech(longList);
        do {
            speakingEnd = myTTS.isSpeaking();
        } while (speakingEnd);
        if (Build.VERSION.SDK_INT >= 21) {
            myTTS.playSilentUtterance(2000, TextToSpeech.QUEUE_ADD, null);
        } else {
            myTTS.playSilence(2000, TextToSpeech.QUEUE_ADD, null);
        }
        do {
            speakingEnd = myTTS.isSpeaking();
        } while (speakingEnd);

        longList="";
    }

    private void findLatLng(String placeName) {
        Geocoder geocoder=new Geocoder(MapActivity.this,Locale.getDefault());
        List<Address> addresses;
        Address address;
        try {
            addresses = geocoder.getFromLocationName(placeName, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if(addresses!=null) {
                address = addresses.get(0);

                speak("Do you want direction for "+address.getAddressLine(0)+", "+placeName+" . If yes, say confirm, If not say deny");

                confirm=true;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        confirm=false;
                    }
                }, 15000);
                desName=placeName;
                desIntentLat=address.getLatitude();
                desIntentLng=address.getLongitude();
            } else {
                speak("Sorry! Location was not pronounced correctly");
            }
        } catch (Exception e){
            speak(e.getLocalizedMessage());
        }
    }


    ///////////////////////////////////////// Have to work//////////////////////////////////Have to work here///////////////


    private void sayNearByLocations() {
        nearBySpeach = false;
        boolean speakingEnd;
        speakingEnd = myTTS.isSpeaking();
        boolean done=false;
        speak(places.size()+" places Found!"+" Nearby Results Are");
        do {
            speakingEnd = myTTS.isSpeaking();
        } while (speakingEnd);
        if (Build.VERSION.SDK_INT >= 21) {
            myTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
        } else {
            myTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, null);
        }
        do {
            speakingEnd = myTTS.isSpeaking();
        } while (speakingEnd);

        if(places.size()>5)
            speakSpeech(listSmall+" To know the name of other places say, More! To get direction for any of these places, say, " +
                    "instructions for place, and than say place number. Like instructions for place 1");
        else {
            speakSpeech(listSmall+" To get direction for any of these places, say, " +
                    "instructions for place, and than say place number. Like instructions for place 1");
        }
        listSmall="";
        do {
            speakingEnd = myTTS.isSpeaking();
        } while (speakingEnd);




    }

    private void sayMore() {
        boolean speakingEnd;
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTTS.shutdown();
        try {
            this.unregisterReceiver(this.batteryReceiver);
        }catch (IllegalArgumentException e){}
    }
















    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = PreferenceManager.getDefaultSharedPreferences(MapActivity.this);
        places = new ArrayList<>();
        //drawer = findViewById(R.id.drawer);
        direction = findViewById(R.id.direction);
        direction.hide();
        Places.initialize(this,getString(R.string.google_near_by_places_api));
        placesClient = Places.createClient(this);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetHeaderColor = findViewById(R.id.color);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        nearByLat = 181;
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedpreferences.edit();
        editor.commit();
        //Create retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(NearByPlacesService.class);
        key = getString(R.string.google_near_by_places_api);


        //navigation drawer
        /*Drawer = findViewById(R.id.drawer_logo);
        Drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });*/


        search = findViewById(R.id.searchbox);
        search.setLogoText("Search Places");
        search.setLogoTextColor(R.color.colorSearchText);
        //search view listener
        search.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                search.toggleSearch();
                try {
                    address = getAddress(lat, lng);

                    List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID,
                            com.google.android.libraries.places.api.model.Place.Field.NAME,com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                            com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                            .build(MapActivity.this);
                    placeBool=true;
                    startActivityForResult(intent, AUTOCOMPLTE_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchClosed() {
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );
            }

            @Override
            public void onSearchTermChanged(String s) {

            }

            @Override
            public void onSearch(String s) {
                /*Geocoder geocoder=new Geocoder(MapActivity.this,Locale.getDefault());
                List<Address> addresses;

                try {
                    addresses = geocoder.getFromLocationName(s, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    if(addresses!=null) {
                        address1 = addresses.get(0);
                        desLat = address1.getLatitude();
                        desLng = address1.getLongitude();
                        destination = "" + desLat + "," + desLng;
                        nearByLng=desLng;
                        nearByLat=desLat;
                        nearByString=address1.getAddressLine(0);
                        origin = "" + lastLocation.getLatitude() + "," + lastLocation.getLongitude();
                        map.clear();
                        clusterItems.clear();
                        clusterManager.clearItems();
                        if(address!=null) {
                            clusterItems.add(new MyItem(lat, lng, address.getLocality(), address.getAdminArea()));
                        }
                        else clusterItems.add(new MyItem(lat,lng));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(desLat, desLng), 12));
                        clusterItems.add(new MyItem(desLat, desLng, address1.getAddressLine(0), s));
                        clusterManager.addItems(clusterItems);
                        clusterManager.cluster();
                        dir=true;
                        direction.setVisibility(View.VISIBLE);
                    } else {
                        speak("Sorry! Location was not pronounced correctly");
                    }
                } catch (Exception e){
                    speak(e.getLocalizedMessage());
                }*/
            }

            @Override
            public void onResultClick(SearchResult searchResult) {

            }
        });

        //map fragment
        options = new GoogleMapOptions();
        options.zoomControlsEnabled(true).compassEnabled(true).mapType(GoogleMap.MAP_TYPE_TERRAIN);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(this);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_DRAGGING
                        || mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetHeaderColor.setBackgroundColor(getResources().getColor(R.color.darkSky));
                } else {
                    bottomSheetHeaderColor.setBackgroundColor(getResources().getColor(R.color.bluish_gray));
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        Intent intent=getIntent();
        lowSaid=intent.getBooleanExtra("lowSaid",false);
        lowSaid2=intent.getBooleanExtra("lowSaid2",false);

    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.map = googleMap;

        clusterManager = new ClusterManager<MyItem>(MapActivity.this, googleMap);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.clearItems();

        //set my location enabled
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
            return;
        }

        //get last location
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    lastLocation = task.getResult();
                    lat = lastLocation.getLatitude();
                    lng = lastLocation.getLongitude();
                    latLng = new LatLng(lat, lng);

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    address = getAddress(lat, lng);
                    if (address != null) {
                        clusterItems.add(new MyItem(lat, lng, address.getLocality(), address.getAddressLine(0)));
                    } else clusterItems.add(new MyItem(lat, lng));
                    clusterManager.addItems(clusterItems);
                }
            }
        });
        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 5, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                if(lat!=location.getLatitude() || lng !=location.getLongitude()) {
                    if (address != null) {
                        clusterItems.remove(new MyItem(lat, lng, address.getLocality(), address.getAddressLine(0)));
                    } else clusterItems.remove(new MyItem(lat, lng));
                    clusterItems.clear();
                    clusterManager.clearItems();
                    clusterManager.cluster();
                    lastLocation = location;
                    lat = lastLocation.getLatitude();
                    lng = lastLocation.getLongitude();
                    latLng = new LatLng(lat, lng);

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    address = getAddress(lat, lng);
                    if (address != null) {
                        clusterItems.add(new MyItem(lat, lng, address.getLocality(), address.getAddressLine(0)));
                    } else clusterItems.add(new MyItem(lat, lng));
                    clusterManager.addItems(clusterItems);
                    clusterManager.cluster();
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
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                nearByLat = lat;
                nearByLng = lng;
                if (address != null) {
                    nearByString = address.getLocality();
                }
                dir = false;
                direction.hide();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 12));
                return false;
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                if (nearByItems.contains(myItem)) {
                    item = myItem;
                    getPhotos(myItem.getPlaceId(), null);

                }
                return false;
            }
        });

        //Disable Map Toolbar:
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Address geoAddress=getAddress(latLng.latitude,latLng.longitude);
                String address="Selected area";
                if(geoAddress!=null){
                    address=geoAddress.getAddressLine(0);
                }
                Intent intent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,50000);
                mySpeechRecognizer.startListening(intent);
            }
        });
        initializeSpeechRecognizer();
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.map = googleMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean speakingEnd;
        if(requestCode==PERMISSION_REQUEST&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            onMapReady(map);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                //When permission is not granted by user, show them message why this permission is needed.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();


                    //Give user option to still opt-in the permissions
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            9996);
                    return;

                } else {
                    // Show user dialog to grant permission to record audio
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            9996);
                    return;
                }
            }

        }else if(requestCode==9996&&grantResults[0]==PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(MapActivity.this,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED
                    ||ContextCompat.checkSelfPermission(MapActivity.this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MapActivity.this,new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS},3786);
            }


        }else if(requestCode==3786&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("phoneDenied", false);
            editor.commit();

            onMapReady(map);
        }else if(requestCode==3786&&grantResults[0]!=PackageManager.PERMISSION_GRANTED&&requestCode==9996&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("phoneDenied", true);
            editor.commit();
            onMapReady(map);
            Toast.makeText(MapActivity.this,"Call permission denied! You won't be able to call anyone through the app!",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    public void getNearByPlaces(){
        Call<NearByPlacesResponse> nearByPlacesResponseCall;
        nearByPlacesResponseCall=service.getNearByPlaces(urlString);
        nearByPlacesResponseCall.enqueue(new Callback<NearByPlacesResponse>() {

            @Override
            public void onResponse(Call<NearByPlacesResponse> call, Response<NearByPlacesResponse> response) {
                editor.putInt("code",response.code());
                editor.apply();
                editor.commit();
                if(nearBySpeach){
                    places.clear();
                }
                if(response.code()==200){
                    NearByPlacesResponse placesResponse;
                    placesResponse=response.body();
                    editor.putString("token",placesResponse.getNextPageToken());
                    editor.apply();
                    editor.commit();
                    places.addAll(placesResponse.getResults());
                    try {
                        if(places.size()!=0)
                            Toast.makeText(MapActivity.this,places.size()+" places found!",Toast.LENGTH_SHORT).show();
                    }catch (NullPointerException e){}

                    Log.e("Url",BASE_URL+urlString);
                    if(nearBySpeach){
                        listSmall="";
                        longList="";
                        if(places.size()>=5){
                            for(int i=0;i<5;i++){

                                NearByPlacesResponse.Result place = places.get(i);
                                listSmall+=i+1+". "+place.getName() +"   . ";
                            }
                            for(int i=5;i<places.size();i++){

                                NearByPlacesResponse.Result place = places.get(i);
                                longList+=i+1+". "+place.getName() +"   . ";
                            }
                        } else {

                            for(int i=5;i<places.size();i++){

                                NearByPlacesResponse.Result place = places.get(i);
                                listSmall+=i+1+". "+place.getName() +"   . ";
                            }
                        }
                        sayNearByLocations();
                    }else {
                        getNextPage();
                    }
                }else {
                    Toast.makeText(MapActivity.this,response.message(),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<NearByPlacesResponse> call, Throwable t) {
                Toast.makeText(MapActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                editor.putInt("code",0);
                editor.apply();
                editor.commit();
                if(nearBySpeach){
                    speak(t.getMessage());
                }
            }
        });

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


    //get searched place
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLTE_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                desLat = place.getLatLng().latitude;
                desLng = place.getLatLng().longitude;
                destination = "" + desLat + "," + desLng;
                nearByLng=desLng;
                nearByLat=desLat;
                nearByString=place.getAddress().toString();
                origin = "" + lastLocation.getLatitude() + "," + lastLocation.getLongitude();
                map.clear();
                clusterItems.clear();
                clusterManager.clearItems();
                if(address!=null) {
                    clusterItems.add(new MyItem(lat, lng, address.getLocality(), address.getAdminArea()));
                }
                else clusterItems.add(new MyItem(lat,lng));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(desLat, desLng), 12));
                clusterItems.add(new MyItem(desLat, desLng, place.getName().toString(), place.getAddress().toString()));
                clusterManager.addItems(clusterItems);
                clusterManager.cluster();
                dir=true;
                direction.show();
            }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                // TODO: Handle the error.
                Log.i("",status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //go to direction activity and get direction
    public void direction(View view) {
        if(!remove&&dir) {
            Intent intent = new Intent(MapActivity.this, DirecctionActivity.class);
            intent.putExtra("desLat", place.getLatLng().latitude);
            intent.putExtra("desLng", place.getLatLng().longitude);
            intent.putExtra("name", place.getName().toString());
            intent.putExtra("map", true);
            intent.putExtra("lowSaid",lowSaid);
            intent.putExtra("lowSaid2",lowSaid2);
            intent.putExtra("batteryLevel",batteryLevel);
            finish();
            startActivity(intent);
        }
        else if(remove){
            clusterManager.clearItems();
            nearByItems.clear();
            clusterManager.addItems(clusterItems);
            clusterManager.cluster();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nearByLat,nearByLng),12));
            remove=false;

            direction.setImageResource(R.drawable.ic_directions_black_24dp);
            direction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            if(!dir){
                direction.hide();
            }else direction.show();
        }
    }
    public void nearBy(View view) {
        clusterManager.clearItems();
        nearByItems.clear();
        clusterManager.addItems(clusterItems);
        clusterManager.cluster();
        if(nearByLat==181){
            nearByLat=lat;
            nearByLng=lng;
            if(address!=null) {
                nearByString = address.getAdminArea();
            }
        }
        places.clear();
        switch (view.getId()){
            case R.id.rest_ibtn:
                Toast.makeText(MapActivity.this,"Restaurants",Toast.LENGTH_SHORT).show();
                type="restaurant";
                break;
            case R.id.cafe_ibtn:
                Toast.makeText(MapActivity.this,"Hotels",Toast.LENGTH_SHORT).show();
                type="lodging";
                break;
            case R.id.gas_ibtn:
                Toast.makeText(MapActivity.this,"Gas Stations",Toast.LENGTH_SHORT).show();
                type="gas_station";
                break;
            case R.id.atm_ibtn:
                Toast.makeText(MapActivity.this,"ATMs",Toast.LENGTH_SHORT).show();
                type="atm";
                break;
            case R.id.pharma_ibtn:
                Toast.makeText(MapActivity.this,"Pharmacies",Toast.LENGTH_SHORT).show();
                type="pharmacy";
                break;
            case R.id.groc_ibtn:
                Toast.makeText(MapActivity.this,"Super Markets",Toast.LENGTH_SHORT).show();
                type="supermarket";
                break;

        }
        urlString=String.format("json?location=%f,%f&rankby=distance&type=%s&key=%s",nearByLat,nearByLng,type,key);
        getNearByPlaces();

    }

    public void getNextPage() {
        responceCode = sharedpreferences.getInt("code", 0);
        if (responceCode == 200) {
            nextPage = sharedpreferences.getString("token", null);
            if (nextPage != null) {
                urlString = String.format("json?pagetoken=%s&key=%s", nextPage, key);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getNearByPlaces();
            }else {
                showPlaces();
            }
        }
    }
    public void showPlaces(){
        if(places.size()!=0) {
            final Dialog dialog = new Dialog(MapActivity.this);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.places, null);
            dialog.setContentView(view);
            TextView title = view.findViewById(R.id.typeTV);
            title.setText("Nearby " + type + " list");
            RecyclerView recyclerView = dialog.findViewById(R.id.placeRV);
            PlacesAdapter adapter = new PlacesAdapter(MapActivity.this, places, this);
            RecyclerView.LayoutManager manager = new LinearLayoutManager(MapActivity.this, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(adapter);
            adapter.Update(places);
            TextView cancel=dialog.findViewById(R.id.cancel);
            TextView showAll=dialog.findViewById(R.id.showAll);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Toast.makeText(MapActivity.this,"No places selected",Toast.LENGTH_SHORT).show();
                }
            });
            showAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nearByItems.clear();
                    LatLngBounds.Builder builder=new LatLngBounds.Builder();
                    for (NearByPlacesResponse.Result place:places){
                        builder.include(new LatLng(place.getGeometry().getLocation().getLat(),
                                place.getGeometry().getLocation().getLng()));
                        MyItem item=new MyItem(place.getGeometry().getLocation().getLat(),
                                place.getGeometry().getLocation().getLng(),place.getName());
                        item.setPlaceId(place.getPlaceId());
                        nearByItems.add(item);

                    }
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),30));
                    clusterManager.addItems(nearByItems);
                    clusterManager.cluster();
                    direction.setImageResource(R.drawable.ic_remove_black_24dp);
                    direction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    direction.show();
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    remove=true;
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else {
            Toast.makeText(MapActivity.this,"No places found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(NearByPlacesResponse.Result place) {
        getPhotos(place.getPlaceId(),place);
    }
    private void getPhotos(String placeId, final NearByPlacesResponse.Result place) {
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place1 = response.getPlace();

            // Get the photo metadata.
            try {

                PhotoMetadata photoMetadata = place1.getPhotoMetadatas().get(0);

                // Get the attribution text.
                String attributions = photoMetadata.getAttributions();

                // Create a FetchPhotoRequest.
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(300) // Optional.
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    getImage(bitmap,place);
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            // Handle error with given status code.
                            getImage(null,place);
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                        }
                    }
                });
            }catch (NullPointerException e){
                getImage(null,place);
            }

        });
    }

    private void getImage(Bitmap bitmap, final NearByPlacesResponse.Result plac) {
        alert=new AlertDialog.Builder(MapActivity.this);
        if(bitmap!=null) {
            final ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            alert.setView(imageView);
        }
        if(plac==null) {
            alert.setTitle(item.getTitle());
        }else alert.setTitle(plac.getName());
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel",null);
        alert.setPositiveButton("Show Direction", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(plac==null) {
                    Intent intent = new Intent(MapActivity.this, DirecctionActivity.class);
                    intent.putExtra("startLat", nearByLat);
                    intent.putExtra("startLng", nearByLng);
                    intent.putExtra("startName", nearByString);
                    intent.putExtra("start", true);
                    intent.putExtra("desLat", item.getPosition().latitude);
                    intent.putExtra("desLng", item.getPosition().longitude);
                    intent.putExtra("name", item.getTitle());
                    intent.putExtra("map", true);
                    intent.putExtra("lowSaid",lowSaid);
                    intent.putExtra("lowSaid2",lowSaid2);
                    intent.putExtra("batteryLevel",batteryLevel);
                    finish();
                    startActivity(intent);
                    Toast.makeText(MapActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                }else {

                    Intent intent=new Intent(MapActivity.this,DirecctionActivity.class);
                    intent.putExtra("startLat",nearByLat);
                    intent.putExtra("startLng",nearByLng);
                    intent.putExtra("startName",nearByString);
                    intent.putExtra("start",true);
                    intent.putExtra("desLat",plac.getGeometry().getLocation().getLat());
                    intent.putExtra("desLng",plac.getGeometry().getLocation().getLng());
                    intent.putExtra("name",plac.getName().toString());
                    intent.putExtra("map",true);
                    intent.putExtra("lowSaid",lowSaid);
                    intent.putExtra("lowSaid2",lowSaid2);
                    intent.putExtra("batteryLevel",batteryLevel);
                    finish();
                    startActivity(intent);
                    Toast.makeText(MapActivity.this,plac.getName(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.unregisterReceiver(this.batteryReceiver);

        }catch (IllegalArgumentException e){}

    }
}