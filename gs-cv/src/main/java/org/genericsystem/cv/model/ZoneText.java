package org.genericsystem.cv.model;

import org.genericsystem.api.core.annotations.Components;
import org.genericsystem.api.core.annotations.InstanceClass;
import org.genericsystem.api.core.annotations.SystemGeneric;
import org.genericsystem.common.Generic;
import org.genericsystem.cv.model.Doc.DocInstance;
import org.genericsystem.cv.model.ImgFilter.ImgFilterInstance;
import org.genericsystem.cv.model.ZoneGeneric.ZoneInstance;
import org.genericsystem.cv.model.ZoneText.ZoneTextInstance;

@SystemGeneric
@Components({ Doc.class, ZoneGeneric.class, ImgFilter.class })
@InstanceClass(ZoneTextInstance.class)
public class ZoneText implements Generic {

	public static class ZoneTextInstance implements Generic {

		public DocInstance getDoc() {
			return (DocInstance) this.getComponent(0);
		}

		public ZoneInstance getZone() {
			return (ZoneInstance) this.getComponent(1);
		}

		public ImgFilterInstance getImgFilter() {
			return (ImgFilterInstance) this.getComponent(2);
		}

	}

	public ZoneTextInstance addZoneText(String text, DocInstance doc, ZoneInstance zone, ImgFilterInstance imgFilter) {
		return (ZoneTextInstance) setInstance(text, doc, zone, imgFilter);
	}

	public ZoneTextInstance getZoneText(DocInstance doc, ZoneInstance zone, ImgFilterInstance imgFilter) {
		// Only one text for given document, zone and filter
		System.out.println("elem " + getInstances(doc, zone, imgFilter).toList().get(0));
		return (ZoneTextInstance) getInstances(doc, zone, imgFilter).toList().get(0);
	}

}
