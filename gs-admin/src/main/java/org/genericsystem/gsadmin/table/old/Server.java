package org.genericsystem.gsadmin.table.old;

import org.genericsystem.admin.model.Car;
import org.genericsystem.admin.model.CarColor;
import org.genericsystem.admin.model.Color;
import org.genericsystem.admin.model.Power;
import org.genericsystem.distributed.GSDeploymentOptions;
import org.genericsystem.distributed.cacheonclient.CocServer;
import org.genericsystem.kernel.Statics;

public class Server {
	public static void main(String[] args) {
		CocServer server = new CocServer(new GSDeploymentOptions(Statics.ENGINE_VALUE, 8082, "test").addClasses(Car.class, Power.class, CarColor.class, Color.class));
		server.start();
	}
}
