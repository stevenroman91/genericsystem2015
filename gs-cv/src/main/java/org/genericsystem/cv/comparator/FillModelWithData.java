package org.genericsystem.cv.comparator;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.genericsystem.common.Generic;
import org.genericsystem.common.Root;
import org.genericsystem.cv.Img;
import org.genericsystem.cv.Zone;
import org.genericsystem.cv.Zones;
import org.genericsystem.cv.model.Doc;
import org.genericsystem.cv.model.Doc.DocFilename;
import org.genericsystem.cv.model.Doc.DocInstance;
import org.genericsystem.cv.model.Doc.DocTimestamp;
import org.genericsystem.cv.model.Doc.RefreshTimestamp;
import org.genericsystem.cv.model.DocClass;
import org.genericsystem.cv.model.DocClass.DocClassInstance;
import org.genericsystem.cv.model.ImgFilter;
import org.genericsystem.cv.model.ImgFilter.ImgFilterInstance;
import org.genericsystem.cv.model.LevDistance;
import org.genericsystem.cv.model.MeanLevenshtein;
import org.genericsystem.cv.model.ModelTools;
import org.genericsystem.cv.model.Score;
import org.genericsystem.cv.model.Score.ScoreInstance;
import org.genericsystem.cv.model.ZoneGeneric;
import org.genericsystem.cv.model.ZoneGeneric.ZoneInstance;
import org.genericsystem.cv.model.ZoneText;
import org.genericsystem.cv.model.ZoneText.ZoneTextInstance;
import org.genericsystem.cv.model.ZoneText.ZoneTimestamp;
import org.genericsystem.kernel.Engine;
import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * The FillModelWithData class can analyze an image (or a batch of images) and store all the OCR text for each zone and each document in GS.
 * 
 * @author Pierrik Lassalas
 */
public class FillModelWithData {

	public static final String ENCODED_FILENAME = "encodedFilename";
	public static final String FILENAME = "filename";
	public static final String CLASS_NAME = "docType";
	public static final String DOC_TIMESTAMP = "docTimestamp";
	public static final String ZONE = "zone";
	public static final String ZONES = "zones";

	public static final int ERROR = 0;
	public static final int NEW_FILE = 1;
	public static final int KNOWN_FILE = 2;
	public static final int KNOWN_FILE_UPDATED_FILTERS = 3;

