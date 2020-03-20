package org.beginners.saran.map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactActivity extends AppCompatActivity {
    EditText emergencyPhoneET;
    TextView emergency01TV;
    TextView emergency02TV;
    TextView emergency03TV;
    Button addEmergencyBT;
    Button changeEmergency01BT;
    Button changeEmergency02BT;
    Button changeEmergency03BT;
    private String emergency;
    private String guardian;
    private ArrayList<Contacts> emergencyArrayList;
    SharedPreferences shref;
    SharedPreferences.Editor editor;
    private boolean lowSaid;
    private boolean map;
    private Intent intent;
    private boolean lowSaid2=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);
        intent=getIntent();
        lowSaid=intent.getBooleanExtra("lowSaid",false);
        lowSaid2=intent.getBooleanExtra("lowSaid2",false);
        map=intent.getBooleanExtra("map",false);
        emergencyPhoneET=findViewById(R.id.phoneET);
        emergency01TV=findViewById(R.id.contact01TV);
        emergency02TV=findViewById(R.id.contact02TV);
        emergency03TV=findViewById(R.id.contact03TV);
        addEmergencyBT=findViewById(R.id.addPhoneBT);
        changeEmergency01BT=findViewById(R.id.change01BT);
        changeEmergency02BT=findViewById(R.id.change02BT);
        changeEmergency03BT=findViewById(R.id.change03BT);

        shref = this.getSharedPreferences("Emergency", Context.MODE_PRIVATE);
        emergency = "emergency";
        guardian = "guardian";
        Gson gson = new Gson();
        String response=shref.getString(emergency , "");
        if(!response.equals("")) {
            emergencyArrayList = gson.fromJson(response,
                    new TypeToken<List<Contacts>>() {
                    }.getType());
            if (emergencyArrayList != null || emergencyArrayList.size() > 0) {
                if (emergencyArrayList.size() == 3) {
                    emergencyPhoneET.setVisibility(View.GONE);
                    addEmergencyBT.setVisibility(View.GONE);
                    emergency03TV.setText(emergencyArrayList.get(2).getName() + " " + emergencyArrayList.get(2).getPhoneNumber());
                    emergency02TV.setText(emergencyArrayList.get(1).getName() + " " + emergencyArrayList.get(1).getPhoneNumber());
                    emergency01TV.setText(emergencyArrayList.get(0).getName() + " " + emergencyArrayList.get(0).getPhoneNumber());
                    emergency03TV.setVisibility(View.VISIBLE);
                    changeEmergency03BT.setVisibility(View.VISIBLE);
                    emergency02TV.setVisibility(View.VISIBLE);
                    changeEmergency02BT.setVisibility(View.VISIBLE);
                    emergency01TV.setVisibility(View.VISIBLE);
                    changeEmergency01BT.setVisibility(View.VISIBLE);
                } else if (emergencyArrayList.size() == 2) {
                    emergency02TV.setText(emergencyArrayList.get(1).getName() + " " + emergencyArrayList.get(1).getPhoneNumber());
                    emergency01TV.setText(emergencyArrayList.get(0).getName() + " " + emergencyArrayList.get(0).getPhoneNumber());
                    emergency02TV.setVisibility(View.VISIBLE);
                    changeEmergency02BT.setVisibility(View.VISIBLE);
                    emergency01TV.setVisibility(View.VISIBLE);
                    changeEmergency01BT.setVisibility(View.VISIBLE);
                } else {
                    emergency01TV.setText(emergencyArrayList.get(0).getName() + " " + emergencyArrayList.get(0).getPhoneNumber());
                    emergency01TV.setVisibility(View.VISIBLE);
                    changeEmergency01BT.setVisibility(View.VISIBLE);

                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent1;
        if(map) {
            intent1 = new Intent(EmergencyContactActivity.this, MapActivity.class);
        } else {
            intent1 = new Intent(EmergencyContactActivity.this, DirecctionActivity.class);
            intent1.putExtra("desLat", intent.getDoubleExtra("desLat",0));
            intent1.putExtra("desLng", intent.getDoubleExtra("desLng",0));
            intent1.putExtra("name", intent.getStringExtra("name"));
            intent1.putExtra("speak", true);
            intent1.putExtra("map", true);
            if(intent.getBooleanExtra("start",false)){
                intent1.putExtra("startLat",intent.getDoubleExtra("startLat",0));
                intent1.putExtra("startLng",intent.getDoubleExtra("startLng",0));
                intent1.putExtra("startName",intent.getStringArrayExtra("startName"));
                intent1.putExtra("start",true);
            }
        }
        intent1.putExtra("lowSaid",lowSaid);
        intent1.putExtra("lowSaid2",lowSaid2);
        finish();
        startActivity(intent1);
    }

    public void addPhone(View view) {
        String phone=emergencyPhoneET.getText().toString();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(EmergencyContactActivity.this,"Type Phone Number!",Toast.LENGTH_LONG).show();
        } else {

            if(emergencyArrayList==null){
                emergencyArrayList=new ArrayList<>();
            }
                Contacts contacts=new Contacts("emergency0"+(emergencyArrayList.size()+1),phone);
                emergencyArrayList.add(contacts);
                Gson gson = new Gson();
                String json = gson.toJson(emergencyArrayList);
                editor=shref.edit();
                editor.remove(emergency).commit();
                editor.putString(emergency,json);
                editor.commit();
                if(emergencyArrayList.size()==1){
                    emergency01TV.setText("emergency01 "+phone);
                    emergency01TV.setVisibility(View.VISIBLE);
                    changeEmergency01BT.setVisibility(View.VISIBLE);
                } else if(emergencyArrayList.size()==2){
                    emergency02TV.setText("emergency02 "+phone);
                    emergency02TV.setVisibility(View.VISIBLE);
                    changeEmergency02BT.setVisibility(View.VISIBLE);
                } else if(emergencyArrayList.size()==3){
                    emergency03TV.setText("emergency03 "+phone);
                    emergencyPhoneET.setVisibility(View.GONE);
                    addEmergencyBT.setVisibility(View.GONE);
                    emergency03TV.setVisibility(View.VISIBLE);
                    changeEmergency03BT.setVisibility(View.VISIBLE);
                }

            emergencyPhoneET.setText("");
        }
    }

    public void changeNumber(View view) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(EmergencyContactActivity.this);
        edittext.setInputType(InputType.TYPE_CLASS_PHONE);
        edittext.setMaxLines(1);
        alert.setMessage("Enter Phone Number");
        alert.setView(edittext);

        alert.setNegativeButton("Cancel" , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                dialog.cancel();
            }
        });
        switch (view.getId()){
            case R.id.change01BT:
                alert.setTitle(emergencyArrayList.get(0).getName());
                alert.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String phone = edittext.getText().toString();
                        emergencyArrayList.get(0).setPhoneNumber(phone);
                        Gson gson = new Gson();
                        String json = gson.toJson(emergencyArrayList);
                        editor=shref.edit();
                        editor.remove(emergency).commit();
                        editor.putString(emergency,json);
                        editor.commit();
                        emergency01TV.setText(emergencyArrayList.get(0).getName()+" "+emergencyArrayList.get(0).getPhoneNumber());
                        dialog.cancel();


                    }
                });
                alert.show();
                break;
            case R.id.change02BT:
                alert.setTitle(emergencyArrayList.get(1).getName());
                alert.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String phone = edittext.getText().toString();
                        emergencyArrayList.get(1).setPhoneNumber(phone);
                        Gson gson = new Gson();
                        String json = gson.toJson(emergencyArrayList);
                        editor=shref.edit();
                        editor.remove(emergency).commit();
                        editor.putString(emergency,json);
                        editor.commit();
                        emergency02TV.setText(emergencyArrayList.get(1).getName()+" "+emergencyArrayList.get(1).getPhoneNumber());
                        dialog.cancel();

                    }
                });
                alert.show();
                break;
            case R.id.change03BT:
                alert.setTitle(emergencyArrayList.get(2).getName());
                alert.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String phone = edittext.getText().toString();
                        emergencyArrayList.get(2).setPhoneNumber(phone);
                        Gson gson = new Gson();
                        String json = gson.toJson(emergencyArrayList);
                        editor=shref.edit();
                        editor.remove(emergency).commit();
                        editor.putString(emergency,json);
                        editor.commit();
                        emergency03TV.setText(emergencyArrayList.get(2).getName()+" "+emergencyArrayList.get(2).getPhoneNumber());
                        dialog.cancel();

                    }
                });
                alert.show();
                break;
        }
    }

}
