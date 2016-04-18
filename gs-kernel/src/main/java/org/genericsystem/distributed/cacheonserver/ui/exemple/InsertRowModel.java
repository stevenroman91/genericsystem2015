package org.genericsystem.distributed.cacheonserver.ui.exemple;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import org.genericsystem.common.Generic;
import org.genericsystem.defaults.tools.Transformation2;

public class InsertRowModel extends GenericModel {

	private final Property<String> inputString = new SimpleStringProperty();
	private final ObservableList<AttributeCellModel> attributeCellModels;

	public InsertRowModel(Generic type, ObservableList<Generic> attributes) {
		super(type);
		this.attributeCellModels = new Transformation2<>(attributes, att -> new AttributeCellModel(att));
	}

	public Property<String> getInputString() {
		return inputString;
	}

	public ObservableList<AttributeCellModel> getAttributeCellModels() {
		return attributeCellModels;
	}

	public void create() {
		Generic instance = getGeneric().setInstance(inputString.getValue());
		for (AttributeCellModel model : attributeCellModels)
			instance.setHolder(model.getGeneric(), model.getInputString().getValue());
	}

}
