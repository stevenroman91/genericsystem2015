package org.genericsystem.cv.newmodel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.genericsystem.api.core.Snapshot;
import org.genericsystem.api.core.annotations.Components;
import org.genericsystem.api.core.annotations.InstanceClass;
import org.genericsystem.api.core.annotations.SystemGeneric;
import org.genericsystem.api.core.annotations.constraints.InstanceValueClassConstraint;
import org.genericsystem.api.core.annotations.constraints.PropertyConstraint;
import org.genericsystem.api.core.annotations.constraints.SingularConstraint;
import org.genericsystem.common.Generic;
import org.genericsystem.cv.newmodel.SimpleModel.ConsolidatedType.ConsolidatedInstance;
import org.genericsystem.cv.newmodel.SimpleModel.DocClassType.DocClassInstance;
import org.genericsystem.cv.newmodel.SimpleModel.DocType.DocInstance;
import org.genericsystem.cv.newmodel.SimpleModel.ImgDocRel.ImgDocLink;
import org.genericsystem.cv.newmodel.SimpleModel.ImgPathType.ImgPathInstance;
import org.genericsystem.cv.newmodel.SimpleModel.ImgRefreshTimestampType.ImgRefreshTimestampInstance;
import org.genericsystem.cv.newmodel.SimpleModel.ImgTimestampType.ImgTimestampInstance;
import org.genericsystem.cv.newmodel.SimpleModel.ImgType.ImgInstance;
import org.genericsystem.cv.newmodel.SimpleModel.LayoutType.LayoutInstance;
import org.genericsystem.cv.newmodel.SimpleModel.SupervisedType.SupervisedInstance;
import org.genericsystem.cv.newmodel.SimpleModel.ZoneNumType.ZoneNumInstance;
import org.genericsystem.cv.newmodel.SimpleModel.ZoneType.ZoneInstance;
import org.opencv.core.Rect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple Model to store the data extracted from documents in Generic System.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SimpleModel {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * DocClassType represents the type of all the classes.
	 */
	@SystemGeneric
	@InstanceClass(DocClassInstance.class)
	public static class DocClassType implements Generic {

		@SystemGeneric
		public static class DocClassInstance implements Generic {

			public Snapshot<DocInstance> getAllDocInstances() {
				return (Snapshot) getHolders(getRoot().find(DocType.class));
			}

			public Snapshot<LayoutInstance> getAllLayouts() {
				return (Snapshot) getComposites().filter(composite -> getRoot().find(LayoutType.class).equals(composite.getMeta()));
			}

			public Snapshot<LayoutInstance> getAllLayoutLeaves() {
				return getAllLayouts().filter(layout -> layout.getInheritings().isEmpty());
			}

			public LayoutInstance getLayoutRoot() {
				return getAllLayouts().filter(layout -> layout.getSupers().isEmpty()).first();
			}

			public DocInstance addDocInstance(String name) {
				return (DocInstance) addHolder(getRoot().find(DocType.class), name);
			}

			public DocInstance getDocInstance(String name) {
				return (DocInstance) getHolder(getRoot().find(DocType.class), name);
			}

			public LayoutInstance addLayout(String json) {
				return (LayoutInstance) addHolder(getRoot().find(LayoutType.class), Collections.emptyList(), json);
			}

			public LayoutInstance addLayout(String json, LayoutInstance... parents) {
				return (LayoutInstance) addHolder(getRoot().find(LayoutType.class), Arrays.asList(parents), json);
			}

			public LayoutInstance getLayout(String json) {
				return (LayoutInstance) getHolder(getRoot().find(LayoutType.class), json);
			}

		}

		public Snapshot<DocClassInstance> getAllDocClasses() {
			return (Snapshot) getInstances();
		}

		public DocClassInstance addDocClass(String name) {
			return (DocClassInstance) addInstance(name);
		}

		public DocClassInstance getDocClass(String name) {
			return (DocClassInstance) getInstance(name);
		}

	}

	/**
	 * LayoutType represents the type of the attribute Layout on the {@link DocClassType}. Each element of the layout is a holder of type LayoutType. </br>
	 * The children/parents relationships are mapped using inheritance.
	 */
	@SystemGeneric
	@Components(DocClassType.class)
	@InstanceClass(LayoutInstance.class)
	public static class LayoutType implements Generic {

		@SystemGeneric
		public static class LayoutInstance implements Generic {

			public DocClassInstance getDocClassInstance() {
				return (DocClassInstance) getBaseComponent();
			}

		}
	}

	/**
	 * ImgDocRel represents the relation (many to one) between an abstract {@link DocType} and a concrete {@link ImgType}.
	 */
	@SystemGeneric
	@Components({ ImgType.class, DocType.class })
	@SingularConstraint(value = 0)
	@InstanceClass(ImgDocLink.class)
	public static class ImgDocRel implements Generic {

		@SystemGeneric
		public static class ImgDocLink implements Generic {

			public ImgInstance getImgInstance() {
				return (ImgInstance) getComponent(0);
			}

			public DocInstance getDocInstance() {
				return (DocInstance) getComponent(1);
			}
		}

		public Snapshot<ImgDocLink> getAllImgDocLinks() {
			return (Snapshot) getInstances();
		}

		public ImgDocLink addImgDocLink(String name, ImgInstance imgInstance, DocInstance docInstance) {
			return (ImgDocLink) addInstance(name, imgInstance, docInstance);
		}

		public ImgDocLink getImgDocLink(ImgInstance imgInstance, DocInstance docInstance) {
			return (ImgDocLink) getInstance(imgInstance, docInstance);
		}
	}

	/**
	 * A DocType represents the "abstract" document, which can be linked to one or more {@link ImgType} through {@link ImgDocRel}. At this stage, duplicates of ImgType are removed.
	 */
	@SystemGeneric
	@Components(DocClassType.class)
	@InstanceClass(DocInstance.class)
	public static class DocType implements Generic {

		@SystemGeneric
		public static class DocInstance implements Generic {

			public DocClassInstance getDocClassInstance() {
				return (DocClassInstance) getBaseComponent();
			}

			public Snapshot<ImgDocLink> getAllImgDocLinks() {
				return (Snapshot) getLinks(getRoot().find(ImgDocRel.class));
			}

			public Snapshot<ImgInstance> getAllLinkedImgs() {
				return getAllImgDocLinks().map(link -> link.getImgInstance());
			}

			public ImgDocLink addImgDocLink(String name, ImgInstance imgInstance) {
				return (ImgDocLink) addLink(getRoot().find(ImgDocRel.class), name, imgInstance);
			}

			public ImgDocLink getImgDocLink(ImgInstance imgInstance) {
				return (ImgDocLink) getLink(getRoot().find(ImgDocRel.class), imgInstance);
			}
		}

	}

	/**
	 * An ImgType represents the actual image of the processed document. Duplicates of the same document can exist as multiple instances of this class.
	 */
	@SystemGeneric
	@InstanceClass(ImgInstance.class)
	@InstanceValueClassConstraint(String.class)
	public static class ImgType implements Generic {

		@SystemGeneric
		public static class ImgInstance implements Generic {

			public Snapshot<ZoneInstance> getZoneInstances() {
				return (Snapshot) getHolders(getRoot().find(ZoneType.class));
			}

			public Snapshot<ZoneInstance> getEmptyZoneInstances() {
				return getZoneInstances().filter(zone -> {
					ConsolidatedInstance consolidated = zone.getConsolidated();
					return !isConsolidated(consolidated);
				});
			}

			public Snapshot<ZoneInstance> getConsolidatedZoneInstances() {
				return getZoneInstances().filter(zone -> {
					ConsolidatedInstance consolidated = zone.getConsolidated();
					return isConsolidated(consolidated);
				});
			}

			private boolean isConsolidated(ConsolidatedInstance consolidated) {
				return consolidated != null && consolidated.getValue() != null && !"".equals(consolidated.getValue());
			}

			public ZoneInstance addZone(Rect rect) {
				try {
					String json = mapper.writeValueAsString(rect);
					return (ZoneInstance) addHolder(getRoot().find(ZoneType.class), json);
				} catch (JsonProcessingException e) {
					throw new IllegalStateException("An error has occured while converting the rectangle to a json string", e);
				}
			}

			public ZoneInstance getZone(Rect rect) {
				try {
					String json = mapper.writeValueAsString(rect);
					return getZone(json);
				} catch (JsonProcessingException e) {
					throw new IllegalStateException("An error has occured while converting the rectangle to a json string", e);
				}
			}

			public ZoneInstance addZone(String json) {
				return (ZoneInstance) addHolder(getRoot().find(ZoneType.class), json);
			}

			public ZoneInstance getZone(String json) {
				return (ZoneInstance) getHolder(getRoot().find(ZoneType.class), json);
			}

			public ImgPathInstance setImgPath(String relativePath) {
				return (ImgPathInstance) setHolder(getRoot().find(ImgPathType.class), relativePath);
			}

			public ImgPathInstance getImgPath() {
				return (ImgPathInstance) getHolder(getRoot().find(ImgPathType.class));
			}

			public ImgTimestampInstance setImgTimestamp(Long timestamp) {
				return (ImgTimestampInstance) setHolder(getRoot().find(ImgTimestampType.class), timestamp);
			}

			public ImgTimestampInstance getImgTimestamp() {
				return (ImgTimestampInstance) getHolder(getRoot().find(ImgTimestampType.class));
			}

			public ImgRefreshTimestampInstance setImgRefreshTimestamp(Long timestamp) {
				return (ImgRefreshTimestampInstance) setHolder(getRoot().find(ImgRefreshTimestampType.class), timestamp);
			}

			public ImgRefreshTimestampInstance getImgRefreshTimestamp() {
				return (ImgRefreshTimestampInstance) getHolder(getRoot().find(ImgRefreshTimestampType.class));
			}

			public ImgDocLink addImgDocLink(String name, DocInstance docInstance) {
				return (ImgDocLink) addLink(getRoot().find(ImgDocRel.class), name, docInstance);
			}

			public ImgDocLink getImgDocLink() {
				return (ImgDocLink) getLink(getRoot().find(ImgDocRel.class));
			}

			public DocInstance getLinkedDoc() {
				return getImgDocLink().getDocInstance();
			}

		}

		public Snapshot<ImgInstance> getImgInstances() {
			return (Snapshot) getInstances();
		}

		public ImgInstance addImg(String name) {
			return (ImgInstance) addInstance(name);
		}

		public ImgInstance getImg(String name) {
			return (ImgInstance) getInstance(name);
		}
	}

	@SystemGeneric
	@Components(ImgType.class)
	@InstanceClass(ZoneInstance.class)
	@InstanceValueClassConstraint(String.class)
	public static class ZoneType implements Generic {
		// TODO: might need to change the value of the instance: String not ideal, since there is no direct comparison of the json objects

		@SystemGeneric
		public static class ZoneInstance implements Generic {

			public ImgInstance getImgInstance() {
				return (ImgInstance) getBaseComponent();
			}

			public ZoneNumInstance setZoneNum(int num) {
				return (ZoneNumInstance) setHolder(getRoot().find(ZoneNumType.class), num);
			}

			public ZoneNumInstance getZoneNum() {
				return (ZoneNumInstance) getHolder(getRoot().find(ZoneNumType.class));
			}

			public ConsolidatedInstance setConsolidated(String consolidated) {
				return (ConsolidatedInstance) setHolder(getRoot().find(ConsolidatedType.class), consolidated);
			}

			public ConsolidatedInstance getConsolidated() {
				return (ConsolidatedInstance) getHolder(getRoot().find(ConsolidatedType.class));
			}

			public SupervisedInstance setSupervised(String supervised) {
				return (SupervisedInstance) setHolder(getRoot().find(SupervisedType.class), supervised);
			}

			public SupervisedInstance getSupervised() {
				return (SupervisedInstance) getHolder(getRoot().find(SupervisedType.class));
			}

			public Rect getZoneRect() {
				try {
					Rect rect = mapper.readValue(getValue().toString(), Rect.class);
					return rect;
				} catch (IOException e) {
					throw new IllegalStateException("An error has occured while converting the json to a rectangle", e);
				}
			}
		}
	}

	@SystemGeneric
	@Components(ZoneType.class)
	@PropertyConstraint
	@InstanceClass(ZoneNumInstance.class)
	@InstanceValueClassConstraint(Integer.class)
	public static class ZoneNumType implements Generic {

		@SystemGeneric
		public static class ZoneNumInstance implements Generic {

			public ZoneInstance getZoneInstance() {
				return (ZoneInstance) getBaseComponent();
			}
		}

	}

	@SystemGeneric
	@Components(ZoneType.class)
	@PropertyConstraint
	@InstanceClass(ConsolidatedInstance.class)
	@InstanceValueClassConstraint(String.class)
	public static class ConsolidatedType implements Generic {

		@SystemGeneric
		public static class ConsolidatedInstance implements Generic {

			public ZoneInstance getZoneInstance() {
				return (ZoneInstance) getBaseComponent();
			}
		}
	}

	@SystemGeneric
	@Components(ZoneType.class)
	@PropertyConstraint
	@InstanceClass(SupervisedInstance.class)
	@InstanceValueClassConstraint(String.class)
	public static class SupervisedType implements Generic {

		@SystemGeneric
		public static class SupervisedInstance implements Generic {

			public ZoneInstance getZoneInstance() {
				return (ZoneInstance) getBaseComponent();
			}
		}
	}

	@SystemGeneric
	@Components(ImgType.class)
	@PropertyConstraint
	@InstanceClass(ImgPathInstance.class)
	@InstanceValueClassConstraint(String.class)
	public static class ImgPathType implements Generic {

		@SystemGeneric
		public static class ImgPathInstance implements Generic {

			public ImgInstance getImgInstance() {
				return (ImgInstance) getBaseComponent();
			}
		}
	}

	@SystemGeneric
	@Components(ImgType.class)
	@PropertyConstraint
	@InstanceClass(ImgTimestampInstance.class)
	@InstanceValueClassConstraint(Long.class)
	public static class ImgTimestampType implements Generic {

		@SystemGeneric
		public static class ImgTimestampInstance implements Generic {

			public ImgInstance getImgInstance() {
				return (ImgInstance) getBaseComponent();
			}

		}
	}

	@SystemGeneric
	@Components(ImgType.class)
	@PropertyConstraint
	@InstanceClass(ImgRefreshTimestampInstance.class)
	@InstanceValueClassConstraint(Long.class)
	public static class ImgRefreshTimestampType implements Generic {

		@SystemGeneric
		public static class ImgRefreshTimestampInstance implements Generic {

			public ImgInstance getImgInstance() {
				return (ImgInstance) getBaseComponent();
			}
		}
	}
}
