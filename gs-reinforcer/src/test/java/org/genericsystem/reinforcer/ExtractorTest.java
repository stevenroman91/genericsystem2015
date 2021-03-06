package org.genericsystem.reinforcer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

@Test
public class ExtractorTest {

	private final static Logger logger = LoggerFactory.getLogger(ExtractorTest.class);

	// 4 labels at the same places in 4 all images, 2 classes depending on text.
	public void test001() {
		Extractor extractor = new Extractor();
		Labels labels1 = new Labels();
		labels1.addLabel(1, 1, 50, 20, "Surname;");
		labels1.addLabel(55, 1, 100, 20, "Kenobi");
		labels1.addLabel(1, 22, 50, 42, "Name:");
		labels1.addLabel(55, 22, 100, 42, "Obi-Wan");
		extractor.reinforce(labels1, "horizSep");
		Labels labels2 = new Labels();
		labels2.addLabel(1, 1, 50, 20, "Surname;");
		labels2.addLabel(55, 1, 100, 20, "Vader");
		labels2.addLabel(1, 22, 50, 42, "Name:");
		labels2.addLabel(55, 22, 100, 42, "Darth");
		extractor.reinforce(labels2, "horizSep");
		Labels labels3 = new Labels();
		labels3.addLabel(1, 1, 50, 20, "Surname:");
		labels3.addLabel(55, 1, 100, 20, "Name:");
		labels3.addLabel(1, 22, 50, 42, "Kenobi");
		labels3.addLabel(55, 22, 100, 42, "Obi-Wan");
		extractor.reinforce(labels3, "vertSep");
		Labels labels4 = new Labels();
		labels4.addLabel(1, 1, 50, 20, "Surname;");
		labels4.addLabel(55, 1, 100, 20, "Name:");
		labels4.addLabel(1, 22, 50, 42, "Vader");
		labels4.addLabel(55, 22, 100, 42, "Darth");
		extractor.reinforce(labels4, "vertSep");

		Labels labels5 = new Labels();
		labels5.addLabel(1, 1, 50, 20, "Surname;");
		labels5.addLabel(55, 1, 100, 20, "Name:");
		labels5.addLabel(1, 22, 50, 42, "Skywalker");
		labels5.addLabel(55, 22, 100, 42, "Luke");
		extractor.reinforce(labels5, "horizSep");
		Labels labels6 = new Labels();
		labels6.addLabel(1, 1, 50, 20, "Surname;");
		labels6.addLabel(55, 1, 100, 20, "Name:");
		labels6.addLabel(1, 22, 50, 42, "Yoda");
		labels6.addLabel(55, 22, 100, 42, "Master");
		Labels labels7 = new Labels();
		labels7.addLabel(1, 1, 50, 20, "Surname;");
		labels7.addLabel(55, 1, 100, 20, "Skywalker");
		labels7.addLabel(1, 22, 50, 42, "Name:");
		labels7.addLabel(55, 22, 100, 42, "Luke");
		Labels labels8 = new Labels();
		labels8.addLabel(1, 1, 50, 20, "Surname;");
		labels8.addLabel(55, 1, 100, 20, "Yoda");
		labels8.addLabel(1, 22, 50, 42, "Name:");
		labels8.addLabel(55, 22, 100, 42, "Master");

		assert DetectedContent.found(extractor.extractData(labels1, "horizSep"), "Kenobi");
		assert DetectedContent.found(extractor.extractData(labels1, "horizSep"), "Obi-Wan");
		assert DetectedContent.found(extractor.extractData(labels2, "horizSep"), "Vader");
		assert DetectedContent.found(extractor.extractData(labels2, "horizSep"), "Darth");
		assert DetectedContent.found(extractor.extractData(labels3, "vertSep"), "Kenobi");
		assert DetectedContent.found(extractor.extractData(labels3, "vertSep"), "Obi-Wan");
		assert DetectedContent.found(extractor.extractData(labels4, "vertSep"), "Darth");
		assert DetectedContent.found(extractor.extractData(labels4, "vertSep"), "Vader");
		assert DetectedContent.found(extractor.extractData(labels5, "vertSep"), "Skywalker");
		assert DetectedContent.found(extractor.extractData(labels5, "vertSep"), "Luke");
		assert DetectedContent.found(extractor.extractData(labels6, "vertSep"), "Yoda");
		assert DetectedContent.found(extractor.extractData(labels6, "vertSep"), "Master");
		assert DetectedContent.found(extractor.extractData(labels7, "horizSep"), "Skywalker");
		assert DetectedContent.found(extractor.extractData(labels7, "horizSep"), "Luke");
		assert DetectedContent.found(extractor.extractData(labels8, "horizSep"), "Yoda");
		assert DetectedContent.found(extractor.extractData(labels8, "horizSep"), "Master");
	}

