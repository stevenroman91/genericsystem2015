package org.genericsystem.reactor.html;

import org.genericsystem.reactor.Model;
import org.genericsystem.reactor.Tag;

/**
 * @author Nicolas Feybesse
 *
 */
public class HtmlSection<M extends Model> extends Tag<M> {

	public HtmlSection(Tag<?> parent) {
		super(parent, "section");
	}

	@Override
	protected HtmlDomNode createNode(String parentId) {
		return new HtmlDomNode(parentId);
	}
}
