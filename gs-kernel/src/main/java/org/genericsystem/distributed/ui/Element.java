package org.genericsystem.distributed.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

public class Element<COMPONENT extends Element<COMPONENT, N>, N> {
	public final Class<N> nodeClass;
	public final List<MetaBinding<N, ?>> metaBindings = new ArrayList<>();
	public final List<Binding<N, ?, ?>> bindings = new ArrayList<>();
	private final Element<?, ?> parent;
	private final List<Element<?, ?>> children = new ArrayList<>();
	final Function<?, ObservableList<?>> getGraphicChildren;
	private List<Boot<N>> boots = new ArrayList<>();

	@Override
	public String toString() {
		return "Element<" + nodeClass.getSimpleName() + ">";
	}

	public <PARENTNODE> Element(Class<N> nodeClass, Function<PARENTNODE, ObservableList<?>> getGraphicChildren) {
		this(null, nodeClass, getGraphicChildren);
	}

	protected <PARENTNODE, W> Element(Element<?, PARENTNODE> parent, Class<N> nodeClass, Function<PARENTNODE, ObservableList<?>> getGraphicChildren) {
		this.nodeClass = nodeClass;
		this.parent = parent;
		this.getGraphicChildren = getGraphicChildren;
		if (parent != null)
			parent.<N> getChildren().add(this);
		initChildren();
	}

	protected void initChildren() {

	}

	@SuppressWarnings("unchecked")
	public <VALUE> COMPONENT addBoot(Function<N, Property<VALUE>> applyOnNode, VALUE value) {
		this.boots.add(Boot.setProperty(applyOnNode, value));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <VALUE> COMPONENT addObservableListBoot(Function<N, ObservableList<VALUE>> applyOnNode, VALUE value) {
		this.boots.add(Boot.addProperty(applyOnNode, value));
		return (COMPONENT) this;
	}

	public List<Boot<N>> getBootList() {
		return boots;
	}

	@SuppressWarnings("unchecked")
	public <M, W> COMPONENT addBidirectionalBinding(Function<N, Property<W>> applyOnNode, Function<M, Property<W>> applyOnModel) {
		bindings.add(Binding.bindBiDirectionalProperty(applyOnModel, applyOnNode));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M, T> COMPONENT addBinding(Function<N, Property<T>> applyOnNode, Function<M, ObservableValue<T>> applyOnModel) {
		bindings.add(Binding.bindProperty(applyOnModel, applyOnNode));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M, T> COMPONENT setObservableListBinding(Function<N, Property<ObservableList<T>>> applyOnNode, Function<M, ObservableList<T>> applyOnModel) {
		bindings.add(Binding.bindObservableList(applyOnModel, applyOnNode));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M, T> COMPONENT addActionBinding(Function<N, Property<T>> applyOnNode, Consumer<M> applyOnModel) {
		bindings.add(Binding.bindAction(applyOnModel, applyOnNode));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M, T> COMPONENT addReversedBinding(Function<N, ObservableValue<T>> applyOnNode, Function<M, Property<T>> applyOnModel) {
		bindings.add(Binding.bindReversedProperty(applyOnModel, applyOnNode));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M> COMPONENT addObservableListToObservableValueBinding(Function<N, ObservableList<String>> applyOnNode, Function<M, ObservableValue<String>> applyOnModel) {
		bindings.add(Binding.bindObservableListToObservableValue(applyOnModel, applyOnNode));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M, T> COMPONENT addObservableListBinding(Function<N, ObservableList<T>> applyOnNode, Function<M, ObservableValue<Boolean>> applyOnModel, T styleClass) {
		bindings.add(Binding.bindObservableList(applyOnModel, styleClass, applyOnNode));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M extends Model, T extends Model> COMPONENT forEach(Function<M, ObservableList<T>> applyOnModel) {
		metaBindings.add(MetaBinding.forEach(applyOnModel));
		return (COMPONENT) this;
	}

	@SuppressWarnings("unchecked")
	public <M extends Model, T extends Model> COMPONENT select(Function<M, ObservableValue<T>> applyOnModel) {
		metaBindings.add(MetaBinding.selector(applyOnModel));
		return (COMPONENT) this;
	}

	protected N createNode(Object parent) {
		System.out.println("Element createNode");
		try {
			return nodeClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <CHILDNODE> List<Element<?, CHILDNODE>> getChildren() {
		return (List) children;
	}

	public Element<?, ?> getParent() {
		return parent;
	}
}
