package org.genericsystem.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.genericsystem.cv.Img;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public abstract class AbstractFields implements Iterable<AbstractField> {

	protected List<AbstractField> fields;

	public AbstractFields() {
		this.fields = new ArrayList<>();
	}

	public AbstractFields(List<AbstractField> fields) {
		this.fields = fields;
	}

	public void removeOverlaps() {
		for (AbstractField field : fields) {
			List<AbstractField> overlaps = getOverlaps(field);
			if (overlaps != null && !overlaps.isEmpty()) {
				tryMerge(field, overlaps);
			}
		}
	}

	public List<AbstractField> getOverlaps(AbstractField targetField) {
		// TODO Auto-generated method stub
		// Look for the fields that overlaps targetField
		List<AbstractField> overlaps = new ArrayList<>();
		for (AbstractField field : fields) {
			if (targetField.isOverlapping(field)) {
				overlaps.add(field);
			}
		}
		return overlaps;
	}

	public void tryMerge(AbstractField targetField, List<AbstractField> overlaps) {
		// TODO Auto-generated method stub

	}

	public void consolidateOcr(Img rootImg) {
		stream().filter(AbstractField::needOcr).forEach(f -> f.ocr(rootImg));
	}

	public void drawOcrPerspectiveInverse(Img display, Mat homography, Scalar color, int thickness) {
		consolidatedFieldStream().forEach(field -> field.drawOcrPerspectiveInverse(display, homography, color, thickness));
	}

	public void drawConsolidated(Img stabilizedDisplay) {
		consolidatedFieldStream().forEach(field -> field.draw(stabilizedDisplay));
	}

	public Stream<AbstractField> consolidatedFieldStream() {
		return stream().filter(f -> f.isConsolidated());
	}

	public int size() {
		return fields.size();
	}

	public Stream<AbstractField> stream() {
		return fields.stream();
	}

	public Stream<AbstractField> parallelStream() {
		return fields.parallelStream();
	}

	@Override
	public Iterator<AbstractField> iterator() {
		return fields.iterator();
	}
}
