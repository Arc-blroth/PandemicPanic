package org.hermitsocialclub.hydra.vision

import org.hermitsocialclub.hydra.vision.util.VisionUtils.zero
import org.opencv.core.Core.*
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc.*
import kotlin.math.max

/**
 * # StaccDetecc
 * Based on the python implementation [here](https://gist.github.com/Arc-blroth/b091121c41543cbdb5d92043cbeca6d2)
 *
 * @author Arc'blroth
 */
class StaccDetecc @JvmOverloads constructor(val config: StaccConfig = StaccConfig()) : IVisionPipelineComponent {

    class StaccConfig {
        /**
         * 300 for extremely bright light conditions and 1000 for extremely dark light conditions.
         */
        var normUpper = 350.0

        /**
         * "Lower" HSV color to look for.
         */
        var lowerYellow = Scalar(0.0, 135.0, 180.0)

        /**
         * "Upper" HSV color to look for.
         */
        var upperYellow = Scalar(46.0, 255.0, 255.0)

        /**
         * Pixel regions with an area smaller than this are discarded by the algorithm.
         */
        var minStackArea = 512

        /**
         * Pixel regions with a ratio greater than this are discarded by the algorithm.
         */
        var maxStackRatio = Int.MAX_VALUE

        /**
         * If more than this number of pixel regions are detected, we assume there is
         * no stack in the image and that the algorithm is probably picking up on the floor.
         */
        var maxStackNoise = 100

        /**
         * Stacks with a ratio less than this are considered one-ring stacks.
         * Stacks with a ratio greater than this are considered four-ring stacks.
         */
        var oneStackRatio = 0.4
    }

    /**
     * The last stack height we detected. Either 0, 1, or 4.
     */
    var lastStackHeight = 0
        private set

    override fun apply(image: Mat, pipeline: VisionPipeline): Mat {
        // Filter image for a certain color
        val hsvImg = zero(image)
        cvtColor(image, hsvImg, COLOR_RGB2HSV)
        normalize(hsvImg, hsvImg, 0.0, config.normUpper, NORM_MINMAX)
        val colorFilter = zero(hsvImg)
        inRange(hsvImg, config.lowerYellow, config.upperYellow, colorFilter)

        // Find largest square area in image
        try {
            val fsout = findStacc(image, colorFilter)
            if (fsout != null) {
                val ratio = fsout.height.toDouble() / fsout.width.toDouble()
                this.lastStackHeight = if (ratio < config.oneStackRatio) 1 else 4
                pipeline.telemetry.setData("Stacc ratio", ratio)
                pipeline.telemetry.setData("Stacc found", "true [${lastStackHeight}]")
                rectangle(image, fsout, Scalar(0.0, 255.0, 0.0), 3)
            } else {
                this.lastStackHeight = 0
                pipeline.telemetry.removeData("Stacc ratio")
                pipeline.telemetry.setData("Stacc found", "false")
            }
        } catch (ex: NullPointerException) {
            this.lastStackHeight = 0
            pipeline.telemetry.removeData("Stacc ratio")
            pipeline.telemetry.setData("Stacc found", "false (NPE)")
        }

        return image
    }

    private fun findStacc(image: Mat, filter: Mat): Rect? {
        filter.convertTo(filter, CvType.CV_8U)
        val labels = Mat()
        val stats = Mat()
        val centroids = Mat()
        val nbComponents = connectedComponentsWithStats(filter, labels, stats, centroids, 4)

        if (nbComponents > config.maxStackNoise) {
            return null
        }

        var maxLabel: Int = -1
        var maxSize: Int = config.minStackArea
        val areaBuffer = intArrayOf(1)
        val widthBuffer = intArrayOf(1)
        val heightBuffer = intArrayOf(1)
        for (i in 2..nbComponents) {
            stats[i, CC_STAT_AREA, areaBuffer]
            if (areaBuffer[0] > maxSize) {
                stats[i, CC_STAT_WIDTH, widthBuffer]
                stats[i, CC_STAT_HEIGHT, heightBuffer]
                if (max(widthBuffer[0] / heightBuffer[0], heightBuffer[0] / widthBuffer[0]) < config.maxStackRatio) {
                    maxLabel = i
                    maxSize = areaBuffer[0]
                }
            }
        }

        return if (maxLabel == -1) {
            null
        } else {
            val leftBuffer = intArrayOf(1)
            val topBuffer = intArrayOf(1)
            stats[maxLabel, CC_STAT_LEFT, leftBuffer]
            stats[maxLabel, CC_STAT_TOP, topBuffer]
            stats[maxLabel, CC_STAT_WIDTH, widthBuffer]
            stats[maxLabel, CC_STAT_HEIGHT, heightBuffer]
            Rect(
                leftBuffer[0],
                topBuffer[0],
                widthBuffer[0],
                heightBuffer[0],
            )
        }
    }

}