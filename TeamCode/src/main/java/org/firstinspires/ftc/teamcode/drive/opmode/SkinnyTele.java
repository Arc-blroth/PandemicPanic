package org.firstinspires.ftc.teamcode.drive.opmode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource;
import org.hermitsocialclub.drive.BaselineMecanumDrive;
import org.hermitsocialclub.hydra.vision.FirstFrameSemaphore;
import org.hermitsocialclub.hydra.vision.VisionPipeline;
import org.hermitsocialclub.localizers.T265LocalizerPro;
import org.hermitsocialclub.localizers.T265LocalizerRR;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.spartronics4915.lib.T265Localizer;
import com.spartronics4915.lib.T265Helper;
import org.hermitsocialclub.telecat.PersistantTelemetry;
import org.hermitsocialclub.tomato.DuckDetect;
import org.hermitsocialclub.util.LinearHelpers;

import static org.checkerframework.checker.units.UnitsTools.m;
import static org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity.slamra;
import static org.hermitsocialclub.drive.config.DriveConstants.*;
//import static org.hermitsocialclub.localizers.T265LocalizerRR.sideMod;
import static org.hermitsocialclub.util.MoveUtils.m;

@TeleOp(name = "SkinnyTele")
public class SkinnyTele extends OpMode {
    Canvas field;
    TelemetryPacket packet;

    private PersistantTelemetry telemetry;

    private final ElapsedTime gameTime = new ElapsedTime();

    private BaselineMecanumDrive drive;

    private final FtcDashboard dash = FtcDashboard.getInstance();

    private final int robotRadius = 8;

    private double trigVal = 0;
    private LinearHelpers linears;
    //in ticks
    private double liftSpeed = -15;
    private MotorConfigurationType liftType;
    boolean lastUpFlick = false;
    boolean lastDownFlick = false;

    private double intakeSpeed = 0.65;
    private MotorConfigurationType intakeType;

    private double carouselSpeed = 0.70;
    private MotorConfigurationType carouselType;

    public static double liftP = 3.0;
    public static double liftI = 1.0;
    public static double liftD = .1;
    public static double liftF = 22.259453425873854;
    public static PIDFCoefficients liftCoefficients = new PIDFCoefficients(3,1,.1,22.259453425873854);

    Trajectory toSharedHub_red;
    Trajectory toSharedHub_blue;

    Pose2d blueWarehouse = new Pose2d(65, 38, m(90));
    Vector2d blueBarrier = new Vector2d(64, 20);
    Pose2d blueSharedHub = new Pose2d(61, 15, m(30));

    Pose2d redWarehouse = new Pose2d(65, -38, m(-90));
    Vector2d redBarrier = new Vector2d(65, -20);
    Pose2d redSharedHub = new Pose2d(63, -15, m(-30));

    boolean leftStickButton = false;
    boolean quantization = true;

    int invert = -1;
    private boolean lastXMash = false;
    private boolean lastX2Mash = false;

    DuckDetect detector = new DuckDetect();
    FirstFrameSemaphore semaphore = new FirstFrameSemaphore();
    VisionPipeline visionPipeline;
    @Override

    public void init() {
        telemetry = new PersistantTelemetry(super.telemetry);
        RUN_USING_ENCODER = true;
//        T265LocalizerRR.setSideMod(1);
        drive = new BaselineMecanumDrive(hardwareMap, telemetry);
//        drive.setPoseEstimate(BaselineMecanumDrive.poseEndingAuton);
        drive.duck_wheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        drive.lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        drive.lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftType = drive.lift.getMotorType();
        intakeType = drive.intake.getMotorType();
        carouselType = drive.duck_wheel.getMotorType();
        telemetry.setData("Lift Pos",drive.lift.getCurrentPosition());
        telemetry.setData("Ticks Per Rev", liftType.getTicksPerRev());
        linears = new LinearHelpers(drive, telemetry,gameTime);
        linears.setMode(LinearHelpers.MODE.TELEOP);


        visionPipeline = new VisionPipeline(hardwareMap, telemetry, detector, semaphore);
        CameraStreamSource cameraStream = visionPipeline.getCamera();
        FtcDashboard.getInstance().startCameraStream(cameraStream, 0);

        toSharedHub_blue = drive.trajectoryBuilder(blueWarehouse, m(-70))
                .splineToConstantHeading(blueBarrier, m(250))
                .splineToSplineHeading(blueSharedHub, m(250))
                .build();

        toSharedHub_red = drive.trajectoryBuilder(redWarehouse, m(70))
                .splineToConstantHeading(redBarrier, m(-250))
                .splineToSplineHeading(redSharedHub, m(-250))
                .build();

    }

