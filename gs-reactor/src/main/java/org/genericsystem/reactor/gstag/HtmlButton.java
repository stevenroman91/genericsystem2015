package org.genericsystem.reactor.gstag;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.genericsystem.common.Generic;
import org.genericsystem.reactor.HtmlDomNode.ActionHtmlNode;
import org.genericsystem.reactor.ReactorStatics;
import org.genericsystem.reactor.gs.GSTag;
import org.genericsystem.reactor.model.GenericModel;
import org.genericsystem.reactor.modelproperties.ActionDefaults;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

/**
 * @author Nicolas Feybesse
 *
 */
public class HtmlButton extends GSTag implements ActionDefaults<GenericModel> {

	public HtmlButton(GSTag parent) {
		super(parent, "button");
	}

	@Override
	protected ActionHtmlNode createNode(String parentId) {
		return new ActionHtmlNode(parentId);
	}

	public List<ObservableValue<Boolean>> getInvalidList(GenericModel model) {
		return this.<List<ObservableValue<Boolean>>> getProperty(ReactorStatics.INVALID_LIST, model).getValue();
	}

	public Map<Generic, Property<Serializable>> getHoldersMap(GenericModel model) {
		return this.<Map<Generic, Property<Serializable>>> getProperty(ReactorStatics.HOLDERS_MAP, model).getValue();
	}

	public Map<Generic, List<Property<GenericModel>>> getComponentsMap(GenericModel model) {
		return this.<Map<Generic, List<Property<GenericModel>>>> getProperty(ReactorStatics.COMPONENTS_MAP, model).getValue();
	}
}
