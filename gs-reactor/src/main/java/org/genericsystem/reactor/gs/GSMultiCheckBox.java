package org.genericsystem.reactor.gs;

import org.genericsystem.common.Generic;
import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.gstag.HtmlLabel;
import org.genericsystem.reactor.model.ObservableListExtractor;
import org.genericsystem.reactor.model.StringExtractor;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;

public class GSMultiCheckBox extends GSSection {

	public GSMultiCheckBox(Tag parent) {
		this(parent, FlexDirection.COLUMN);
	}

	public GSMultiCheckBox(Tag parent, FlexDirection flexDirection) {
		super(parent, flexDirection);
		addStyle("flex-wrap", "wrap");

		new GenericRow(this) {

			private Tag checkbox;

			{
				addStyle("flex", "1");
				addStyle("justify-content", "center");
				addStyle("align-items", "center");
				forEach(gs -> ObservableListExtractor.SUBINSTANCES.apply(ObservableListExtractor.COMPONENTS.apply(gs).filtered(g -> !g.equals(gs[2])).stream().toArray(Generic[]::new)));
				addPrefixBinding(model -> {
					if ("Color".equals(StringExtractor.SIMPLE_CLASS_EXTRACTOR.apply(model.getGeneric().getMeta())))
						addStyle(model, "background-color", getGenericStringProperty(model).getValue());
				});

				checkbox = new GSCheckBoxWithValue(this) {
					{
						initValueProperty(context -> context.getGenerics()[2].getLink(context.getGenerics()[1], context.getGeneric()) != null ? true : false);
						storeProperty("exists", context -> {
							ObservableValue<Boolean> exists = Bindings.createBooleanBinding(() -> context.getGenerics()[2].getObservableLink(context.getGenerics()[1], context.getGeneric()).getValue() != null ? true : false,
									context.getGenerics()[2].getObservableLink(context.getGenerics()[1], context.getGeneric()));
							exists.addListener((o, v, nva) -> getConvertedValueProperty(context).setValue(nva));
							return exists;
						});
						addConvertedValueChangeListener((context, nva) -> {
							if (Boolean.TRUE.equals(nva))
								context.getGenerics()[2].setHolder(context.getGenerics()[1], null, context.getGeneric());
							if (Boolean.FALSE.equals(nva)) {
								Generic link = context.getGenerics()[2].getLink(context.getGenerics()[1], context.getGeneric());
								if (link != null)
									link.remove();
							}
						});
					}
				};

				new HtmlLabel(this) {
					{
						addStyle("flex", "1");
						addStyle("text-align", "center");
						addPrefixBinding(context -> addAttribute(context, "for", context.getHtmlDomNode(checkbox).getId()));
						bindText();
					}
				};
			}
		};
	}

}
