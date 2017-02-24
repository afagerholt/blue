package com.visma.blue.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by anders.fagerholt on 24.02.2017.
 */

public class EdgeDetector {

    public static Mat getEdges (Mat mat, int step) {
        //First step: Invert. Just to test.
        Core.bitwise_not(mat, mat);
        if (step == 0) {
            return mat;
        }

        System.out.println("Bitwise_not: "+mat.type());

        //Imgproc.rectangle(mat, new Point(200, 200), new Point(mat.width()-200, mat.height()-200), new Scalar(255, 255, 255));

        //Second step: Detect edges.
        Mat edges = new Mat();
        Imgproc.Canny(mat, edges, 50, 200);
        if (step == 1) {
            return edges;
        }

        Point leftTop = new Point(edges.width()/2 - 5, edges.height()/2 - 5);
        Point rightBottom = new Point(edges.width()/2 + 5, edges.height()/2 + 5);

        Imgproc.rectangle(edges, leftTop, rightBottom, new Scalar(255), -1);

        return edges;
    }

    public static int getLines(Mat mat) {
        Mat edges = new Mat();
        Imgproc.Canny(mat, edges, 50, 300);
        return edges.rows();
    }
}
