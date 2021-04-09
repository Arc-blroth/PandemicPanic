/*package org.firstinspires.ftc.teamcode.drive.opmode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.hardware.motors.GoBILDA5202Series;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.hermitsocialclub.hydra.vision.StaccDetecc;
import org.hermitsocialclub.hydra.vision.VisionPipeline;
import org.hermitsocialclub.hydra.vision.VisionSemaphore;
import org.hermitsocialclub.telecat.PersistantTelemetry;

@Autonomous (name = "ArmExperiments")
public class ArmExperiments extends LinearOpMode {

    PersistantTelemetry telemetry = new PersistantTelemetry(super.telemetry);


    MotorConfigurationType goBildaOuttake =  MotorConfigurationType.getMotorType(GoBILDA5202Series.class);
    double outTake75Speed;
    public static double SPEED_PERCENT = .625;
    public int ringStack = 0;
    private StaccDetecc stackDetector;
    private VisionSemaphore semaphore;
    private VisionPipeline visionPipeline;
    private ElapsedTime time = new ElapsedTime();
*//*
    @Override
    public void runOpMode() throws InterruptedException {
        BaselineMecanumDrive drive = new BaselineMecanumDrive(hardwareMap,telemetry);

        outTake75Speed = ((SPEED_PERCENT * 2 * Math.PI * goBildaOuttake.getMaxRPM() * goBildaOuttake.getAchieveableMaxRPMFraction())/60);

        //drive.wobbleGrab.setPower(1);

        drive.setPoseEstimate(new Pose2d(-63,-15.5,0));

        stackDetector = new StaccDetecc();
        semaphore = new VisionSemaphore();
        visionPipeline = new VisionPipeline(hardwareMap, telemetry, stackDetector, semaphore);
        time.reset();
        while (time.seconds() < 4 && !isStarted()  && !isStopRequested()){
            ringStack = getZoneFromOpenCV();
            telemetry.setDebug("detected", ringStack);
        }
        telemetry.setDebug("done","viewing");
        telemetry.setDebug("final position",ringStack);


        Trajectory strafeRight = drive.trajectoryBuilder(drive.getPoseEstimate())
                .strafeRight(10)
                .build();
        Trajectory launchSpline = drive.trajectoryBuilder(drive.getPoseEstimate())
                .addDisplacementMarker(() -> {drive.wobbleGrab.setPower(1);})
                .splineToConstantHeading(new Vector2d(-3,-42),60)
                .build();
        Trajectory oneSpline = drive.trajectoryBuilder(launchSpline.end())
                .forward(12)
                .build();
        Trajectory fourSpline = drive.trajectoryBuilder(launchSpline.end())
                .splineToConstantHeading(new Vector2d(33,-68),0)
                .build();
        Trajectory fourBack = drive.trajectoryBuilder(fourSpline.end())
                .back(30)
                .build();

        Trajectory noSquare = drive.trajectoryBuilder(launchSpline.end().plus(new Pose2d(0,0,Math.toRadians(-60))))
                .forward(4)
                .build();
        Trajectory noRings = drive.trajectoryBuilder(noSquare.end().plus(new Pose2d(0,0,Math.toRadians(-120))))
                .splineToConstantHeading(new Vector2d(-36.50,-46.50),180)
                .addDisplacementMarker(() -> drive.wobbleGrab.setPower(1))
                .build();
        telemetry.setDebug("Achievable RPM Fraction",goBildaOuttake.getAchieveableMaxRPMFraction());
        telemetry.setDebug("Achievable Ticks Per Second",goBildaOuttake.getAchieveableMaxTicksPerSecond());
        telemetry.setDebug("hub velocity params",goBildaOuttake.getHubVelocityParams());
        telemetry.setDebug("velocity params",goBildaOuttake.hasExpansionHubVelocityParams());
        telemetry.setDebug("outtakeVelocity",outTake75Speed);


        waitForStart();


        drive.followTrajectory(launchSpline);

        while(drive.isBusy()){
            telemetry.setDebug("Pose",drive.getPoseEstimate().toString());
        }

        /*drive.followTrajectory(strafeRight);

        while(drive.isBusy()){
            telemetry.setDebug("Pose",drive.getPoseEstimate().toString());
        }

        drive.outtake.setVelocity(outTake75Speed, AngleUnit.RADIANS);

        while (opModeIsActive() && Math.abs(drive.outtake.getVelocity(AngleUnit.RADIANS)-outTake75Speed) > Math.pow(10,-1)){
            telemetry.setDebug("Outtake Velocity",drive.outtake.getVelocity(AngleUnit.RADIANS));
            telemetry.setDebug("Ticks Outtake Velocity",drive.outtake.getVelocity(AngleUnit.DEGREES));
            telemetry.setDebug("Ticks Outtake Position",drive.outtake.getCurrentPosition());

        }

        int ringsFired = 0;

        while (opModeIsActive() && ringsFired <= 5) {
            telemetry.setDebug("ringsFired",ringsFired);
            drive.kicker.setPower(1);
            sleep(200);
            drive.kicker.setPower(-1);
            sleep(200);
            drive.kicker.setPower(0);
            while (Math.abs(drive.outtake.getVelocity(AngleUnit.RADIANS)-outTake75Speed) > Math.pow(10,-1)){
                telemetry.setDebug("Outtake Velocity",drive.outtake.getVelocity(AngleUnit.RADIANS));
            }
            ringsFired++;
        }
        drive.outtake.setVelocity(0, AngleUnit.RADIANS);

        while (opModeIsActive() && Math.abs(drive.outtake.getVelocity(AngleUnit.RADIANS)) > Math.pow(10,-1)){
            telemetry.setDebug("Outtake Velocity",drive.outtake.getVelocity(AngleUnit.RADIANS));
            telemetry.setDebug("Ticks Outtake Velocity",drive.outtake.getVelocity(AngleUnit.DEGREES));
            telemetry.setDebug("Ticks Outtake Position",drive.outtake.getCurrentPosition());

        }
        if(ringStack == 0) {
            drive.turn(Math.toRadians(-60));
            drive.followTrajectory(noSquare);
        } else if (ringStack == 1){
            drive.followTrajectory(oneSpline);
        } else if(ringStack == 4){
            drive.followTrajectory(fourSpline);
        }
        drive.liftWobble(-110,.45,AngleUnit.DEGREES,1500);
        drive.wobbleGrab.setPower(-1);
        sleep(300);
        drive.wobbleGrab.setPower(0);
        sleep(200);
        if(ringStack == 4){
            drive.followTrajectory(fourBack);
        } else if(ringStack == 0){
            drive.turn(Math.toRadians(-120));
            while (drive.isBusy()){}
            drive.followTrajectory(noRings);
            while (drive.isBusy()){}
            sleep(300);
            drive.liftWobble(30,.45,AngleUnit.DEGREES,1500);
            PoseStorage.currentPose = drive.getPoseEstimate();
        }
    }
   /* private int getZoneFromOpenCV() {
        semaphore.waitForFrame();
        return stackDetector.getLastStackHeight();

    }
}*/