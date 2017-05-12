package org.genericsystem.cv;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Img {
	private final Mat src = new Mat();

	public Mat getSrc() {
		return src;
	}

	public Img(Mat src) {
		src.copyTo(this.src);
	}

	public Img sobel(int ddepth, int dx, int dy, int ksize, double scale, double delta, int borderType) {
		Mat result = new Mat();
		Imgproc.Sobel(src, result, ddepth, dx, dy, ksize, scale, delta, borderType);
		return new Img(result);
	}

	public Img thresHold(double thresh, double maxval, int type) {
		Mat result = new Mat();
		Imgproc.threshold(src, result, thresh, maxval, type);
		return new Img(result);
	}

	public Img morphologyEx(int morphOp, StructuringElement structuringElement) {
		Mat result = new Mat();
		Imgproc.morphologyEx(src, result, morphOp, structuringElement.getSrc());
		return new Img(result);
	}

	public List<MatOfPoint> findContours(Img[] hierarchy, int mode, int method) {
		Mat mat = new Mat();
		List<MatOfPoint> result = new ArrayList<>();
		Imgproc.findContours(src, result, mat, mode, method);
		hierarchy[0] = new Img(mat);
		return result;
	}

	public List<MatOfPoint> findContours(Img[] hierarchy, int mode, int method, Point point) {
		Mat mat = new Mat();
		List<MatOfPoint> result = new ArrayList<>();
		Imgproc.findContours(src, result, mat, mode, method, point);
		hierarchy[0] = new Img(mat);
		return result;
	}

	public Img canny(double threshold1, double threshold2) {
		Mat result = new Mat();
		Imgproc.Canny(src, result, threshold1, threshold2);
		return new Img(result);
	}

	public Size size() {
		return src.size();
	}

	public int height() {
		return src.height();
	}

	public int width() {
		return src.width();
	}

	public double[] get(int row, int col) {
		return src.get(row, col);
	}

	public Img cvtColor(int code) {
		Mat result = new Mat();
		Imgproc.cvtColor(src, result, code);
		return new Img(result);
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(Tools.mat2bufferedImage(src));
	}

	public void rectangle(Rect rect, Scalar color, int thickNess) {
		Imgproc.rectangle(src, rect.br(), rect.tl(), color, thickNess);
	}

	public Node getImageView() {
		Mat conv = new Mat();
		src.convertTo(conv, CvType.CV_8UC1);
		Mat target = new Mat();
		Imgproc.resize(conv, target, new Size(AbstractApp.displayWidth, Math.floor((AbstractApp.displayWidth / conv.width()) * conv.height())));
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", target, buffer);
		ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(buffer.toArray())));
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(AbstractApp.displayWidth);
		return imageView;
	}

	public int channels() {
		return src.channels();
	}

	public Img range(Scalar scalar, Scalar scalar2, boolean hsv) {
		Img ranged = this;
		if (hsv)
			ranged = ranged.cvtColor(Imgproc.COLOR_BGR2HSV);
		Mat result = new Mat(ranged.size(), ranged.type(), new Scalar(0, 0, 0));
		Mat mask = new Mat();
		Core.inRange(ranged.getSrc(), scalar, scalar2, mask);
		ranged.getSrc().copyTo(result, mask);
		Img resultImg = new Img(result);
		if (hsv)
			resultImg = resultImg.cvtColor(Imgproc.COLOR_HSV2BGR);
		return resultImg;
	}

	public int type() {
		return src.type();
	}

	public Img gaussianBlur(Size size) {
		Mat result = new Mat();
		Imgproc.GaussianBlur(src, result, size, 0);
		return new Img(result);
	}
}
