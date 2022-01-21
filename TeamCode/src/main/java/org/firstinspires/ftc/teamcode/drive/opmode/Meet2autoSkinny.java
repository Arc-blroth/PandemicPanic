package org.firstinspires.ftc.teamcode.drive.opmode;

import static org.hermitsocialclub.drive.config.DriveConstants.MAX_ACCEL;
import static org.hermitsocialclub.drive.config.DriveConstants.MAX_ANG_ACCEL;
import static org.hermitsocialclub.drive.config.DriveConstants.MAX_ANG_VELO;
import static org.hermitsocialclub.util.MoveUtils.m;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumConstraints;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource;
import org.hermitsocialclub.drive.BaselineMecanumDrive;
import org.hermitsocialclub.hydra.opmodes.SubmatSetupOp;
import org.hermitsocialclub.hydra.vision.StaccDetecc;
import org.hermitsocialclub.hydra.vision.VisionPipeline;
import org.hermitsocialclub.hydra.vision.VisionSemaphore;
import org.hermitsocialclub.hydra.vision.util.VisionUtils;
import org.hermitsocialclub.telecat.PersistantTelemetry;
import org.hermitsocialclub.tomato.BarcodeDetect;
import org.hermitsocialclub.hydra.vision.FirstFrameSemaphore;

@Autonomous(name = "Meet2AutoSkinny")
public class Meet2autoSkinny extends LinearOpMode {

    private BaselineMecanumDrive drive;
    private PersistantTelemetry telemetry;

    Trajectory backUp;
    Trajectory toBlueHub;
    Trajectory toBlueBarrier;
    Trajectory toBlueWarehouse;
    Trajectory toBlueWarehouseBack;
    Trajectory goBack;

    Pose2d blueStart =  new Pose2d(10,60,m(90));
    Vector2d blueHub = new Vector2d(-10, 40);
    Pose2d blueBarrier = new Pose2d(19,64.5, m(0));
//    Pose2d blueCarousel = new Pose2d(-12,44,m(90));
//    Pose2d blueBarrier = new Pose2d(12,42,m(0));
//    Pose2d bluePit = new Pose2d(48,48,m(45));

    private VisionPipeline visionPipeline;
    private FirstFrameSemaphore semaphore;
    private BarcodeDetect detector;
    private Byte barcodeLevel;

    private MotorConfigurationType carouselType;
    private double carouselSpeed = .3;
    private MotorConfigurationType liftType;
    private MotorConfigurationType intakeType;
    private Trajectory toBlueBarrierBack;


