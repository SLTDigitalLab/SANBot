package com.example.san;



import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import static com.example.san.MyUtils.rotateAtRelativeAngle;
import com.sanbot.opensdk.function.unit.interfaces.hardware.PIRListener;
import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.function.beans.headmotion.LocateAbsoluteAngleHeadMotion;
import com.sanbot.opensdk.function.beans.headmotion.RelativeAngleHeadMotion;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.HeadMotionManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity2 extends TopBaseActivity {
    private final static String TAG = "DIL-BAS";
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    public static boolean busy = false;
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
            register(MainActivity2.class);
            //screen always on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);
            ButterKnife.bind(this);
            hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);
            systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
            hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
            wheelMotionManager = (WheelMotionManager) getUnitManager(FuncConstant.WHEELMOTION_MANAGER);

            //float button of the system
            systemManager.switchFloatBar(true, getClass().getName());

        initHardwareListeners();
        //LOAD handshakes stats
        MySettings.initializeXML();
        MySettings.loadHandshakes();

        //initialize speak
        MySettings.initializeSpeak();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onMainServiceConnected() {

    }
    /**
     * initialize listeners
     */
    private void initHardwareListeners() {


        hardWareManager.setOnHareWareListener(new PIRListener() {
            @Override
            public void onPIRCheckResult(boolean isCheck, int part) {
                if (part != 1) {
                    Toast.makeText(getApplicationContext(),"you are behind me", Toast.LENGTH_SHORT).show();
                    //if it's the back PIR
                    Log.i(TAG, "PIR back triggered -> rotating");
                    if (!busy && MySettings.isSoundRotationAllowed()) {
                        //flicker led
                        hardWareManager.setLED(new LED(LED.PART_ALL, LED.MODE_FLICKER_RED));
                        //rotate at angle
                        rotateAtRelativeAngle(wheelMotionManager, 180);

                    }
                } else {
                    Toast.makeText(getApplicationContext(),"you are in front of me", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "PIR frontal triggered");
                }
            }
        });
    }
}