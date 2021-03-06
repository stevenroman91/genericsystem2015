package org.genericsystem.ir;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.genericsystem.cv.Classifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ClassifierVerticle extends ActionVerticle {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String ACTION = "classification";

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	protected void handle(Future<Object> future, JsonObject task) {
		Path imgPath = Paths.get(DistributedVerticle.BASE_PATH + task.getString(DistributedVerticle.FILENAME));
		logger.info("Starting classification for {}", imgPath.toAbsolutePath().normalize());
		Path savedFile;
		synchronized (ClassifierVerticle.class) {
			savedFile = Classifier.classify(Paths.get(DistributedVerticle.BASE_PATH + "classes/"), imgPath);
		}
		if (savedFile != null)
			future.complete(savedFile.toString());
		else
			future.fail("Impossible to classify image.");
	}

	@Override
	protected void handleResult(AsyncResult<Object> res, JsonObject task) {
		if (res.succeeded()) {
			Path path = Paths.get((String) res.result());
			Path relative = Paths.get(DistributedVerticle.BASE_PATH).relativize(path);
			addTask(relative.toString(), AddImageToEngineVerticle.ACTION);
		} else
			throw new IllegalStateException("Error when classifying the image " + task.getString(DistributedVerticle.FILENAME), res.cause());
	}
}
