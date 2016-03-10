package org.genericsystem.distributed.cacheonclient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import org.genericsystem.api.core.Snapshot;
import org.genericsystem.api.core.exceptions.ConcurrencyControlException;
import org.genericsystem.api.core.exceptions.OptimisticLockConstraintViolationException;
import org.genericsystem.common.CheckedContext;
import org.genericsystem.common.Container;
import org.genericsystem.common.Generic;
import org.genericsystem.common.Vertex;
import org.genericsystem.distributed.cacheonclient.ClientEngine.ClientEngineHandler;

public class FrontEndTransaction extends CheckedContext implements AsyncITransaction {

	private final long ts;

	protected FrontEndTransaction(ClientEngine engine, long ts) {
		super(engine);
		this.ts = ts;
	}

	protected FrontEndTransaction(ClientEngine engine) {
		this(engine, engine.pickNewTs());
	}

	@Override
	public long getTs() {
		return ts;
	}

	@Override
	public void apply(Snapshot<Generic> removes, Snapshot<Generic> adds) throws ConcurrencyControlException, OptimisticLockConstraintViolationException {
		assert adds.stream().allMatch(add -> add.getBirthTs() == Long.MAX_VALUE);
		getRoot().getServer().apply(getTs(), removes.stream().mapToLong(g -> g.getTs()).toArray(), adds.stream().map(g -> g.getVertex()).toArray(Vertex[]::new));
		removes.forEach(this::invalid);// Not efficient ! plug and unplug is
										// better
		adds.forEach(this::invalid);// Not efficient !
		adds.forEach(this::giveBirth);
	}

	private void giveBirth(Generic generic) {
		assert Long.MAX_VALUE == generic.getBirthTs();
		((ClientEngineHandler) generic.getProxyHandler()).birthTs = getTs();
	}

	private void invalid(Generic generic) {
		generic.getComponents().forEach(component -> dependenciesMap.remove(component));
		generic.getSupers().forEach(superG -> dependenciesMap.remove(superG));
		dependenciesMap.remove(generic.getMeta());
		dependenciesMap.remove(generic);

		generic.getComponents().forEach(component -> dependenciesPromiseMap.remove(component));
		generic.getSupers().forEach(superG -> dependenciesPromiseMap.remove(superG));
		dependenciesPromiseMap.remove(generic.getMeta());
		dependenciesPromiseMap.remove(generic);
	}

	@Override
	public ClientEngine getRoot() {
		return (ClientEngine) super.getRoot();
	}

	private Map<Generic, Snapshot<Generic>> dependenciesMap = new HashMap<>();

	@Override
	public Snapshot<Generic> getDependencies(Generic generic) {
		Snapshot<Generic> dependencies = dependenciesMap.get(generic);
		if (dependencies == null) {
			dependencies = new Container(Arrays.stream(getRoot().getServer().getDependencies(getTs(), generic.getTs())).map(vertex -> getRoot().getGenericByVertex(vertex)));
			Snapshot<Generic> result = dependenciesMap.put(generic, dependencies);
			assert result == null;
		}
		return dependencies;
	}

	private Map<Generic, CompletableFuture<Snapshot<Generic>>> dependenciesPromiseMap = new HashMap<>();

	@Override
	public CompletableFuture<Snapshot<Generic>> getDependenciesPromise(Generic generic) {
		CompletableFuture<Snapshot<Generic>> dependenciesPromise = dependenciesPromiseMap.get(generic);
		if (dependenciesPromise == null) {
			dependenciesPromise = getRoot().getServer().getDependenciesPromise(getTs(), generic.getTs()).thenApply(vertices -> new Container(Arrays.stream(vertices).map(vertex -> getRoot().getGenericByVertex(vertex))));
			dependenciesPromiseMap.put(generic, dependenciesPromise);
		}
		return dependenciesPromise;
	}

	@Override
	public Observable getInvalidator(Generic generic) {
		return new SimpleObjectProperty<Snapshot<Generic>>();
	}

	@Override
	public ObservableList<Generic> getObservableDependencies(Generic generic) {
		throw new UnsupportedOperationException();
	}
}

// @Override
// public ObservableSnapshot<Generic>
// getDependenciesObservableSnapshot(Generic generic) {
// return new
// CompletableObservableSnapshot2<>(getRoot().getServer().getDependenciesPromise(getTs(),
// generic.getTs()), vertex -> getRoot().getGenericByVertex(vertex));
// }

// @Override
// public Observable getInvalidator(Generic generic) {
// ObjectProperty<Snapshot<Generic>> objectProperty = new SimpleObjectProperty<Snapshot<Generic>>();
// getDependenciesPromise(generic).thenAcceptAsync(snapshot -> {
// try {
// Thread.sleep(200);
// } catch (Exception e) {
// e.printStackTrace();
// }
// objectProperty.set(snapshot);
// });
// return objectProperty;
// }