    @Override
    public void runOpMode() throws InterruptedException {

        telemetry = new PersistantTelemetry(super.telemetry);
        drive = new BaselineMecanumDrive(hardwareMap,telemetry);

        //init vision stuff
//        detector = new BarcodeDetect(true);
//        semaphore = new FirstFrameSemaphore();
//        visionPipeline = new VisionPipeline(hardwareMap, telemetry, detector, semaphore);

//        semaphore.waitForFirstFrame();
//        barcodeLevel = detector.getResult();
//
//        telemetry.setData("Barcode Level", barcodeLevel);


        drive.setPoseEstimate(blueStart);
        toBlueHub = drive.trajectoryBuilder(blueStart)
                .strafeTo(blueHub)
                .build();
        toBlueBarrier = drive.trajectoryBuilder(new Pose2d(blueHub, m(90)))
                .lineToLinearHeading(blueBarrier)
                .build();

        toBlueWarehouse = drive.trajectoryBuilder(toBlueBarrier.end())
                .forward(35)
                .build();

        toBlueWarehouseBack = drive.trajectoryBuilder(toBlueBarrier.end(),true)
                .forward(35)
                .build();
        toBlueBarrierBack = drive.trajectoryBuilder(new Pose2d(blueHub, m(90)), true)
                .lineToLinearHeading(blueBarrier)
                .build();

//        toBlueWarehouse = drive.trajectoryBuilder(new Pose2d(blueHub, m(90)),m(90))
//                .splineTo(blueBarrier,m(0))

//                .forward(36)
//                .build();



//        backUp = drive.trajectoryBuilder(blueStart, -90)
//                .splineToLinearHeading(new Pose2d(-59.6360, 59.1360,m(140)), m(135))
//                .build();
//
//        toBlueHub = drive.trajectoryBuilder(backUp.end(),m(-20))
//                .splineToLinearHeading(blueCarousel,m(-90))
//                .build();
//        toBlueBarrier = drive.trajectoryBuilder(blueCarousel,m(0))
//                .splineToSplineHeading(blueBarrier,m(0))
//                //.lineTo(new Vector2d(40,42), new DriveConstraints(12,MAX_ACCEL,0.0,
//                //MAX_ANG_VELO,MAX_ANG_ACCEL,0.0))
//                .build();
//        toBlueWarehouse = drive.trajectoryBuilder(blueBarrier)
//                .splineToLinearHeading(bluePit,m(20))
//                .build();
//        goBack = drive.trajectoryBuilder(bluePit)
//                .back(5)
//                .build();

        detector = new BarcodeDetect(true);
        this.semaphore = new FirstFrameSemaphore();
        this.visionPipeline = new VisionPipeline(hardwareMap, telemetry, detector, semaphore);

        barcodeLevel = detector.getResult();

        telemetry.setData("Barcode Level", barcodeLevel);
        CameraStreamSource cameraStream = visionPipeline.getCamera();
        FtcDashboard.getInstance().startCameraStream(cameraStream,0);

        carouselType = drive.duck_wheel.getMotorType();

        liftType = drive.lift.getMotorType();
        intakeType = drive.intake.getMotorType();

        drive.lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        drive.duck_wheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        waitForStart();


//        drive.followTrajectory(backUp);
        drive.followTrajectory(toBlueHub);
//        drive.duck_wheel.setPower(0.15);
//        sleep(1200);
//        drive.duck_wheel.setPower(0);
//        drive.followTrajectory(toBlueHub);
        drive.lift.setVelocity(liftType
                .getMaxRPM() / 60 * liftType.getAchieveableMaxRPMFraction() * .85 *
                1, AngleUnit.RADIANS);
        sleep(1500);
        drive.outtakeArm.setPosition(0.55);
        drive.lift.setPower(0);
        sleep(800);
        drive.outtakeArm.setPosition(0);
        sleep(800);
        drive.outtakeArm.setPosition(0.45);
        drive.lift.setVelocity(liftType
                .getMaxRPM() / 60 * liftType.getAchieveableMaxRPMFraction() * -.55 *
                1, AngleUnit.RADIANS);
        sleep(2100);
        drive.lift.setPower(0);
        drive.followTrajectory(toBlueBarrier);
        drive.intake.setVelocity(0.85
                * intakeType.getAchieveableMaxRPMFraction() *
                intakeType.getMaxRPM() / 60 * Math.PI * 2, AngleUnit.RADIANS);
        drive.followTrajectory(toBlueWarehouse);
        sleep(300);
        drive.intake.setPower(0);
        drive.intake.setVelocity(-0.85
                * intakeType.getAchieveableMaxRPMFraction() *
                intakeType.getMaxRPM() / 60 * Math.PI * 2, AngleUnit.RADIANS);
        drive.followTrajectory(toBlueWarehouseBack);
        drive.intake.setPower(0);
        drive.followTrajectory(toBlueBarrierBack);
        drive.lift.setVelocity(liftType
                .getMaxRPM() / 60 * liftType.getAchieveableMaxRPMFraction() * .85 *
                1, AngleUnit.RADIANS);
        sleep(2100);
        drive.outtakeArm.setPosition(0.55);
        drive.lift.setPower(0);
        sleep(800);
        drive.outtakeArm.setPosition(0);
        sleep(800);
        drive.outtakeArm.setPosition(0.45);




//        drive.setWeightedDrivePower(new Pose2d(.3,0));
//        sleep(1800);
//        drive.setWeightedDrivePower(new Pose2d());
//        sleep(2000);


    }
}
