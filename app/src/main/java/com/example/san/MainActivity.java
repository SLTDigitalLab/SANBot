package com.example.san;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.san.MyUtils.rotateAtRelativeAngle;

import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.Toast;

import static com.example.san.MyUtils.concludeSpeak;
import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.EmotionsType;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.function.beans.headmotion.LocateAbsoluteAngleHeadMotion;
import com.sanbot.opensdk.function.beans.headmotion.RelativeAngleHeadMotion;
import com.sanbot.opensdk.function.beans.wing.AbsoluteAngleWingMotion;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.HeadMotionManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.SpeechManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;
import com.sanbot.opensdk.function.unit.interfaces.hardware.PIRListener;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class  MainActivity extends TopBaseActivity {
    private final static String TAG = "DIL-SPLASH";

    public static boolean busy = false;

    @BindView(R.id.exitButton)
    Button exitButton;

    private ModularMotionManager modularMotionManager; //wander
    private HardWareManager hardWareManager;
    private WheelMotionManager wheelMotionManager;
    private SpeechManager speechManager; //voice, speechRec
    private SystemManager systemManager; //emotions
    private HeadMotionManager headMotionManager;    //head movements
    private WingMotionManager wingMotionManager;    //hands movements
    //head motion
    LocateAbsoluteAngleHeadMotion locateAbsoluteAngleHeadMotion = new LocateAbsoluteAngleHeadMotion(
            LocateAbsoluteAngleHeadMotion.ACTION_VERTICAL_LOCK,90,30
    );
    RelativeAngleHeadMotion relativeHeadMotionDOWN = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_DOWN, 30);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            register(ChoiceActivity.class);
            //screen always on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
            systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
            modularMotionManager = (ModularMotionManager) getUnitManager(FuncConstant.MODULARMOTION_MANAGER);
            hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
            wheelMotionManager = (WheelMotionManager) getUnitManager(FuncConstant.WHEELMOTION_MANAGER);
            headMotionManager = (HeadMotionManager) getUnitManager(FuncConstant.HEADMOTION_MANAGER);
            wingMotionManager = (WingMotionManager) getUnitManager(FuncConstant.WINGMOTION_MANAGER);
            //float button of the system
            systemManager.switchFloatBar(true, getClass().getName());

            //check app permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{INTERNET}, 12);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 12);

            }
            //LOAD handshakes stats
            MySettings.initializeXML();
            MySettings.loadHandshakes();

            //initialize speak
            MySettings.initializeSpeak();




            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                @OnClick(R.id.exitButton)
                public void onClick(View view) {
                    wanderOffNow();
                    finish();
                }
            });



            initHardwareListeners();
            //initialize body
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //hands down
                    AbsoluteAngleWingMotion absoluteAngleWingMotion = new AbsoluteAngleWingMotion(AbsoluteAngleWingMotion.PART_BOTH, 8, 180);
                    wingMotionManager.doAbsoluteAngleMotion(absoluteAngleWingMotion);
                    //head up
                    headMotionManager.doAbsoluteLocateMotion(locateAbsoluteAngleHeadMotion);
                    //initially sets the wander to on
                    wanderOnNow();
                }
            }, 1000);





        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();

        }
    }
    @Override
    protected void onMainServiceConnected() {

    }
    public void wanderOnNow() {
        if (!busy) {
            MySettings.setWanderAllowed(true);
            Toast.makeText(MainActivity.this, "Wander " + MySettings.isWanderAllowed()+" now", Toast.LENGTH_SHORT).show();
            modularMotionManager.switchWander(MySettings.isWanderAllowed());
            Log.i(TAG, "Wander " + MySettings.isWanderAllowed() + " now");
        }
    }
    public void wanderOffNow() {
        MySettings.setWanderAllowed(false);
        Toast.makeText(MainActivity.this, "Wander off now", Toast.LENGTH_SHORT).show();
        modularMotionManager.switchWander(false);
        Log.i(TAG, "Wander forced off now");
    }

    private void initHardwareListeners() {


        hardWareManager.setOnHareWareListener(new PIRListener() {
            @Override
            public void onPIRCheckResult(boolean isCheck, int part) {

                if (part != 1 && isCheck==true) {
                    wanderOffNow();
                    Toast.makeText(getApplicationContext(), "you are behind me", Toast.LENGTH_SHORT).show();
                    //if it's the back PIR
                    Log.i(TAG, "PIR back triggered -> rotating");
                    MySettings.setSoundRotationAllowed(true);

                        //flicker led
                        hardWareManager.setLED(new LED(LED.PART_ALL, LED.MODE_FLICKER_RED));
                        //rotate at angle
                        rotateAtRelativeAngle(wheelMotionManager, 180);




                        //starts greeting with this person passing
                        busy = true;
                        Toast.makeText(MainActivity.this, "Smiling", Toast.LENGTH_SHORT).show();
                        systemManager.showEmotion(EmotionsType. SMILE);
                        //say hi

                        speechManager.startSpeak(getString(R.string.Welcome_to_Techno_2023_We_are_glad_that_you_are_here), MySettings.getSpeakDefaultOption());
                        concludeSpeak(speechManager);


                        // 50% say Good morning/afternoon/ecc...
                        double random_num = Math.random();
                        Log.i(TAG, "Random = " + random_num);
                        if (random_num < 0.5) {
                            int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                            if (hours < 6) {
                                speechManager.startSpeak(getString(R.string.Top_of_the_morning_to_you), MySettings.getSpeakDefaultOption());
                            } else if (hours < 12) {
                                speechManager.startSpeak(getString(R.string.Good_Morning), MySettings.getSpeakDefaultOption());
                            } else if (hours < 16) {
                                speechManager.startSpeak(getString(R.string.Good_Afternoon), MySettings.getSpeakDefaultOption());
                            } else if (hours < 19) {
                                speechManager.startSpeak(getString(R.string.Good_Evening), MySettings.getSpeakDefaultOption());
                            }
                            concludeSpeak(speechManager);

                        }
                        Intent intent = new Intent(MainActivity.this, videoActivity.class);
                        startActivity(intent);
                        finish();





                } else if(part ==1 && isCheck==true ){

                    wanderOffNow();

                    //starts greeting with this person passing
                    busy = true;
                    Toast.makeText(MainActivity.this, "Smiling", Toast.LENGTH_SHORT).show();
                    systemManager.showEmotion(EmotionsType. SMILE);
                    //say hi

                    speechManager.startSpeak(getString(R.string.Welcome_to_Techno_2023_We_are_glad_that_you_are_here), MySettings.getSpeakDefaultOption());
                    concludeSpeak(speechManager);


                    // 50% say Good morning/afternoon/ecc...
                    double random_num = Math.random();
                    Log.i(TAG, "Random = " + random_num);
                    if (random_num < 0.5) {
                        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        if (hours < 6) {
                            speechManager.startSpeak(getString(R.string.Top_of_the_morning_to_you), MySettings.getSpeakDefaultOption());
                        } else if (hours < 12) {
                            speechManager.startSpeak(getString(R.string.Good_Morning), MySettings.getSpeakDefaultOption());
                        } else if (hours < 16) {
                            speechManager.startSpeak(getString(R.string.Good_Afternoon), MySettings.getSpeakDefaultOption());
                        } else if (hours < 19) {
                            speechManager.startSpeak(getString(R.string.Good_Evening), MySettings.getSpeakDefaultOption());
                        }
                        concludeSpeak(speechManager);
                    }


                    Intent intent = new Intent(MainActivity.this, videoActivity.class);
                    startActivity(intent);
                    finish();




                }
                else {
                    wanderOnNow();
                    systemManager.showEmotion(EmotionsType. WHISTLE);


                }
            }
        });

    }


}