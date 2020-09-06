package org.hermitsocialclub.pandemicpanic.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.hermitsocialclub.hydra.vision.DistanceToObjectDetector;
import org.opencv.core.Mat;
import org.openftc.easyopencv.*;

@TeleOp(name = "Contour Test")
public class ContourTestOp extends LinearOpMode {

    @Override
    public void runOpMode() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        OpenCvCamera phoneCam = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        phoneCam.openCameraDevice();

        phoneCam.setPipeline(new SamplePipeline());
        phoneCam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);

        waitForStart();

        while (opModeIsActive()) {
            telemetry.addData("Frame Count", phoneCam.getFrameCount());
            telemetry.addData("FPS", String.format("%.2f", phoneCam.getFps()));
            telemetry.addData("Total frame time ms", phoneCam.getTotalFrameTimeMs());
            telemetry.addData("Pipeline time ms", phoneCam.getPipelineTimeMs());
            telemetry.addData("Overhead time ms", phoneCam.getOverheadTimeMs());
            telemetry.addData("Theoretical max FPS", phoneCam.getCurrentPipelineMaxFps());
            telemetry.update();

            if (gamepad1.a) {
                phoneCam.stopStreaming();
            } else if (gamepad1.x) {
                phoneCam.pauseViewport();
            } else if (gamepad1.y) {
                phoneCam.resumeViewport();
            }

            sleep(100);
        }
    }

    private static class SamplePipeline extends OpenCvPipeline {

        private final DistanceToObjectDetector distance2Obj = new DistanceToObjectDetector();

        @Override
        public Mat processFrame(Mat input) {
            return distance2Obj.apply(input);
        }

    }

}