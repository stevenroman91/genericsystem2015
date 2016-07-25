package org.genericsystem.reactor.gs;

import io.vertx.core.http.ServerWebSocket;

import org.genericsystem.reactor.ViewContext.RootViewContext;
import org.genericsystem.reactor.appserver.PersistentApplication.App;
import org.genericsystem.reactor.model.GenericModel;

public class GSApp extends GSSection implements App<GenericModel> {

	private final ServerWebSocket webSocket;
	private RootViewContext<GenericModel> rootViewContext;

	public GSApp(ServerWebSocket webSocket) {
		super(null, FlexDirection.COLUMN);
		this.webSocket = webSocket;
	}

	@Override
	public GSApp init(GenericModel rootModelContext, String rootId) {
		HtmlDomNode rootNode = new HtmlDomNode(rootId);
		rootNode.sendAdd(0);
		rootViewContext = new RootViewContext<GenericModel>(rootModelContext, this, rootNode);
		return this;
	}

	@Override
	public ServerWebSocket getWebSocket() {
		return webSocket;
	}

	public HtmlDomNode getNodeById(String id) {
		return rootViewContext.getNodeById(id);
	}

	@Override
	protected HtmlDomNode createNode(String parentId) {
		throw new UnsupportedOperationException();
	}
}
