package org.genericsystem.reactor.composite;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.genericsystem.common.Generic;
import org.genericsystem.defaults.tools.Transformation2;
import org.genericsystem.reactor.Element;
import org.genericsystem.reactor.Model;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Nicolas Feybesse
 *
 * @param <M>
 */
public class CompositeModel extends Model {

	private final Generic[] generics;
	private final StringExtractor stringExtractor;

	public CompositeModel(Generic[] generics, StringExtractor stringExtractor) {
		assert stringExtractor != null;
		this.generics = generics;
		this.stringExtractor = stringExtractor;
	}

	Generic[] getGenerics() {
		return generics;
	}

	public static Generic[] addToGenerics(Generic generic, Generic[] generics) {
		Generic[] result = new Generic[generics.length + 1];
		result[0] = generic;
		System.arraycopy(generics, 0, result, 1, generics.length);
		return result;
	}

	public Generic getGeneric() {
		return generics[0];
	}

	public ObservableValue<String> getString() {
		return new ReadOnlyStringWrapper(stringExtractor.apply(getGenerics()[0]));
	}

	public void remove() {
		getGeneric().remove();
	}

	public void select() {
		System.out.println("select click!!!");
	}

	@FunctionalInterface
	public static interface ObservableListExtractor extends Function<Generic[], ObservableList<Generic>> {

		public static ObservableListExtractor from(Class<?>... genericClasses) {
			return gs -> FXCollections.observableArrayList(Arrays.stream(genericClasses).map(gs[0].getRoot()::<Generic> find).collect(Collectors.toList()));
		}

		public static final ObservableListExtractor INSTANCES = generics -> {
			System.out.println("INSTANCES : " + Arrays.toString(generics) + " " + generics[0].getObservableSubInstances());
			return generics[0].getObservableSubInstances();
		};

		public static final ObservableListExtractor ATTRIBUTES = generics -> {
			System.out.println("ATTRIBUTES : " + Arrays.toString(generics) + " " + generics[0].getObservableAttributes());
			return generics[0].getObservableAttributes();
		};
		public static final ObservableListExtractor HOLDERS = generics -> {
			System.out.println("HOLDERS : " + Arrays.toString(generics) + " " + generics[1].getObservableHolders(generics[0]));
			return generics[1].getObservableHolders(generics[0]);
		};
	}

	@FunctionalInterface
	public static interface StringExtractor extends Function<Generic, String> {
		public static final StringExtractor EXTRACTOR = generic -> Objects.toString(generic.getValue());
		public static final StringExtractor SIMPLE_CLASS_EXTRACTOR = generic -> {
			Serializable value = generic.getValue();
			return value instanceof Class ? ((Class<?>) value).getSimpleName() : Objects.toString(value);
		};
		public static final StringExtractor MANAGEMENT = g -> StringExtractor.SIMPLE_CLASS_EXTRACTOR.apply(g) + "(s) Management";
	}

	@FunctionalInterface
	public static interface Builder<M extends Model> extends Function<Generic[], M> {

	}

	@FunctionalInterface
	public interface ModelConstructor<M extends Model> {
		M build(Generic[] generics, StringExtractor stringExtractor);
	}

	private Set<ObservableList<CompositeModel>> observableLists = new HashSet<ObservableList<CompositeModel>>();

	public <M extends CompositeModel> ObservableList<CompositeModel> getObservableList(StringExtractor stringExtractor,
			ObservableListExtractor observableListExtractor, ModelConstructor<CompositeModel> constructor) {
		ObservableList<CompositeModel> observableList = new Transformation2<Generic, CompositeModel>(observableListExtractor.apply(generics),
				generic -> constructor.build(CompositeModel.addToGenerics(generic, generics), stringExtractor));
		observableLists.add(observableList);// Prevents garbaging
		return observableList;
	}

	public <M extends CompositeModel> ObservableList<CompositeModel> getObservableList(StringExtractor stringExtractor, Supplier<Generic> genericSupplier,
			ModelConstructor<CompositeModel> constructor) {
		return getObservableList(stringExtractor, gs -> FXCollections.singletonObservableList(genericSupplier.get()), constructor);
	}

	public <M extends CompositeModel> ObservableList<CompositeModel> getObservableList(Element<?, ?> element, StringExtractor stringExtractor,
			Class<?> genericClass, ModelConstructor<CompositeModel> constructor) {
		return getObservableList(stringExtractor, () -> getGenerics()[0].getRoot().find(genericClass), constructor);
	}

	private Map<Element<?, ?>, Map<String, Property<String>>> observableStyles = new HashMap<Element<?, ?>, Map<String, Property<String>>>() {
		private static final long serialVersionUID = -1827306835524845605L;

		@Override
		public Map<String, Property<String>> get(Object key) {
			Map<String, Property<String>> result = super.get(key);
			if (result == null)
				put((Element<?, ?>) key, result = new HashMap<String, Property<String>>() {
					private static final long serialVersionUID = -8866241510145377825L;

					@Override
					public Property<String> get(Object key) {
						Property<String> result = super.get(key);
						if (result == null)
							put((String) key, result = new SimpleStringProperty());
						return result;
					};
				});
			return result;
		};
	};

	public ObservableValue<String> getObservableStyle(Element<?, ?> element, String propertyName, String initialValue) {
		Property<String> result = getStyleProperty(element, propertyName);
		result.setValue(initialValue);
		return result;
	}

	public Property<String> getStyleProperty(Element<?, ?> element, String propertyName) {
		return observableStyles.get(element).get(propertyName);
	}

	public void flush() {
		getGeneric().getCurrentCache().flush();
	}

	public void cancel() {
		getGeneric().getCurrentCache().clear();
	}

}
