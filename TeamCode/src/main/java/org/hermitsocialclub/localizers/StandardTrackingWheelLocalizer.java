package org.hermitsocialclub.localizers;

import androidx.annotation.NonNull;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.ThreeTrackingWheelLocalizer;
import com.qualcomm.hardware.motors.NeveRest20Gearmotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import org.hermitsocialclub.telecat.PersistantTelemetry;
import org.hermitsocialclub.util.Encoder;

import java.util.Arrays;
import java.util.List;

/*
 * Sample tracking wheel localizer implementation assuming the standard configuration:
 *
 *    /--------------\
 *    |     ____     |
 *    |     ----     |
 *    | ||        || |
 *    | ||        || |
 *    |              |
 *    |              |
 *    \--------------/
 *
 */
@Config
public class StandardTrackingWheelLocalizer extends ThreeTrackingWheelLocalizer {
    public static double TICKS_PER_REV = 8192;
    public static double WHEEL_RADIUS = 1; // in
    public static MotorConfigurationType neverRest20GearMotor = MotorConfigurationType.getMotorType(NeveRest20Gearmotor.class);
    public static double GEAR_RATIO = 1; // output (wheel) speed / input (encoder) speed

    public static double LATERAL_DISTANCE = 15.851035725;//15.3857258; // in; distance between the left and right wheels
    public static double FORWARD_OFFSET = -3.875; // in; offset of the lateral wheel

    public List<Double> initialPos;
    public boolean initialPosTaken;

    private final PersistantTelemetry telemetry;

    private final Encoder leftEncoder;
    private final Encoder rightEncoder;
    private final Encoder frontEncoder;

    public static double LEFT_X_MULTIPLIER = 1.1880561390647101148678033918417;//1.228778966131907;
    public static double RIGHT_X_MULTIPLIER = 1.1880561390647101148678033918417;//1.228778966131907;//71.25/60.07878281971142 * (61.25/59.866903109526284);//69/56.42137206749;
    public static double Y_MULTIPLIER = 1.2216224960779911031098058493221;//1.192406360167337;//(99.25/100)*(98.625/100)*77/59.94436059903564 *(42.125/88.68691433432166)*(53.75/26.662470262090235);//37/28.990683875252298;


    public StandardTrackingWheelLocalizer(HardwareMap hardwareMap, PersistantTelemetry telemetry) {
        super(Arrays.asList(
                new Pose2d(0, LATERAL_DISTANCE / 2, 0), // left
                new Pose2d(0, -LATERAL_DISTANCE / 2, 0), // right
                new Pose2d(FORWARD_OFFSET, 0, Math.toRadians(90)) // front
        ));

        leftEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "midEncoder"));
        rightEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "left_drive_2"));
        frontEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "left_drive"));
/*
        expansionHub = hardwareMap.get(ExpansionHubEx.class, "Control Hub");

        motor0 = (ExpansionHubMotor) hardwareMap.get(DcMotorEx.class, "right_drive");
        motor1 = (ExpansionHubMotor) hardwareMap.get(DcMotorEx.class, "left_drive");
        motor2 = (ExpansionHubMotor) hardwareMap.get(DcMotorEx.class, "left_drive_2");

 */
/*
        trigger1 = hardwareMap.get(AnalogOutput.class,"trigger1");
        trigger2 = hardwareMap.get(AnalogOutput.class,"trigger2");

        echo1 = hardwareMap.get(AnalogInput.class,"echo1");
        echo2 = hardwareMap.get(AnalogInput.class,"echo2");

        ultra1 = new AnalogUltrasonic(echo1,trigger1,telemetry,bulkData,expansionHub);
        ultra2 = new AnalogUltrasonic(echo2,trigger2,telemetry,bulkData,expansionHub);
*/
        // TODO: reverse any encoders using Encoder.setDirection(Encoder.Direction.REVERSE)
        leftEncoder.setDirection(Encoder.Direction.REVERSE);
        frontEncoder.setDirection(Encoder.Direction.REVERSE);
        //rightEncoder.setDirection(Encoder.Direction.REVERSE);

        initialPos = getWheelPositions();
        initialPosTaken = true;

        this.telemetry = telemetry;

    }

    public static double encoderTicksToInches(double ticks) {
        return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV;
    }

    @NonNull
    @Override
    public List<Double> getWheelPositions() {

        List<Double> a = Arrays.asList(

                encoderTicksToInches(leftEncoder.getCurrentPosition() * LEFT_X_MULTIPLIER + 13),
                encoderTicksToInches(rightEncoder.getCurrentPosition() * RIGHT_X_MULTIPLIER),
                encoderTicksToInches(frontEncoder.getCurrentPosition() * Y_MULTIPLIER)
        );
        /*if(initialPosTaken) {
            telemetry.setDebug("leftEncoder", a.get(0) - initialPos.get(0));
            telemetry.setDebug("rightEncoder", a.get(1) - initialPos.get(1));
            telemetry.setDebug("frontEncoder", a.get(2) - initialPos.get(2));
            telemetry.setDebug("left-Right Multiplier", (a.get(1) - initialPos.get(1)) / (a.get(0) - initialPos.get(0)));
            telemetry.setDebug("left-Right Difference", (a.get(1) - initialPos.get(1)) - (a.get(0) - initialPos.get(0)));

        }*/
        return a;
    }

    public List<Double> getWheelChange() {


        return Arrays.asList(

                encoderTicksToInches(leftEncoder.getCurrentPosition() * LEFT_X_MULTIPLIER) - initialPos.get(0),
                encoderTicksToInches(rightEncoder.getCurrentPosition() * RIGHT_X_MULTIPLIER) - initialPos.get(1),
                encoderTicksToInches(frontEncoder.getCurrentPosition() * Y_MULTIPLIER) - initialPos.get(2)
        );
    }

    @NonNull
    @Override
    public List<Double> getWheelVelocities() {
        // TODO: If your encoder velocity can exceed 32767 counts / second (such as the REV Through Bore and other
        //  competing magnetic encoders), change Encoder.getRawVelocity() to Encoder.getCorrectedVelocity() to enable a
        //  compensation method


        return Arrays.asList(
                encoderTicksToInches(leftEncoder.getCorrectedVelocity()),
                encoderTicksToInches(rightEncoder.getCorrectedVelocity()),
                encoderTicksToInches(frontEncoder.getCorrectedVelocity())
        );
    }

/*

    public void cycleHardware(){
        bulkData = expansionHub.getBulkInputData();
    }

    public List<Double> getUltrasonicDistances(){
        List<Double> ultraList = new ArrayList<>();
        ultra1.pulse();
        ultra2.pulse();
        while (true){
            if(ultra1.getPulseFinished()){ultraList.add(0,ultra1.getDistance());}
            if(ultra2.getPulseFinished()){ultraList.add(1,ultra2.getDistance());}
            if(ultra1.getPulseFinished()&&ultra2.getPulseFinished()){break;}
        }
        return ultraList;
    }
 */
}
