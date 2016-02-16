package org.genericsystem.distributed.cacheonserver.ui.js;

import io.vertx.core.http.ServerWebSocket;
import java.util.function.Function;
import javafx.collections.ObservableList;
import org.genericsystem.distributed.cacheonserver.ui.js.utils.Utils;

public class HtmlElement extends Element<HtmlNode> {

	public HtmlElement(HtmlElement parent) {
		super(parent, HtmlNode.class, Utils.getClassChildren(parent));

	}

	public HtmlElement(Class<HtmlNode> nodeClass) {
		super(nodeClass, HtmlNode::getChildrenNode);
	}

	public ServerWebSocket getWebSocket() {
		return ((HtmlElement) getParent()).getWebSocket();
	}

	@Override
	public <M extends Model, T extends Model> Element<HtmlNode> forEach(Function<M, ObservableList<T>> applyOnModel) {
		return super.forEach(applyOnModel);
	}
}