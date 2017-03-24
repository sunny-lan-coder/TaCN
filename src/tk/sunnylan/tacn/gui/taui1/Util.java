package tk.sunnylan.tacn.gui.taui1;

import java.lang.reflect.Field;

import com.jfoenix.controls.JFXDrawer;

import javafx.animation.Transition;

public class Util {
	public static void antiAlias() {
		System.setProperty("prism.lcdtext", "false");
		System.setProperty("prism.text", "t2k");
	}

	public static Transition getDrawerTransition(JFXDrawer drawer) {
		try {
			Class<?> c = drawer.getClass();
			Field drawerTrans = c.getDeclaredField("drawerTransition");
			drawerTrans.setAccessible(true);
			return (Transition) drawerTrans.get(drawer);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
