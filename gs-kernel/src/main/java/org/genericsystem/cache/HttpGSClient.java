package org.genericsystem.cache;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

public class HttpGSClient extends AbstractGSClient {

	private HttpClient httpClient;
	private final String path;

	HttpGSClient(String host, int port, String path) {
		httpClient = GSVertx.vertx().getVertx().createHttpClient(new HttpClientOptions().setDefaultPort(port).setDefaultHost(host != null ? host : HttpClientOptions.DEFAULT_DEFAULT_HOST));
		this.path = path;
	}

	@Override
	<T> void send(Buffer buffer, Handler<Buffer> responseHandler) {
		httpClient.post(path, reponse -> reponse.bodyHandler(responseHandler)).exceptionHandler(e -> {
			System.out.println("Discard http request because of : ");
			e.printStackTrace();
		}).end(buffer);
	}

	@Override
	public void close() {
		System.out.println("Close httpclient");
		if (httpClient != null)
			httpClient.close();
		httpClient = null;
	}
}