    @Override
    public void init_loop() {
        super.init_loop();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void loop() {
//        trigVal = -gamepad2.right_stick_y > 0.05 ? -gamepad2.right_stick_y * 1.25 :
//                -gamepad2.right_stick_y < -.05 ? -gamepad2.right_stick_y : 0.000;
////        if(trigVal > .05)
//        drive.lift.setVelocity(liftType
//                .getMaxRPM() / 60 * 2 * Math.PI * liftType.getAchieveableMaxRPMFraction() * .85 *
//                trigVal, AngleUnit.RADIANS);
//        if (trigVal == 0) {
//            drive.lift.setPower(0.2);
//        }


//        if (gamepad2.left_stick_y < -.25 && !lastUpFlick) {
//            linears.LiftLinears();
//        }
//

//        if (gamepad2.right_stick_y < -0.05) {
//            linears.setState(LinearHelpers.STATE.UP);
//        } else if (gamepad2.right_stick_y > 0.05 && linears.getState().equals(LinearHelpers.STATE.SAME)) {
//            linears.setState(LinearHelpers.STATE.DOWN);
//        } else if (linears.getState().equals(LinearHelpers.STATE.SAME)){
//            linears.setState(LinearHelpers.STATE.SET);
//        }

        if (gamepad2.y){
            quantization = !quantization;
        }
//        leftStickButton = gamepad2.left_stick_button;
        if(quantization){
            if (gamepad2.right_stick_y < 0){
                linears.setState(LinearHelpers.STATE.UP);
            }
            else if (gamepad2.right_stick_y > 0 && !linears.getState().equals(LinearHelpers.STATE.SAME)) {
                linears.setState(LinearHelpers.STATE.DOWN);
            }
            else if (!linears.getState().equals(LinearHelpers.STATE.SAME)){
                linears.setState(LinearHelpers.STATE.SET);
            }
            linears.LinearUpdateNew();
        }
        else {
            trigVal = -gamepad2.right_stick_y > 0.05 ? -gamepad2.right_stick_y * 1.25 :
                    -gamepad2.right_stick_y < -.05 ? -gamepad2.right_stick_y : 0.000;
//        if(trigVal > .05)
            drive.lift.setVelocity(liftType
                    .getMaxRPM() / 60 * 2 * Math.PI * liftType.getAchieveableMaxRPMFraction() * .85 *
                    trigVal, AngleUnit.RADIANS);
            if (trigVal == 0) {
                drive.lift.setPower(0.2);
            }
        }

        telemetry.setData("liftMode: ", drive.lift.getMode().toString());
        telemetry.setData("liftPID: ", drive.lift
                .getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION).toString());

        lastUpFlick = gamepad2.left_stick_y < -.25;
        lastDownFlick = gamepad2.left_stick_y > .25;

        if (gamepad2.right_trigger > 0.05) {
            drive.intake.setVelocity(intakeSpeed
                    * intakeType.getAchieveableMaxRPMFraction() *
                    intakeType.getMaxRPM() / 60 * Math.PI * 2, AngleUnit.RADIANS);
        } else if (gamepad2.left_trigger > .05) {
            drive.intake.setVelocity(-intakeSpeed
                    * intakeType.getAchieveableMaxRPMFraction() *
                    intakeType.getMaxRPM() / 60 * Math.PI * 2, AngleUnit.RADIANS);
        } else drive.intake.setPower(0);

        if (Math.abs(gamepad2.left_stick_x) > 0.05) {
            drive.duck_wheel.setVelocity(carouselSpeed * (gamepad2.left_stick_x/Math.abs(gamepad2.left_stick_x)) *
                    carouselType.getAchieveableMaxRPMFraction()
                    * carouselType.getMaxRPM() / 60 *
                    Math.PI * 2, AngleUnit.RADIANS);
        } else {
            drive.duck_wheel.setPower(0);
        }

//        telemetry.setData("color_in Red",drive.color_intake.red());
//        telemetry.setData("color_in Blue",drive.color_intake.blue());
//        telemetry.setData("color_in Green",drive.color_intake.green());
//        drive.outtakeArm.setDirection(Servo.Direction.REVERSE);
        if (gamepad2.left_bumper) {
            telemetry.setData("left_bumper", " pressed");
            drive.outtakeArm.setPosition(0.40);
            telemetry.setData("Servo_Pos: ", drive.outtakeArm.getPosition());
        } else {
            drive.outtakeArm.setPosition(1);
            telemetry.setData("Servo_Pos: ", drive.outtakeArm.getPosition());
        }
       /* drive.duck_wheel.setVelocity(liftType
                .getAchieveableMaxTicksPerSecond() * .65 *
                trigVal, AngleUnit.RADIANS);

    }*/
        packet = new TelemetryPacket();

        field = packet.fieldOverlay();

        if (!lastX2Mash && gamepad2.x) {
            if (linears.levelOverride < 50) linears.levelOverride = 10000;
            else if (linears.levelOverride > 50) linears.levelOverride = 0;
        }
        lastX2Mash = gamepad2.x;

        if (!lastXMash && gamepad2.x) {
            invert *= -1;
        }
        lastXMash = gamepad1.x;

        if (gamepad1.dpad_right && !drive.isBusy()) {
            drive.followTrajectory(toSharedHub_blue);
        }
        if (gamepad1.dpad_left && !drive.isBusy()) {
            drive.followTrajectory(toSharedHub_red);
        }

//        telemetry.setData("y_thing: ", gamepad1.left_stick_y);
        if (!drive.isBusy())
            {drive.setWeightedDrivePower(
                new Pose2d(
                        invert * gamepad1.left_stick_y,
                        invert * gamepad1.left_stick_x,
                        -gamepad1.right_stick_x
                ).times(1.25));
            }
        drive.update();

        Pose2d pose = drive.getPoseEstimate();
        double angle = pose.getHeading();

        /*telemetry.setData("x", pose.getX());
        telemetry.setData("y", pose.getY());
        telemetry.setData("heading", pose.getHeading());*/



        field.strokeCircle(pose.getX(), pose.getY(), angle);
        double arrowX = Math.cos(angle) * robotRadius, arrowY = Math.sin(angle) * robotRadius;
        double x1 = pose.getX() + arrowX / 2, y1 = pose.getY() + arrowY / 2;
        double x2 = pose.getX() + arrowX, y2 = pose.getY() + arrowY;
        field.strokeLine(x1, y1, x2, y2);
        packet.put("Pose", pose.toString());
        telemetry.setData("Pose Estimate",pose);

        Pose2d cameraPose = slamra.getLastReceivedCameraUpdate().pose;
        double cameraAngle = cameraPose.getHeading();
        field.strokeCircle(cameraPose.getX(), cameraPose.getY(), cameraAngle);
        double arrowX2 = Math.cos(cameraAngle) * robotRadius, arrowY2 = Math.sin(cameraAngle) * robotRadius;
        double x3 = cameraPose.getX() + arrowX2 / 2, y3 = cameraPose.getY() + arrowY2 / 2;
        double x4 = cameraPose.getX() + arrowX2, y4 = cameraPose.getY() + arrowY2;
        field.strokeLine(x3, y3, x4, y4);
        packet.put("Camera Pose", cameraPose.toString());
        telemetry.setData("Camera Pose Estimate",cameraPose);
        telemetry.setData("rightFollower",drive.intake.getCurrentPosition());
        telemetry.setData("leftFollower",drive.leftFollower.getCurrentPosition());


        dash.sendTelemetryPacket(packet);

    }

    @Override
    public void stop() {
        super.stop();
    }
}