	private static Logger log = LoggerFactory.getLogger(FillModelWithData.class);
	private static final String gsPath = System.getenv("HOME") + "/genericsystem/gs-cv_model3/";
	private static final String docType = "id-fr-front";

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		log.info("OpenCV core library loaded");
	}

	public static void main(String[] mainArgs) {
		final Root engine = getEngine();
		engine.newCache().start();
		compute(engine);
		// cleanModel(engine);
		engine.close();
	}

	public static Root getEngine() {
		return new Engine(gsPath, Doc.class, RefreshTimestamp.class, DocTimestamp.class, DocFilename.class, DocClass.class, ZoneGeneric.class, ZoneText.class, ZoneTimestamp.class, ImgFilter.class, LevDistance.class, MeanLevenshtein.class, Score.class);
	}

	/**
	 * This Map will contain the names of the filters that will be applied to a specified {@link Img}.
	 * 
	 * @return - a Map containing the filter names as key, and a {@link Function} that will apply the specified algorithm to an Img.
	 */
	@Deprecated
	public static Map<String, ImgFunction> getFiltersMap() {
		final Map<String, ImgFunction> map = new ConcurrentHashMap<>();
		map.put("original", i -> i);
		map.put("reality", i -> i);
		// map.put("bernsen",Img::bernsen);
		map.put("equalizeHisto", i -> i.equalizeHisto());
		map.put("equalizeHistoAdaptative", i -> i.equalizeHistoAdaptative());
		map.put("otsuAfterGaussianBlur", i -> i.otsuAfterGaussianBlur());
		map.put("adaptativeGaussianThreshold", i -> i.adaptativeGaussianThreshold());
		return map;
	}

	/**
	 * This List contains all the functions defined in {@link ImgFilterFunction}.
	 * 
	 * @return a list of {@link ImgFilterFunction}
	 */
	public static List<ImgFilterFunction> getFilterFunctions() {
		final List<ImgFilterFunction> filterSet = new ArrayList<>();
		for (ImgFilterFunction iff : ImgFilterFunction.values()) {
			log.info("Adding: {}", iff);
			filterSet.add(iff);
		}
		return filterSet;
	}

	/**
	 * Check if a given file has already been processed by the system. This verification is conducted by comparing the SHA-256 hash code generated from the file and the one stored in Generic System. If there is a match, the file is assumed to be known.
	 * 
	 * @param engine - the engine used to store the data
	 * @param file - the desired file
	 * @return - true if the file was not found in the engine, false if it has already been processed
	 */
	private static boolean isThisANewFile(Root engine, File file) {
		return isThisANewFile(engine, file, docType);
	}

	/**
	 * Check if a given file has already been processed by the system. This verification is conducted by comparing the SHA-256 hash code generated from the file and the one stored in Generic System. If there is a match, the file is assumed to be known.
	 * 
	 * @param engine - the engine used to store the data
	 * @param file - the desired file
	 * @param docType - {@code String} representing the type of document (i.e., class)
	 * @return - true if the file was not found in the engine, false if it has already been processed
	 */
	private static boolean isThisANewFile(Root engine, File file, String docType) {
		Generic doc = engine.find(Doc.class);
		DocClass docClass = engine.find(DocClass.class);
		DocClassInstance docClassInstance = docClass.getDocClass(docType);
		String filenameExt = ModelTools.generateFileName(file.toPath());
		if (null == filenameExt) {
			log.error("An error has occured during the generation of the hascode from file (assuming new)");
			return true;
		} else {
			DocInstance docInstance = docClassInstance.getDoc(doc, filenameExt);
			return null == docInstance ? true : false;
		}
	}

	/**
	 * Collect all the informations required to process a given file through OCR.
	 * 
	 * @param engine - the engine used to store the data
	 * @param imagePath - the {@link Path} of the image to proceed
	 * @return a {@link JsonObject} containing all the informations required to process the file
	 */
	public static JsonObject getOcrParameters(Root engine, Path imagePath) {
		try {
			engine.getCurrentCache();
		} catch (IllegalStateException e) {
			log.error("Current cache could not be loaded. Starting a new one...");
			engine.newCache().start();
		}
		final Path imgClassDirectory = imagePath.getParent();
		final String docType = ModelTools.getImgClass(imagePath);

		// Find the generics
		DocClass docClass = engine.find(DocClass.class);
		Doc doc = engine.find(Doc.class);
		ZoneText zoneText = engine.find(ZoneText.class);
		ImgFilter imgFilter = engine.find(ImgFilter.class);

		// Find and save the doc class and the doc instance
		DocClassInstance docClassInstance = docClass.setDocClass(docType);
		DocInstance docInstance = docClassInstance.setDoc(doc, ModelTools.generateFileName(imagePath));
		engine.getCurrentCache().flush();

		// Get the filters and the predefined zones
		final List<ImgFilterFunction> imgFilterFunctions = FillModelWithData.getFilterFunctions();
		final Zones zones = Zones.load(imgClassDirectory.toString());

		// Save the zones if necessary
		zones.getZones().forEach(z -> {
			ZoneInstance zoneInstance = docClassInstance.getZone(z.getNum());
			if (zoneInstance != null) {
				Zone zone = zoneInstance.getZoneObject();
				// log.info("z : {} ; zone : {}", z, zone);
				if (z.equals(zone)) {
					log.info("Zone n°{} already known", z.getNum());
				} else {
					log.info("Adding zone n°{} ", z.getNum());
					docClassInstance.setZone(z.getNum(), z.getRect().x, z.getRect().y, z.getRect().width, z.getRect().height);
				}
			} else {
				log.info("Adding zone n°{} ", z.getNum());
				docClassInstance.setZone(z.getNum(), z.getRect().x, z.getRect().y, z.getRect().width, z.getRect().height);
			}
		});
		engine.getCurrentCache().flush();

		// Save the filternames if necessary
		final List<ImgFilterFunction> updatedImgFilterList = new ArrayList<>();
		imgFilterFunctions.forEach(entry -> {
			String filtername = entry.getName();
			ImgFilterInstance filter = imgFilter.getImgFilter(filtername);
			if (filter == null) {
				log.info("Adding algorithm : {} ", filtername);
				imgFilter.setImgFilter(filtername);
				updatedImgFilterList.add(entry);
			} else {
				// TODO: add another criteria to verify if the filter has been applied on the image
				boolean containsNullZoneTextInstance = zones.getZones().stream().anyMatch(z -> {
					ZoneTextInstance zti = zoneText.getZoneText(docInstance, docClassInstance.getZone(z.getNum()), filter);
					return zti == null;
				});
				if (containsNullZoneTextInstance) {
					imgFilter.setImgFilter(filtername);
					updatedImgFilterList.add(entry);
				} else {
					log.debug("Algorithm {} already known", filtername);
				}
			}
		});

		if (null == updatedImgFilterList || updatedImgFilterList.isEmpty()) {
			log.info("Nothing to add");
			return new JsonObject();
		} else {
			// Return the parameters required to process this file as a JsonObject
			OcrParameters params = new OcrParameters(imagePath.toFile(), zones, updatedImgFilterList);
			return params.toJson();
		}
	}

	/**
	 * Process a given file through OCR. All the necessary parameters are retrieved from the {@code params} argument. The results are stored in a {@link JsonObject}.
	 * 
	 * @param params - the {@link OcrParameters} as a {@link JsonObject}
	 * @return a {@link JsonObject} containing all the data from the OCR
	 */
	public static JsonObject processFile(JsonObject params) {
		// Get all necessary parameters from the JsonObject
		OcrParameters ocrParameters = new OcrParameters(params);
		File file = ocrParameters.getFile();
		Zones zones = ocrParameters.getZones();
		List<ImgFilterFunction> updatedImgFilterList = ocrParameters.getImgFilterFunctions();

		// Save the current file
		log.info("\nProcessing file: {}", file.getName());
		String filenameExt = ModelTools.generateFileName(file.toPath());
		if (null == filenameExt)
			throw new RuntimeException("An error has occured while saving the file! Aborted...");
		final Path imgClassDirectory = file.toPath().getParent();
		final String docType = imgClassDirectory.getName(imgClassDirectory.getNameCount() - 1).toString();

		// Create a JsonObject for the answer
		JsonObject jsonObject = new JsonObject();
		jsonObject.put(CLASS_NAME, docType);
		jsonObject.put(FILENAME, file.getName());
		jsonObject.put(ENCODED_FILENAME, filenameExt);
		jsonObject.put(DOC_TIMESTAMP, ModelTools.getCurrentDate());

		// Create a map of Imgs
		Map<String, Img> imgs = new ConcurrentHashMap<>();
		Img originalImg = new Img(file.getPath());
		updatedImgFilterList.forEach(entry -> {
			String filtername = entry.getName();
			ImgFunction function = entry.getLambda();

			log.info("Applying algorithm {}...", filtername);
			Img img = null;
			if ("original".equals(filtername) || "reality".equals(filtername))
				img = originalImg;
			else
				img = function.apply(originalImg);
			if (null != img)
				imgs.put(filtername, img);
			else
				log.error("An error as occured for image {} and filter {}", filenameExt, filtername);
		});

		// Process each zone
		Map<String, Map<String, String>> result = new ConcurrentHashMap<>();
		zones.getZones().forEach(z -> {
			log.info("Zone n° {}", z.getNum());
			Map<String, String> map = new ConcurrentHashMap<>();
			imgs.entrySet().forEach(entry -> {
				if ("reality".equals(entry.getKey()) || "best".equals(entry.getKey())) {
					// Do nothing
				} else {
					String ocrText = z.ocr(entry.getValue());
					map.put(entry.getKey(), ocrText);
				}
			});
			result.put(String.valueOf(z.getNum()), map);
		});
		jsonObject.put(ZONES, result);

		// Close the images to force freeing OpenCV's resources (native matrices)
		originalImg.close();
		imgs.entrySet().forEach(entry -> entry.getValue().close());

		return jsonObject;
	}

	/**
	 * Save the OCR data into Generic System.
	 * 
	 * @param engine - the engine used to store the data
	 * @param data - a {@link JsonObject} containing all the data (see {@link #getOcrParameters(Root, Path)}).
	 */
	public static void saveOcrDataInModel(Root engine, JsonObject data) {
		try {
			engine.getCurrentCache();
		} catch (IllegalStateException e) {
			log.error("Current cache could not be loaded. Starting a new one...");
			engine.newCache().start();
		}
		// Parse the data
		String docType = data.getString(CLASS_NAME);
		String filename = data.getString(FILENAME);
		String filenameExt = data.getString(ENCODED_FILENAME);
		Long timestamp = data.getLong(DOC_TIMESTAMP);
		JsonObject zones = data.getJsonObject(ZONES);

		// Get the generics
		DocClass docClass = engine.find(DocClass.class);
		Doc doc = engine.find(Doc.class);
		ZoneText zoneText = engine.find(ZoneText.class);
		ImgFilter imgFilter = engine.find(ImgFilter.class);

		// Set the docClass, doc instance and timestamp
		DocClassInstance docClassInstance = docClass.setDocClass(docType);
		DocInstance docInstance = docClassInstance.setDoc(doc, filenameExt);
		docInstance.setDocFilename(filename);
		docInstance.setDocTimestamp(timestamp);
		engine.getCurrentCache().flush();

		zones.forEach(entry -> {
			log.info("Current zone: {}", entry.getKey());
			ZoneInstance zoneInstance = docClassInstance.getZone(Integer.parseInt(entry.getKey(), 10));
			JsonObject currentZone = (JsonObject) entry.getValue();
			if (!currentZone.isEmpty())
				currentZone.put("reality", ""); // Add this filter only if there are other filters
			currentZone.forEach(e -> {
				log.debug("key: {};  value: {}", e.getKey(), e.getValue().toString());
				if ("reality".equals(e.getKey()) || "best".equals(e.getKey())) {
					// Do not proceed to OCR if the real values are known. By default, the "reality" and "best" filters are left empty
					if (null == zoneText.getZoneText(docInstance, zoneInstance, imgFilter.getImgFilter(e.getKey())))
						zoneText.setZoneText("", docInstance, zoneInstance, imgFilter.getImgFilter(e.getKey()));
				} else {
					String ocrText = (String) e.getValue();
					ZoneTextInstance zti = zoneText.setZoneText(ocrText, docInstance, zoneInstance, imgFilter.getImgFilter(e.getKey()));
					zti.setZoneTimestamp(ModelTools.getCurrentDate()); // TODO: concatenate with previous line?
				}
			});
			engine.getCurrentCache().flush();
		});
		log.info("Data for {} successfully saved.", filenameExt);
	}

	/**
	 * Process an image, and store all the informations in the engine of Generic System. When no Engine is provided, a default one is created.
	 * 
	 * @param imagePath - a {@link Path} object pointing to the image to be processed
	 * @return an {@code int} representing {@link #KNOWN_FILE_UPDATED_FILTERS}, {@link #NEW_FILE} or {@link #KNOWN_FILE}
	 */
	public static int doImgOcr(Path imagePath) {
		final Root engine = getEngine();
		engine.newCache().start();
		int result = doImgOcr(engine, imagePath);
		engine.close();
		return result;
	}

	/**
	 * Process an image, and store all the informations in the engine of Generic System.
	 * 
	 * @param engine - the engine used to store the data
	 * @param imagePath - a {@link Path} object pointing to the image to be processed
	 * @return an {@code int} representing {@link #KNOWN_FILE_UPDATED_FILTERS}, {@link #NEW_FILE} or {@link #KNOWN_FILE}
	 */
	public static int doImgOcr(Root engine, Path imagePath) {
		try {
			engine.getCurrentCache();
		} catch (IllegalStateException e) {
			log.error("Current cache could not be loaded. Starting a new one...");
			engine.newCache().start();
		}
		final Path imgClassDirectory = imagePath.getParent();
		final String docType = ModelTools.getImgClass(imagePath);
		int result = ERROR;

		// Find and save the doc class
		DocClass docClass = engine.find(DocClass.class);
		DocClassInstance docClassInstance = docClass.setDocClass(docType);
		engine.getCurrentCache().flush();

		// Get the filters and the predefined zones
		final Map<String, ImgFunction> imgFilters = getFiltersMap();
		final Zones zones = Zones.loadZones(imgClassDirectory.toString());

		// Process the image file
		initComputation(engine, docType, zones);
		result = processFile(engine, imagePath.toFile(), docClassInstance, zones, imgFilters.entrySet().stream());
		return result;
	}

	/**
	 * Process all the images in the specified folder, and store all the data in Generic System. The docType is set to the default value.
	 * 
	 * @param engine - the engine used to store the data
	 */
	public static void compute(Root engine) {
		compute(engine, docType);
	}

	/**
	 * Process all the images in the specified folder, and store all the data in Generic System.
	 * 
	 * @param engine - the engine used to store the data
	 * @param docType - {@code String} representing the type of document (i.e., class)
	 */
	public static void compute(Root engine, String docType) {
		final String imgClassDirectory = "classes/" + docType;
		// TODO: remove the following line (only present in development)
		final String imgDirectory = imgClassDirectory + "/ref2/";
		log.debug("imgClassDirectory = {} ", imgClassDirectory);
		DocClass docClass = engine.find(DocClass.class);
		DocClassInstance docClassInstance = docClass.setDocClass(docType);
		final Map<String, ImgFunction> imgFilters = getFiltersMap();
		final Zones zones = Zones.loadZones(imgClassDirectory);

		initComputation(engine, docType, zones);
		Arrays.asList(new File(imgDirectory).listFiles((dir, name) -> name.endsWith(".png"))).forEach(file -> {
			processFile(engine, file, docClassInstance, zones, imgFilters.entrySet().stream());
			engine.getCurrentCache().flush();
		});
		engine.getCurrentCache().flush();
	}

	/**
	 * Initialize the computation. The zones are added to the model only if they differ from the ones previously saved.
	 * 
	 * @param engine - the engine used to store the data
	 * @param docType - the document type (i.e., class)
	 * @param zones - a {@link Zones} object, representing all the zones detected for ocr
	 */
	// TODO: change method's name
	private static void initComputation(Root engine, String docType, Zones zones) {
		DocClass docClass = engine.find(DocClass.class);
		DocClassInstance docClassInstance = docClass.getDocClass(docType);
		// Save the zones if necessary
		// TODO: refactor the code (duplicate)
		zones.getZones().forEach(z -> {
			ZoneInstance zoneInstance = docClassInstance.getZone(z.getNum());
			if (zoneInstance != null) {
				Zone zone = zoneInstance.getZoneObject();
				// log.info("z : {} ; zone : {}", z, zone);
				if (z.equals(zone)) {
					log.info("Zone n°{} already known", z.getNum());
				} else {
					log.info("Adding zone n°{} ", z.getNum());
					docClassInstance.setZone(z.getNum(), z.getRect().x, z.getRect().y, z.getRect().width, z.getRect().height);
				}
			} else {
				log.info("Adding zone n°{} ", z.getNum());
				docClassInstance.setZone(z.getNum(), z.getRect().x, z.getRect().y, z.getRect().width, z.getRect().height);
			}
		});
		// Persist the changes
		engine.getCurrentCache().flush();
	}

	/**
	 * Process an image file. Each zone of each image is analyzed through OCR, and the results are stored in Generic System engine.
	 * 
	 * @param engine - the engine where the data will be stored
	 * @param file - the file to be processed
	 * @param docClassInstance - the instance of {@link DocClass} representing the current class of the file
	 * @param zones - the list of zones for this image
	 * @param imgFilters - a stream of {@link Entry} for a Map containing the filternames that will be applied to the original file, and the functions required to apply these filters
	 * @return an {@code int} representing {@link #KNOWN_FILE_UPDATED_FILTERS}, {@link #NEW_FILE} or {@link #KNOWN_FILE}
	 */
	private static int processFile(Root engine, File file, DocClassInstance docClassInstance, Zones zones, Stream<Entry<String, ImgFunction>> imgFilters) {
		final boolean newFile = isThisANewFile(engine, file);
		int result = ERROR;
		log.info("\nProcessing file: {}", file.getName());

		Generic doc = engine.find(Doc.class);
		ZoneText zoneText = engine.find(ZoneText.class);
		ImgFilter imgFilter = engine.find(ImgFilter.class);

		// Save the current file
		String filenameExt = ModelTools.generateFileName(file.toPath());
		if (null == filenameExt) {
			log.error("An error has occured while saving the file! Aborted...");
			return result;
		}

		DocInstance docInstance = docClassInstance.setDoc(doc, filenameExt);
		docInstance.setDocFilename(file.getName());
		engine.getCurrentCache().flush();

		// TODO: refactor the code (duplicates)
		// Save the filternames if necessary
		Map<String, ImgFunction> updatedImgFilters = new ConcurrentHashMap<>();
		imgFilters.forEach(entry -> {
			ImgFilterInstance filter = imgFilter.getImgFilter(entry.getKey());
			if (filter == null) {
				log.info("Adding algorithm : {} ", entry.getKey());
				imgFilter.setImgFilter(entry.getKey());
				updatedImgFilters.put(entry.getKey(), entry.getValue());
			} else {
				// TODO: add another criteria to verify if the filter has been
				// applied on the image
				boolean containsNullZoneTextInstance = zones.getZones().stream().anyMatch(z -> {
					ZoneTextInstance zti = zoneText.getZoneText(docInstance, docClassInstance.getZone(z.getNum()), filter);
					return zti == null;
				});
				if (containsNullZoneTextInstance) {
					imgFilter.setImgFilter(entry.getKey());
					updatedImgFilters.put(entry.getKey(), entry.getValue());
				} else {
					log.debug("Algorithm {} already known", entry.getKey());
				}
			}
		});

		// Check whether or not the file has already been stored in the system
		if (newFile) {
			log.info("Adding a new image ({}) ", file.getName());
			result = NEW_FILE;
		} else {
			if (updatedImgFilters.isEmpty()) {
				log.info("The image {} has already been processed (pass)", file.getName());
				result = KNOWN_FILE;
				return result;
			} else {
				log.info("New filters detected for image {} ", file.getName());
				result = KNOWN_FILE_UPDATED_FILTERS;
			}
		}

		// If this is a new file, or a new filter has been added, update the last-update doc timestamp
		docInstance.setDocTimestamp(ModelTools.getCurrentDate());

		// Create a map of Imgs
		Map<String, Img> imgs = new ConcurrentHashMap<>();
		Img originalImg = new Img(file.getPath());
		updatedImgFilters.entrySet().forEach(entry -> {
			log.info("Applying algorithm {}...", entry.getKey());
			Img img = null;
			if ("original".equals(entry.getKey()) || "reality".equals(entry.getKey()))
				img = originalImg;
			else
				img = entry.getValue().apply(originalImg);
			if (null != img)
				imgs.put(entry.getKey(), img);
			else
				log.error("An error as occured for image {} and filter {}", filenameExt, entry.getKey());
		});

		// Draw the image's zones + numbers
		Img imgCopy = new Img(file.getPath());
		zones.draw(imgCopy, new Scalar(0, 255, 0), 3);
		zones.writeNum(imgCopy, new Scalar(0, 0, 255), 3);
		// Copy the images to the resources folder - TODO implement a filter mechanism to avoid creating duplicates in a public folder
		log.info("Copying {} to resources folder", filenameExt);
		Imgcodecs.imwrite(System.getProperty("user.dir") + "/../gs-watch/src/main/resources/" + filenameExt, imgCopy.getSrc());

		// Process each zone
		zones.getZones().forEach(z -> {
			log.info("Zone n° {}", z.getNum());
			ZoneInstance zoneInstance = docClassInstance.getZone(z.getNum());
			imgs.entrySet().forEach(entry -> {
				if ("reality".equals(entry.getKey()) || "best".equals(entry.getKey())) {
					// Do not proceed to OCR if the real values are known. By default, the "reality" and "best" filters are left empty
					if (null == zoneText.getZoneText(docInstance, zoneInstance, imgFilter.getImgFilter(entry.getKey())))
						zoneText.setZoneText("", docInstance, zoneInstance, imgFilter.getImgFilter(entry.getKey()));
				} else {
					String ocrText = z.ocr(entry.getValue());
					ZoneTextInstance zti = zoneText.setZoneText(ocrText, docInstance, zoneInstance, imgFilter.getImgFilter(entry.getKey()));
					zti.setZoneTimestamp(ModelTools.getCurrentDate()); // TODO: concatenate with previous line?
				}
			});
			engine.getCurrentCache().flush();
		});

		// Close the images to force freeing OpenCV's resources (native matrices)
		imgCopy.close();
		originalImg.close();
		imgs.entrySet().forEach(entry -> entry.getValue().close());

		return result;
	}

	/**
	 * Save a new document in Generic System using the default Engine.
	 * 
	 * @param imgPath - the Path of the file
	 * @return true if this was a success, false otherwise
	 */
	public static boolean registerNewFile(Path imgPath) {
		final Root engine = getEngine();
		engine.newCache().start();
		boolean result = registerNewFile(engine, imgPath);
		engine.close();
		return result;
	}

	/**
	 * Save a new document in Generic System.
	 * 
	 * @param imgPath - the Path of the file
	 * @return true if this was a success, false otherwise
	 */
	public static boolean registerNewFile(Root engine, Path imgPath) {
		try {
			engine.getCurrentCache();
		} catch (IllegalStateException e) {
			log.debug("Current cache could not be loaded. Starting a new one...");
			engine.newCache().start();
		}
		final String docType = ModelTools.getImgClass(imgPath);

		// Find and save the doc class
		DocClass docClass = engine.find(DocClass.class);
		DocClassInstance docClassInstance = docClass.setDocClass(docType);
		engine.getCurrentCache().flush();

		final boolean newFile = isThisANewFile(engine, imgPath.toFile(), docType);
		if (!newFile) {
			log.info("Image {} is already known", imgPath.getFileName());
			return true;
		} else {
			log.info("Adding a new image ({}) ", imgPath.getFileName());
			String filenameExt = ModelTools.generateFileName(imgPath);
			Generic doc = engine.find(Doc.class);
			DocInstance docInstance = docClassInstance.setDoc(doc, filenameExt);
			if (null != docInstance) {
				docInstance.setDocFilename(imgPath.getFileName().toString());
				docInstance.setDocTimestamp(ModelTools.getCurrentDate());
				engine.getCurrentCache().flush();
				try (Img img = new Img(imgPath.toString())) {
					log.info("Copying {} to resources folder", filenameExt);
					Imgcodecs.imwrite(System.getProperty("user.dir") + "/../gs-watch/src/main/resources/" + filenameExt, img.getSrc());
				}
				return true;
			} else {
				log.error("An error has occured while saving file {}", filenameExt);
				return false;
			}
		}
	}

	@SuppressWarnings("unused")
	private static Map<String, ImgFunction> filterOptimizationMap() {
		final Map<String, ImgFunction> imgFilters = new ConcurrentHashMap<>();
		// Niblack
		// List<Integer> blockSizes = Arrays.asList(new Integer[] { 7, 9, 11,
		// 15, 17, 21, 27, 37 });
		// List<Double> ks = Arrays.asList(new Double[] { -1.0, -0.8, -0.6,
		// -0.5, -0.4, -0.3, -0.2, -0.1, 0.0, 0.1 });
		// Sauvola, Nick
		List<Integer> blockSizes = Arrays.asList(new Integer[] { 7, 9, 11, 15, 17, 21, 27, 37 });
		List<Double> ks = Arrays.asList(new Double[] { 0.0, 0.1, 0.2, 0.3, 0.4 });
		// Wolf
		// List<Integer> blockSizes = Arrays.asList(new Integer[] { 7, 9, 11,
		// 15, 17, 21, 27, 37 });
		// List<Double> ks = Arrays.asList(new Double[] { -0.25, -0.2, -0.15,
		// 0.1, -0.05, 0.0 });
		for (Integer bs : blockSizes) {
			for (Double k : ks) {
				imgFilters.put("nick" + "_" + bs + "_" + k.toString().replace("-", "m"), img -> img.niblackThreshold(bs, k));
			}
		}
		imgFilters.put("reality", i -> i);
		imgFilters.put("original", i -> i);
		return imgFilters;
	}

	/**
	 * Remove all the data stored in the engine, except the real values used for training (e.g., for which imgFilter = "reality")
	 * 
	 * @param engine - the engine used to store the data
	 */
	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	private static void cleanModel(Root engine) {

		System.out.println("Cleaning model...");

		// Get the necessary classes from the engine
		DocClass docClass = engine.find(DocClass.class);
		Generic doc = engine.find(Doc.class);
		ZoneText zoneText = engine.find(ZoneText.class);
		ImgFilter imgFilter = engine.find(ImgFilter.class);
		Score score = engine.find(Score.class);
		MeanLevenshtein ml = engine.find(MeanLevenshtein.class);

		// Save the current document class
		Generic currentDocClass = engine.find(DocClass.class).getInstance(docType);

		List<ImgFilterInstance> imgFilterInstances = (List) imgFilter.getInstances().filter(f -> !"reality".equals(f.getValue())).toList();
		List<ZoneInstance> zoneInstances = (List) currentDocClass.getHolders(engine.find(ZoneGeneric.class)).toList();
		List<DocInstance> docInstances = (List) currentDocClass.getHolders(engine.find(Doc.class)).toList();

		// Delete all ZoneTextInstances that are not "reality"
		docInstances.forEach(currentDoc -> {
			imgFilterInstances.forEach(i -> {
				zoneInstances.forEach(z -> {
					ZoneTextInstance zti = zoneText.getZoneText(currentDoc, z, i);
					if (zti != null) {
						zti.getHolders(engine.find(ZoneTimestamp.class)).forEach(g -> g.remove());
						zti.remove();
					}
				});
				engine.getCurrentCache().flush();
			});
		});

		// Delete all filters that are not reality", and their attached scores
		imgFilterInstances.forEach(i -> {
			zoneInstances.forEach(z -> {
				ScoreInstance scoreInst = score.getScore(z, i);
				if (scoreInst != null) {
					scoreInst.getHolder(ml).remove();
					scoreInst.remove();
				}
			});
			i.remove();
			engine.getCurrentCache().flush();
		});

		// Finally delete all documents for which no ZoneTextInstances exist (i.e., not supervised)
		docInstances.forEach(currentDoc -> {
			zoneInstances.forEach(z -> {
				boolean result = imgFilter.getInstances().stream().allMatch(i -> {
					ZoneTextInstance zti = zoneText.getZoneText(currentDoc, z, (ImgFilterInstance) i);
					return null == zti || zti.getValue().toString().isEmpty();
				});
				if (result) {
					currentDoc.getDependencies().forEach(dependency -> {
						currentDoc.getHolders(dependency).forEach(g -> g.remove());
						dependency.remove();
						engine.getCurrentCache().flush();
					});
					// FIXME unable to delete the currentDoc (AliveConstraint violation)
					currentDoc.remove();
					engine.getCurrentCache().flush();
				}
			});
		});

		engine.getCurrentCache().flush();
		System.out.println("Done!");
	}
}
