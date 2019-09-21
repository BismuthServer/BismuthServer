package carpet.bismuth.logging;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoggerRegistry {
	//statics to quickly asses if its worth even to call each one
	public static boolean __tps;
	public static boolean __mobcaps;
	// Map from logger names to loggers.
	private static final Map<String, Logger> loggerRegistry = new HashMap<>();
	// Map from player names to the set of names of the logs that player is subscribed to.
	private static final Map<String, Map<String, String>> playerSubscriptions = new HashMap<>();

	public static void initLoggers(MinecraftServer server) {
		registerLogger("tps", new Logger(server, "tps", null, null));
		registerLogger("mobcaps", new Logger(server, "mobcaps", "dynamic", new String[]{"dynamic", "overworld", "nether", "end"}));
	}

	/**
	 * Gets the logger with the given name. Returns null if no such logger exists.
	 */
	public static Logger getLogger(String name) {
		return loggerRegistry.get(name);
	}

	/**
	 * Gets the set of logger names.
	 */
	public static Set<String> getLoggerNames() {
		return loggerRegistry.keySet();
	}

	/**
	 * Subscribes the player with name playerName to the log with name logName.
	 */
	public static void subscribePlayer(String playerName, String logName, String option, LogHandler handler) {
		if (!playerSubscriptions.containsKey(playerName))
			playerSubscriptions.put(playerName, new HashMap<>());
		Logger log = loggerRegistry.get(logName);
		if (option == null)
			option = log.getDefault();
		playerSubscriptions.get(playerName).put(logName, option);
		log.addPlayer(playerName, option, handler);
	}

	/**
	 * Unsubscribes the player with name playerName from the log with name logName.
	 */
	public static void unsubscribePlayer(String playerName, String logName) {
		if (playerSubscriptions.containsKey(playerName)) {
			Map<String, String> subscriptions = playerSubscriptions.get(playerName);
			subscriptions.remove(logName);
			loggerRegistry.get(logName).removePlayer(playerName);
			if (subscriptions.size() == 0)
				playerSubscriptions.remove(playerName);
		}
	}

	/**
	 * If the player is not subscribed to the log, then subscribe them. Otherwise, unsubscribe them.
	 */
	public static boolean togglePlayerSubscription(String playerName, String logName, LogHandler handler) {
		if (playerSubscriptions.containsKey(playerName) && playerSubscriptions.get(playerName).containsKey(logName)) {
			unsubscribePlayer(playerName, logName);
			return false;
		} else {
			subscribePlayer(playerName, logName, null, handler);
			return true;
		}
	}

	/**
	 * Get the set of logs the current player is subscribed to.
	 */
	public static Map<String, String> getPlayerSubscriptions(String playerName) {
		if (playerSubscriptions.containsKey(playerName)) {
			return playerSubscriptions.get(playerName);
		}
		return null;
	}

	static void setAccess(Logger logger) {
		String name = logger.getLogName();
		boolean value = logger.hasOnlineSubscribers();
		try {
			Field f = LoggerRegistry.class.getDeclaredField("__" + name);
			f.setBoolean(null, value);
		} catch (Exception ignored) {
		}
	}

	/**
	 * Called when the server starts. Creates the logs used by Carpet mod.
	 */
	private static void registerLogger(String name, Logger logger) {
		loggerRegistry.put(name, logger);
		setAccess(logger);
	}

	public static void playerConnected(EntityPlayer player) {
		for (Logger log : loggerRegistry.values()) {
			log.onPlayerConnect(player);
		}

	}

	public static void playerDisconnected(EntityPlayer player) {
		for (Logger log : loggerRegistry.values()) {
			log.onPlayerDisconnect(player);
		}
	}
}