	// 4 labels at roughly the same places in all images, 2 classes depending on text.
	public void test002() {
		Extractor extractor = new Extractor();
		Labels labels1 = new Labels();
		labels1.addLabel(5, 2, 50, 21, "Surname:");
		labels1.addLabel(60, 3, 110, 19, "Kenobi");
		labels1.addLabel(6, 24, 54, 44, "Name.");
		labels1.addLabel(60, 23, 104, 46, "Obi-Wan");
		extractor.reinforce(labels1, "horizSep");
		Labels labels2 = new Labels();
		labels2.addLabel(5, 3, 106, 39, "Surname:");
		labels2.addLabel(114, 4, 207, 41, "Vader") ;
		labels2.addLabel(6, 45, 108, 83, "Name:");
		labels2.addLabel(117, 43, 210, 85, "Darth");
		extractor.reinforce(labels2, "horizSep");
		Labels labels3 = new Labels();
		labels3.addLabel(6, 1, 78, 27, "Surname;");
		labels3.addLabel(84, 6, 144, 33, "Name:");
		labels3.addLabel(2, 33, 81, 60, "Kenobi");
		labels3.addLabel(84, 36, 151, 69, "Obi-Wan");
		extractor.reinforce(labels3, "vertSep");
		Labels labels4 = new Labels();
		labels4.addLabel(20, 42, 69, 59, "Surname;");
		labels4.addLabel(76, 43, 121, 60, "Name:");
		labels4.addLabel(21, 62, 72, 83, "Vader");
		labels4.addLabel(74, 65, 123, 81, "Darth");
		extractor.reinforce(labels4, "vertSep");

		Labels labels5 = new Labels();
		labels5.addLabel(1, 1, 50, 20, "Surname;");
		labels5.addLabel(55, 1, 100, 20, "Name:");
		labels5.addLabel(1, 22, 50, 42, "Skywalker");
		labels5.addLabel(55, 22, 100, 42, "Luke");
		Labels labels6 = new Labels();
		labels6.addLabel(1, 1, 50, 20, "Surnarne;");
		labels6.addLabel(55, 1, 100, 20, "Name:");
		labels6.addLabel(1, 22, 50, 42, "Yoda");
		labels6.addLabel(55, 22, 100, 42, "Master");
		Labels labels7 = new Labels();
		labels7.addLabel(1, 5, 50, 30, "Sumame:");
		labels7.addLabel(55, 7, 100, 29, "Skywalker");
		labels7.addLabel(1, 30, 50, 50, "Name:");
		labels7.addLabel(55, 31, 100, 49, "Luke");
		Labels labels8 = new Labels();
		labels8.addLabel(1, 1, 50, 20, "Surname;");
		labels8.addLabel(55, 1, 100, 20, "Yoda");
		labels8.addLabel(1, 22, 50, 42, "Name:");
		labels8.addLabel(55, 22, 100, 42, "Master");

		assert DetectedContent.found(extractor.extractData(labels1, "horizSep"), "Kenobi");
		assert DetectedContent.found(extractor.extractData(labels1, "horizSep"), "Obi-Wan");
		assert DetectedContent.found(extractor.extractData(labels2, "horizSep"), "Vader");
		assert DetectedContent.found(extractor.extractData(labels2, "horizSep"), "Darth");
		assert DetectedContent.found(extractor.extractData(labels3, "vertSep"), "Kenobi");
		assert DetectedContent.found(extractor.extractData(labels3, "vertSep"), "Obi-Wan");
		assert DetectedContent.found(extractor.extractData(labels4, "vertSep"), "Darth");
		assert DetectedContent.found(extractor.extractData(labels4, "vertSep"), "Vader");
		assert DetectedContent.found(extractor.extractData(labels5, "vertSep"), "Skywalker");
		assert DetectedContent.found(extractor.extractData(labels5, "vertSep"), "Luke");
		assert DetectedContent.found(extractor.extractData(labels6, "vertSep"), "Yoda");
		assert DetectedContent.found(extractor.extractData(labels6, "vertSep"), "Master");
		assert DetectedContent.found(extractor.extractData(labels7, "horizSep"), "Skywalker");
		assert DetectedContent.found(extractor.extractData(labels7, "horizSep"), "Luke");
		assert DetectedContent.found(extractor.extractData(labels8, "horizSep"), "Yoda");
		assert DetectedContent.found(extractor.extractData(labels8, "horizSep"), "Master");
	}
}
