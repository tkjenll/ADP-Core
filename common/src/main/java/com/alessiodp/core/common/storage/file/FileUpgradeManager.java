package com.alessiodp.core.common.storage.file;

import com.alessiodp.core.common.ADPPlugin;
import com.alessiodp.core.common.storage.StorageType;
import com.alessiodp.core.common.storage.interfaces.IDatabaseFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ninja.leaping.configurate.ConfigurationNode;

@RequiredArgsConstructor
public abstract class FileUpgradeManager {
	@NonNull protected final ADPPlugin plugin;
	@NonNull protected final IDatabaseFile databaseFile;
	@NonNull protected final StorageType storageType;
	
	public void checkForUpgrades() {
		ConfigurationNode node = databaseFile.getRootNode().getNode("database-version");
		if (node.getValue() != null) {
			upgradeDatabase(node.getInt());
		}
	}
	
	/**
	 * Upgrade the database
	 *
	 * @param currentVersion the current version of the database
	 */
	protected abstract void upgradeDatabase(int currentVersion);
}
