package com.alessiodp.core.common;

import com.alessiodp.core.common.addons.AddonManager;
import com.alessiodp.core.common.addons.internal.JsonHandler;
import com.alessiodp.core.common.addons.internal.TitleHandler;
import com.alessiodp.core.common.bootstrap.ADPBootstrap;
import com.alessiodp.core.common.bootstrap.AbstractADPPlugin;
import com.alessiodp.core.common.libraries.LibraryManager;
import com.alessiodp.core.common.libraries.LibraryUsage;
import com.alessiodp.core.common.logging.LoggerManager;
import com.alessiodp.core.common.addons.internal.ADPUpdater;
import com.alessiodp.core.common.commands.CommandManager;
import com.alessiodp.core.common.configuration.ConfigurationManager;
import com.alessiodp.core.common.configuration.Constants;
import com.alessiodp.core.common.messaging.ADPMessenger;
import com.alessiodp.core.common.players.LoginAlertsManager;
import com.alessiodp.core.common.scheduling.ADPScheduler;
import com.alessiodp.core.common.storage.DatabaseManager;
import com.alessiodp.core.common.utils.IPlayerUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public abstract class ADPPlugin extends AbstractADPPlugin {
	// Plugin fields
	@Getter private static ADPPlugin instance;
	@Getter @Setter private boolean isPluginDisabled;
	
	// Common fields
	@Getter protected AddonManager addonManager;
	@Getter protected CommandManager commandManager;
	@Getter protected ConfigurationManager configurationManager;
	@Getter protected DatabaseManager databaseManager;
	@Getter protected LoggerManager loggerManager;
	@Getter protected LoginAlertsManager loginAlertsManager;
	@Getter protected ADPMessenger messenger;
	@Getter protected ADPScheduler scheduler;
	
	// Utils
	@Getter protected ADPUpdater adpUpdater;
	@Getter protected JsonHandler jsonHandler;
	@Getter protected TitleHandler titleHandler;
	@Getter protected IPlayerUtils playerUtils;
	
	protected ADPPlugin(ADPBootstrap bootstrap) {
		super(bootstrap);
	}
	
	/**
	 * On plugin load
	 */
	public void loading() {
		instance = this;
		isPluginDisabled = false;
		
		LibraryManager libraryManager = new LibraryManager(this);
		if (!libraryManager.init()) {
			logConsole(Constants.DEBUG_PLUGIN_LOADING_FAILED, LogLevel.ERROR);
			isPluginDisabled = true;
		}
	}
	
	/**
	 * On plugin enable
	 */
	public void enabling() {
		if (!isPluginDisabled) {
			// Init
			logConsole(String.format(Constants.DEBUG_PLUGIN_ENABLING, this.getPluginName(), this.getVersion()));
			
			// Check if plugin can be loaded
			canBeLoaded();
			
			// Pre handle
			if (!isPluginDisabled)
				preHandle();
			
			// Handle
			if (!isPluginDisabled)
				handle();
			
			// Post handle
			if (!isPluginDisabled)
				postHandle();
			
			if (isPluginDisabled) {
				super.getBootstrap().stopPlugin();
			} else {
				getLoggerManager().log(String.format(Constants.DEBUG_PLUGIN_ENABLED, this.getPluginName(), this.getVersion()), true);
			}
		}
	}
	
	/**
	 * On plugin disable
	 */
	public void disabling() {
		logConsole(String.format(Constants.DEBUG_PLUGIN_DISABLING, this.getPluginName()));
		
		onDisabling();
		
		if (databaseManager != null) {
			// This is not a force close
			getDatabaseManager().stop();
		}
		
		if (scheduler != null)
			scheduler.shutdown();
		
		if (getLoggerManager() != null)
			getLoggerManager().log(String.format(Constants.DEBUG_PLUGIN_DISABLED_LOG, getPluginName()), false);
		
		logConsole(String.format(Constants.DEBUG_PLUGIN_DISABLED, this.getPluginName()));
	}
	
	/**
	 * Used to disable other non-common managers
	 */
	public abstract void onDisabling();
	
	/**
	 * Can the plugin be loaded?
	 *
	 * @return True if can be loaded
	 */
	protected boolean canBeLoaded() {
		return true;
	}
	
	public static int getJavaVersion() {
		return getJavaVersion(System.getProperty("java.version"));
	}
	
	public static int getJavaVersion(String fullVersion) {
		String version = fullVersion;
		if(version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf(".");
			if (dot != -1) { version = version.substring(0, dot); }
			int dash = version.indexOf("-");
			if (dash != -1) { version = version.substring(0, dash); }
		}
		try {
			return Integer.parseInt(version);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Preparation before the main handle method
	 * Initialize necessary classes
	 */
	protected final void preHandle() {
		loggerManager = new LoggerManager(this);
		loginAlertsManager = new LoginAlertsManager();
		initializeCore();
	}
	
	/**
	 * Load necessaries classes and initialize others
	 */
	protected final void handle() {
		loadCore();
		
		adpUpdater = new ADPUpdater(this);
		initializeJsonHandler();
		initializeTitleHandler();
	}
	
	/**
	 * Load any other class
	 */
	protected abstract void postHandle();
	
	/**
	 * Used to initialize: scheduler, database and configuration
	 */
	protected abstract void initializeCore();
	
	/**
	 * Use to load: scheduler, database and configuration
	 */
	protected abstract void loadCore();
	
	/**
	 * Reload the configuration
	 */
	public abstract void reloadConfiguration();
	
	/**
	 * Initialize Json Handler instance
	 */
	protected abstract void initializeJsonHandler();
	
	/**
	 * Initialize Title Handler instance
	 */
	protected abstract void initializeTitleHandler();
	
	/**
	 * Returns a list of usages of libraries to load the plugin
	 *
	 * @return Returns a list of usages
	 */
	public abstract List<LibraryUsage> getLibrariesUsages();
}
