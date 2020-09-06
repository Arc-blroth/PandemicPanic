package org.firstinspires.ftc.teamcode.drive.opmode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.config.TrajectoryGroupConfig;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.vision.SkystoneVuforiaEngine;
import org.hermitsocialclub.telecat.PersistantTelemetry;

@Autonomous(name = "Kuai Attempts Road Runner")
public class KuaiAttemptsRoadrunner extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        PersistantTelemetry telemetry = new PersistantTelemetry(super.telemetry);
        SkystoneVuforiaEngine vuforiaEngine = SkystoneVuforiaEngine.get(telemetry);
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap, vuforiaEngine);
        TrajectoryGroupConfig tgc = new TrajectoryGroupConfig(
                drive.constraints.maxVel,
                drive.constraints.maxAccel,
                drive.constraints.maxAngVel,
                drive.constraints.maxAngAccel,
                15, 10.75,
                TrajectoryGroupConfig.DriveType.MECANUM,
                DriveConstants.TRACK_WIDTH,
                DriveConstants.TRACK_WIDTH,
                1.0);

        Trajectory t1 = drive.trajectoryBuilder(new Pose2d(38, -64, -90)).splineTo(new Pose2d(0, -64)).build();

        waitForStart();
        drive.followTrajectoryAsync(t1);
        drive.waitForIdle();
    }

}
