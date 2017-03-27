package tk.sunnylan.tacn.tst;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DEBUG_CONFIG {
	public static final String cachepath = new File("").getAbsolutePath() + "\\cache\\";
	// REMEMBER TO SET THIS TO FALSE!!! WILL DISABLE SSL IF SET TO TRUE
	public static final boolean USE_PROXY = false;
	public static final boolean DEBUG_MODE = false;
	private static final boolean LOG_TO_CONSOLE = false;
	public static final String PROXY_HOST = "127.0.0.1";
	public static final int PROXY_PORT = 8888;
	private static final Level LOG_LEVEL = Level.ALL;

	public static void initDebug() {
		System.out.println("initing debug");
		try {
			if (!LOG_TO_CONSOLE) {
				// suppress the logging output to the console
				Logger rootLogger = Logger.getLogger("");
				rootLogger.removeHandler(rootLogger.getHandlers()[0]);

			}
			if (!Files.isDirectory(Paths.get(cachepath))) {
				Files.createDirectory(Paths.get(cachepath));
			}
			FileHandler fh = new FileHandler(cachepath + "\\" + "log.xml");
			
			Logger.getLogger("").addHandler(fh);
			Logger.getLogger("").setLevel(LOG_LEVEL);
		} catch (SecurityException | IOException e) {
			throw new RuntimeException("Could not initialize logger");
		}

		if (DEBUG_CONFIG.USE_PROXY) {
			SSLUtilities.trustAllHostnames();
			SSLUtilities.trustAllHttpsCertificates();
			System.setProperty("http.proxyHost", DEBUG_CONFIG.PROXY_HOST); // set
																			// proxy
																			// server
			System.setProperty("http.proxyPort", DEBUG_CONFIG.PROXY_PORT + ""); // set
																				// proxy
																				// port
			System.setProperty("https.proxyHost", DEBUG_CONFIG.PROXY_HOST); // set
																			// proxy
																			// server
			System.setProperty("https.proxyPort", DEBUG_CONFIG.PROXY_PORT + ""); // set
																					// proxy
																					// port
		}
	}
}
