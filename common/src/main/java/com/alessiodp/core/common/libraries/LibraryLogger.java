package com.alessiodp.core.common.libraries;

import com.alessiodp.core.common.ADPPlugin;
import io.github.slimjar.logging.ProcessLogger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LibraryLogger implements ProcessLogger {
	@NonNull protected final ADPPlugin plugin;
	
	private static final String[] BLACKLIST = {
			"Checksum matched",
			"Resolved",
			"Downloaded"
	};
	
	@Override
	public void log(String message, Object... args) {
		String finalMessage = message;
		for (int i=0; i < args.length; i++) {
			finalMessage = finalMessage.replace("{" + i + "}", args[i].toString());
		}
		
		boolean blacklisted = false;
		for (String bl : BLACKLIST) {
			if (finalMessage.startsWith(bl)) {
				blacklisted = true;
				break;
			}
		}
		
		if (!blacklisted)
			plugin.logConsole(finalMessage);
	}
}
