package tk.sunnylan.tacn.tst;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DEBUG_CONFIG {
	public static final String logpath = new File("").getAbsolutePath() + "\\cache\\logs\\";
	private static final Logger logger = Logger.getLogger(DEBUG_CONFIG.class.getName());
	// REMEMBER TO SET THIS TO FALSE!!! WILL DISABLE SSL IF SET TO TRUE
	public static final boolean USE_PROXY = false;
	public static final boolean DEBUG_MODE = false;
	private static final boolean LOG_TO_CONSOLE = false;
	public static final String PROXY_HOST = "127.0.0.1";
	public static final int PROXY_PORT = 8888;
	private static final Level LOG_LEVEL = Level.ALL;
	private static final boolean LOG_FILTER_ON = true;

	public static void initDebug() {
		try {
			Logger rootLogger = Logger.getLogger("");
			if (!LOG_TO_CONSOLE) {
				rootLogger.removeHandler(rootLogger.getHandlers()[0]);
			}
			File targetFile = new File(logpath + "\\" + "log.xml");
			File parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
			    throw new IllegalStateException("Couldn't create dir: " + parent);
			}
			FileHandler fh = new FileHandler(logpath + "\\" + "log.xml");

			rootLogger.addHandler(fh);
			rootLogger.setLevel(LOG_LEVEL);
			if (LOG_FILTER_ON) {
				Filter filt = new Filter() {
					@Override
					public boolean isLoggable(LogRecord record) {
						if (record.getLevel() == Level.SEVERE)
							return true;
						if (record.getLoggerName().startsWith("tk.sunnylan.tacn"))
							return true;
						return false;
					}
				};
				fh.setFilter(filt);
			}
			

		} catch (SecurityException | IOException e) {
			throw new RuntimeException("Could not initialize logger");
		}

		if (DEBUG_CONFIG.USE_PROXY) {
			logger.info("WARNING: USING PROXY - CONNECTION MAY BE INSECURE");
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
