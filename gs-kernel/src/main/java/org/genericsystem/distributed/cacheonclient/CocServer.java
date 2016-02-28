package org.genericsystem.distributed.cacheonclient;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

import org.genericsystem.distributed.AbstractGSClient;
import org.genericsystem.distributed.AbstractGSServer;
import org.genericsystem.distributed.GSBuffer;
import org.genericsystem.distributed.GSDeploymentOptions;
import org.genericsystem.distributed.WebSocketsServer;
import org.genericsystem.kernel.BasicEngine;

public class CocServer extends AbstractGSServer<BasicEngine> {

	public static void main(String[] args) {
		new CocServer(new GSDeploymentOptions()).start();
	}

	public CocServer(GSDeploymentOptions options) {
		super(options);
	}

	@Override
	protected Buffer getReplyBuffer(int methodId, int op, BasicEngine root, GSBuffer gsBuffer) {
		GSBuffer replyBuffer = new GSBuffer().appendInt(op);
		System.out.println("REPLY BUFFER : " + methodId + " " + op);
		switch (methodId) {
		case AbstractGSClient.PICK_NEW_TS:
			return replyBuffer.appendLongThrowException(() -> root.pickNewTs());
		case AbstractGSClient.GET_DEPENDENCIES:
			return replyBuffer.appendGSVertexArrayThrowException(() -> root.getDependencies(gsBuffer.getLong(), gsBuffer.getLong()));
		case AbstractGSClient.GET_VERTEX:
			return replyBuffer.appendGSVertexThrowException(() -> root.getVertex(gsBuffer.getLong()));
		case AbstractGSClient.APPLY:
			return replyBuffer.appendLongThrowException(() -> {
				root.apply(gsBuffer.getLong(), gsBuffer.getGSLongArray(), gsBuffer.getGSVertexArray());
				return 0L;
			});
		default:
			return replyBuffer.appendLongThrowException(() -> {
				throw new IllegalStateException("unable to find method:" + methodId + " ");
			});
		}
	}

	@Override
	protected BasicEngine buildRoot(String value, String persistentDirectoryPath, Class[] userClasses) {
		return new BasicEngine(value, persistentDirectoryPath, userClasses);
	}

	@Override
	protected WebSocketsServer<BasicEngine> buildWebSocketsServer(GSDeploymentOptions options) {
		return new WebSocketsServer<BasicEngine>(this, options.getHost(), options.getPort()) {
			@Override
			public Handler<Buffer> getHandler(BasicEngine root, ServerWebSocket socket) {
				return buffer -> {
					GSBuffer gsBuffer = new GSBuffer(buffer);
					int methodId = gsBuffer.getInt();
					int op = gsBuffer.getInt();
					socket.writeBinaryMessage(CocServer.this.getReplyBuffer(methodId, op, root, gsBuffer));
				};
			}
		};
	}
}
