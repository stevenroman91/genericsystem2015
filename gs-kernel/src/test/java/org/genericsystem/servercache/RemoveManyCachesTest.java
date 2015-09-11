package org.genericsystem.servercache;

import org.genericsystem.common.Cache;
import org.genericsystem.common.Generic;
import org.genericsystem.kernel.ServerEngine;
import org.testng.annotations.Test;

@Test
public class RemoveManyCachesTest extends AbstractTest {

	public void test001_simpleHolder() {
		ServerEngine engine = new ServerEngine();
		Cache cache = engine.getCurrentCache();
		Generic car = engine.addInstance("Car");
		Generic color = car.addAttribute("Color");
		cache.flush();
		Cache cache2 = engine.newCache().start();
		Generic myBmw = car.addInstance("myBmw");
		Generic myBmwRed = myBmw.addHolder(color, "red");

		assert myBmw.getHolders(color).contains(myBmwRed);
		assert myBmw.getHolders(color).size() == 1;

		myBmwRed.remove();

		assert myBmw.getHolders(color).size() == 0;
	}

	public void test002_simpleHolder() {
		ServerEngine engine = new ServerEngine();
		Cache cache = engine.getCurrentCache();
		Generic car = engine.addInstance("Car");
		Generic color = car.addAttribute("Color");
		cache.flush();
		Cache cache2 = engine.newCache().start();
		Generic myBmw2 = car.addInstance("myBmw");
		Generic myBmwRed2 = myBmw2.addHolder(color, "red");
		cache.start();
		cache.shiftTs();
		Generic myBmw = car.addInstance("myBmw");
		Generic myBmwRed = myBmw.addHolder(color, "red");
		assert myBmw.getHolders(color).contains(myBmwRed);
		assert myBmw.getHolders(color).size() == 1;

		myBmwRed.remove();

		assert myBmw.getHolders(color).size() == 0;
	}

}
