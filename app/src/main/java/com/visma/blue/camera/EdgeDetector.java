package com.visma.blue.camera;

import android.util.Pair;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anders.fagerholt on 24.02.2017.
 */

public class EdgeDetector {

    public static Mat resizeAndDraw (Mat mat) {
        int maxSize = 300;
        int h = mat.height();
        int w = mat.width();
        double scale = 1;
        System.out.println("h = "+h);
        System.out.println("w = "+w);

        if (h > maxSize) {
            scale = maxSize / (double) h;
            h = maxSize;
            w = (int) (w * scale);
        }
        if (w > maxSize) {
            scale = maxSize / (double) w;
            w = maxSize;
            h = (int) (h * scale);
        }

        scale = 1 / scale;

        System.out.println(scale);

        Mat miniMat = new Mat();
        Imgproc.resize(mat, miniMat, new Size(w, h));

        System.out.println("minimat = "+miniMat.height() + " " + miniMat.width());

        Mat edges = new Mat();
        Imgproc.Canny(miniMat, edges, 50, 300);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180, 20, 30, 3);

        Point[][] linesList = new Point[lines.rows()][0];
        System.out.println("cols: "+lines.cols());
        System.out.println("rows: "+lines.rows());

        for (int i = 0; i < lines.rows(); i++) {
            System.out.println("line "+i+" found in rows.");
            double[] vec = lines.get(i, 0);
            linesList[i] = new Point[]{new Point(), new Point()};

            linesList[i][0].x = vec[0] * scale;
            linesList[i][0].y = vec[1] * scale;
            linesList[i][1].x = vec[2] * scale;
            linesList[i][1].y = vec[3] * scale;
            System.out.println(linesList[i][1]);
        }
        for (Point[] line : linesList) {
            Imgproc.line(mat, line[0], line[1], new Scalar(255, 0, 0), 3);
        }
        return mat;
    }

    public static Mat getEdges (Mat mat, double scale, int step) {
        //First step: Invert. Just to test.
        //Core.bitwise_not(mat, mat);
        if (step == 0) {
            return mat;
        }

        System.out.println("Bitwise_not: "+mat.type());

        //Imgproc.rectangle(mat, new Point(200, 200), new Point(mat.width()-200, mat.height()-200), new Scalar(255, 255, 255));

        //Second step: Detect edges.
        Mat edges = new Mat();
        Imgproc.Canny(mat, edges, 50, 300);
        if (step == 1) {
            return edges;
        }

        //Conditional third step: floodfilling takes time.
        if (step == 2) {
            //Create points for drawing rectangle
            Point leftTop = new Point(edges.width() / 2 - 5, edges.height() / 2 - 5);
            Point rightBottom = new Point(edges.width() / 2 + 5, edges.height() / 2 + 5);

            //Draw rectangle to ensure floodfilling has room to spread
            Core.bitwise_not(edges, edges);
            Imgproc.rectangle(edges, leftTop, rightBottom, new Scalar(255), -1);
            Imgproc.floodFill(edges, new Mat(), new Point(edges.width() / 2, edges.height() / 2), new Scalar(128));
            return edges;
        }

        if (step == 3) {

            Mat houghP = new Mat();
            Imgproc.HoughLinesP(edges, houghP, 1, Math.PI/180, 30, 20, 5);
            Pair<Point, Point>[] lines = new Pair[houghP.width()];
            List<Pair<Pair<Point, Point>, Double>> selectedLines = new ArrayList<>();
            for (int x = 0; x < houghP.cols(); x++) {
                double[] vec = houghP.get(0, x);
                double x1 = vec[0],
                        y1 = vec[1],
                        x2 = vec[2],
                        y2 = vec[3];
                Point start = new Point(x1, y1);
                Point end = new Point(x2, y2);
                lines[x] = new Pair<>(start, end);
                double angle = Math.abs(Math.atan2(end.y - start.y, end.x - start.x) * 180.0 / Math.PI);
                selectedLines.add(new Pair<Pair<Point, Point>, Double>(lines[x], angle));
                System.out.println("cloning");
                Mat h = mat.clone();

                for (int i = 0; i < selectedLines.size(); ++i) {
                    Pair<Pair<Point, Point>, Double> line = selectedLines.get(i);
                    Imgproc.line(h, line.first.first, line.first.second, new Scalar(0, 255, 0), 2);
                }
                System.out.println("returning h");
                return h;
            }
        }

        if (step == 4) {
            Mat lines = new Mat();
            Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180, 30, 20, 5);
            Point[][] linesList = new Point[lines.rows()][0];
            for (int i = 0; i < lines.rows(); i++) {
                double[] vec = lines.get(i, 0);
                linesList[i] = new Point[]{new Point(), new Point()};

                linesList[i][0].x = vec[0] * scale;
                linesList[i][0].y = vec[1] * scale;
                linesList[i][1].x = vec[2] * scale;
                linesList[i][1].y = vec[3] * scale;
            }
            for (Point[] line : linesList) {
                System.out.println("line x = "+line[0].x);
                Imgproc.line(edges, line[0], line[1], new Scalar(0, 0, 255), 1);
            }
            return edges;
        }
        return edges;

    }

}
