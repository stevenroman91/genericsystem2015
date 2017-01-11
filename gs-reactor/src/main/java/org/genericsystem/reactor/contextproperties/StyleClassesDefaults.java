package org.genericsystem.reactor.contextproperties;

import org.genericsystem.reactor.Context;
import org.genericsystem.reactor.Tag;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public interface StyleClassesDefaults extends ContextProperty {

	public static final String STYLE_CLASSES = "styleClasses";

	default ObservableSet<String> getDomNodeStyleClasses(Context model) {
		if (!model.containsProperty((Tag) this, STYLE_CLASSES)) {
			createNewInitializedProperty(STYLE_CLASSES, model, m -> {
				ObservableSet<String> styleClasses = FXCollections.observableSet();
				styleClasses.addListener(model.getHtmlDomNode((Tag) this).getStyleClassesListener());
				return styleClasses;
			});
		}
		return (ObservableSet<String>) getProperty(STYLE_CLASSES, model).getValue();
	}
}
