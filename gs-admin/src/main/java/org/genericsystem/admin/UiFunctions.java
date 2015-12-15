package org.genericsystem.admin;

import java.io.Serializable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.genericsystem.admin.UiFunctions.AttributeUiFunctions;
import org.genericsystem.admin.javafx.AbstractColumn;
import org.genericsystem.admin.javafx.LinksTableView.TriConsumer;
import org.genericsystem.api.core.Snapshot;
import org.genericsystem.common.Generic;
import org.genericsystem.distributed.cacheonclient.CocClientEngine;

public abstract class UiFunctions<G> implements Function<G, AttributeUiFunctions<G>> {
	public Function<G, Serializable> genericGetter;
	public BiConsumer<G, Serializable> genericSetter;
	public Function<G, List<G>> genericComponents;
	public BiFunction<G, Integer, G> genericComponentGetter;
	public TriConsumer<G, Integer, G> genericComponentSetter;
	public Function<G, BiFunction<Serializable, List<G>, G>> attributeAddAction;
	public Function<G, StringConverter<Serializable>> attributeConverter;
	public Function<G, Function<G, ObservableList<G>>> attributeGetter;
	public Function<G, Snapshot<G>> typeAttributes;
	public Function<G, ObservableList<G>> genericSubInstances;
	public Consumer<G> removeConsumer;

	// juba
	public Consumer<G> flushConsumer;
	public Consumer<G> mountConsumer;
	public Consumer<G> unmountConsumer;
	public Consumer<G> clearConsumer;
	public Consumer<G> shiftTsConsumer;

	public static abstract class AttributeUiFunctions<G> extends UiFunctions<G> {
		public BiFunction<Serializable, List<G>, G> addAction;
		public StringConverter<Serializable> converter;
		public Function<G, ObservableList<G>> linksGetter;
	}

	public static class GsUiFunctions extends UiFunctions<Generic> {
		Generic vehicle;
		Generic vehiclePower;

		public GsUiFunctions() {
			genericGetter = generic -> generic != null ? generic.getValue() : null;
			genericSetter = (generic, value) -> {
				generic.updateValue(value);
				System.out.println("Update in GS : " + generic.info());
			};
			genericComponents = generic -> generic.getComponents();
			genericComponentGetter = (generic, pos) -> generic.getComponent(pos);
			genericComponentSetter = (generic, pos, target) -> {
				generic.updateComponent(target, pos);
				System.out.println("Update in GS : " + generic.info());
			};
			attributeAddAction = attribute -> (value, components) -> {
				Generic generic = attribute.addInstance(value, components.toArray(new Generic[components.size()]));
				System.out.println("Add into GS : " + generic.info());
				return generic;
			};
			attributeConverter = attribute -> AbstractColumn.getDefaultInstanceValueStringConverter(attribute.getInstanceValueClassConstraint());
			attributeGetter = attribute -> generic -> FXCollections.observableArrayList((generic.getHolders(attribute).toList()));
			typeAttributes = typ -> typ.getAttributes().filter(attribute -> attribute.isCompositeForInstances(typ));
			genericSubInstances = targetComponent -> FXCollections.observableArrayList((targetComponent.getSubInstances().toList()));

			removeConsumer = generic -> {
				generic.remove();
				System.out.println("Remove from GS : " + generic.info());
			};

			flushConsumer = generic -> {
				generic.getRoot().getCurrentCache().flush();
			};

			clearConsumer = generic -> {
				((CocClientEngine) generic).getCurrentCache().clear();
			};

			shiftTsConsumer = generic -> {
				((CocClientEngine) generic).getCurrentCache().shiftTs();
			};

			mountConsumer = generic -> {
				((CocClientEngine) generic).getCurrentCache().mount();
			};

			unmountConsumer = generic -> {
				((CocClientEngine) generic).getCurrentCache().unmount();
			};

		}

		@Override
		public AttributeUiFunctions<Generic> apply(Generic attribute) {
			return new AttributeFunctions(this, attribute);
		}
	}

	public static class AttributeFunctions extends AttributeUiFunctions<Generic> {
		public AttributeFunctions(GsUiFunctions uiFunctions, Generic attribute) {
			genericGetter = uiFunctions.genericGetter;
			genericSetter = uiFunctions.genericSetter;
			genericComponents = uiFunctions.genericComponents;
			genericComponentGetter = uiFunctions.genericComponentGetter;
			genericComponentSetter = uiFunctions.genericComponentSetter;
			attributeAddAction = uiFunctions.attributeAddAction;
			attributeConverter = uiFunctions.attributeConverter;
			attributeGetter = uiFunctions.attributeGetter;
			typeAttributes = uiFunctions.typeAttributes;
			genericSubInstances = uiFunctions.genericSubInstances;
			removeConsumer = uiFunctions.removeConsumer;
			flushConsumer = uiFunctions.flushConsumer;

			addAction = attributeAddAction.apply(attribute);
			converter = attributeConverter.apply(attribute);
			linksGetter = attributeGetter.apply(attribute);
		}

		@Override
		public AttributeFunctions apply(Generic t) {
			throw new IllegalStateException();
		}
	}
}