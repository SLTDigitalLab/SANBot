package com.example.san;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.san.MyUtils.rotateAtRelativeAngle;
import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.beans.OperationResult;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.function.beans.StreamOption;
import com.sanbot.opensdk.function.beans.headmotion.LocateAbsoluteAngleHeadMotion;
import com.sanbot.opensdk.function.beans.headmotion.RelativeAngleHeadMotion;
import com.sanbot.opensdk.function.beans.wheelmotion.DistanceWheelMotion;
import com.sanbot.opensdk.function.beans.wing.AbsoluteAngleWingMotion;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.HeadMotionManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;
import com.sanbot.opensdk.function.unit.interfaces.hardware.PIRListener;


import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends TopBaseActivity  {
    private final static String TAG = "DIL-BAS";

    @BindView(R.id.buttonarro)
    Button buttonarro;
    @BindView(R.id.buttonsurp)
    Button buttonsurp;
    @BindView(R.id.buttonwhis)
    Button buttonwhis;
    @BindView(R.id.buttonlau)
    Button buttonlau;
    @BindView(R.id.buttongb)
    Button buttongb;
    @BindView(R.id.buttonshy)
    Button buttonshy;
    @BindView(R.id.buttonsweat)
    Button buttonsweat;
    @BindView(R.id.buttonsnicker)
    Button buttonsnicker;
    @BindView(R.id.buttonpick)
    Button buttonpick;
    @BindView(R.id.buttoncry)
    Button buttoncry;
    @BindView(R.id.buttonab)
    Button buttonab;
    @BindView(R.id.buttonang)
    Button buttonang;
    @BindView(R.id.buttonki)
    Button buttonki;
    @BindView(R.id.buttonsleep)
    Button buttonsleep;
    @BindView(R.id.buttonsmile)
    Button buttonsmile;
    @BindView(R.id.buttongri)
    Button buttongri;
    @BindView(R.id.buttonques)
    Button buttonques;
    @BindView(R.id.buttonfaint)
    Button buttonfaint;
    @BindView(R.id.buttonprise)
    Button buttonprise;
    @BindView(R.id.buttonnormal)
    Button buttonnormal;

    //robot managers

    private HDCameraManager hdCameraManager; //video, faceRec
    private HeadMotionManager headMotionManager;    //head movements
    private WingMotionManager wingMotionManager;    //hands movements
    private SystemManager systemManager; //emotions
    private HardWareManager hardWareManager; //leds //touch sensors //voice locate //gyroscope
    private ModularMotionManager modularMotionManager; //wander
    private WheelMotionManager wheelMotionManager;
    //head motion
    LocateAbsoluteAngleHeadMotion locateAbsoluteAngleHeadMotion = new LocateAbsoluteAngleHeadMotion(
            LocateAbsoluteAngleHeadMotion.ACTION_VERTICAL_LOCK,90,30
    );
    RelativeAngleHeadMotion relativeHeadMotionDOWN = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_DOWN, 30);

    private List<Integer> handleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            register(BaseActivity.class);
            //screen always on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_base);
            ButterKnife.bind(this);
            hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);
            systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
            hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
            wheelMotionManager = (WheelMotionManager) getUnitManager(FuncConstant.WHEELMOTION_MANAGER);

            //float button of the system
            systemManager.switchFloatBar(true, getClass().getName());







        }catch(Exception e){
            Log.e(TAG, "Error in onCreate: " + e.getMessage());

        }
        //turnOnLights();
        //LOAD handshakes stats
        MySettings.initializeXML();
        MySettings.loadHandshakes();

        //initialize speak
        MySettings.initializeSpeak();
        buttonarro.setOnClickListener(new View.OnClickListener() {
            @Override
            @OnClick(R.id.buttonarro)
            public void onClick(View view) {

            }
        });
        buttonsurp.setOnClickListener(new View.OnClickListener() {
            @Override
            @OnClick(R.id.buttonsurp)
            public void onClick(View view) {

            }
        });





    }

    @Override
    protected void onMainServiceConnected() {

    }

    private void moveForward(){
        // Create a DistanceWheelMotion instance
        DistanceWheelMotion distanceWheelMotion = new DistanceWheelMotion(
                DistanceWheelMotion.ACTION_FORWARD_RUN, 5, 100
        );

        // Set up the Wheel Motion Listener before executing the motion
        wheelMotionManager.setWheelMotionListener(new WheelMotionManager.WheelMotionListener() {
            @Override
            public void onWheelStatus(String s) {
                Log.i("Cris", "onWheelStatus: s=" + s);
            }
        });

        // Execute the distance motion
        wheelMotionManager.doDistanceMotion(distanceWheelMotion);

    }

    private void turnOnLights() {
        hardWareManager.setOnHareWareListener(new PIRListener() {
            @Override
            public void onPIRCheckResult(boolean isCheck, int part) {


                //if it's the back PIR
                Log.i(TAG, "PIR back triggered -> rotating");

                //flicker led
                hardWareManager.setLED(new LED(LED.PART_ALL, LED.MODE_FLICKER_PINK));

                //rotate at angle
                rotateAtRelativeAngle(wheelMotionManager, 180);




            }
        });

    }




}