package com.alessiodp.core.common.libraries;

import com.alessiodp.core.common.ADPPlugin;
import com.alessiodp.core.common.configuration.Constants;
import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.relocation.RelocationRule;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.DependencyData;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.mirrors.SimpleMirrorSelector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@RequiredArgsConstructor
public class LibraryManager {
	@NonNull protected final ADPPlugin plugin;
	
	public boolean init() {
		try {
			if (plugin.getLibrariesUsages().size() > 0) {
				plugin.logConsole(String.format(Constants.DEBUG_PLUGIN_LOADING, plugin.getPluginName(), plugin.getVersion()));
				
				DependencyData dependencyData = getDependencyData();
				
				ApplicationBuilder
						.appending(plugin.getPluginName())
						.downloadDirectoryPath(plugin.getFolder().resolve("libraries"))
						.dataProviderFactory((url) -> () -> dependencyData)
						.internalRepositories(getRepositories())
						.logger(new LibraryLogger(plugin))
						.build();
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	private List<Repository> getRepositories() throws MalformedURLException {
		return Arrays.asList(
				new Repository(new URL("https://repo.alessiodp.com/releases/")),
				new Repository(new URL("https://repo1.maven.org/maven2/"))
		);
	}
	
	public DependencyData getDependencyData() throws IOException {
		Properties versions = new Properties();
		versions.load(plugin.getResource("libraries.properties"));
		
		List<LibraryUsage> usages = plugin.getLibrariesUsages();
		ArrayList<Dependency> dependencies = new ArrayList<>();
		ArrayList<RelocationRule> relocations = new ArrayList<>();
		
		if (usages.contains(LibraryUsage.H2)) {
			dependencies.add(new Dependency(
					filter("com{}h2database"), "h2", versions.getProperty("h2"), null, Collections.emptyList()
			));
			relocations.add(new RelocationRule(filter("org{}h2"), plugin.getPackageName() + ".libs.h2"));
		}
		
		if (usages.contains(LibraryUsage.MYSQL) || usages.contains(LibraryUsage.MARIADB) || usages.contains(LibraryUsage.POSTGRESQL)) {
			dependencies.add(new Dependency(
					filter("com{}zaxxer"), "HikariCP", versions.getProperty("hikaricp"), null, Collections.emptyList()
			));
			dependencies.add(new Dependency(
					filter("org{}jdbi"), "jdbi3-core", versions.getProperty("jdbi"), null, Arrays.asList(
					new Dependency(
							filter("org{}antlr"), "antlr4-runtime", versions.getProperty("jdbi.antlr4"), null, Collections.emptyList()
					),
					new Dependency(
							filter("io{}leangen{}geantyref"), "geantyref", versions.getProperty("jdbi.geantyref"), null, Collections.emptyList()
					),
					new Dependency(
							filter("com{}github{}ben-manes{}caffeine"), "caffeine", versions.getProperty("jdbi.caffeine"), null, Collections.emptyList()
					)
			)
			));
			dependencies.add(new Dependency(
					filter("org{}jdbi"), "jdbi3-stringtemplate4", versions.getProperty("jdbi"), null, Arrays.asList(
					new Dependency(
							filter("org{}antlr"), "ST4", versions.getProperty("jdbi.st4"), null, Collections.emptyList()
					),
					new Dependency(
							filter("org{}antlr"), "antlr-runtime", versions.getProperty("jdbi.antlr"), null, Collections.emptyList()
					)
			)
			));
			dependencies.add(new Dependency(
					filter("org{}jdbi"), "jdbi3-sqlobject", versions.getProperty("jdbi"), null, Collections.emptyList()
			));
			dependencies.add(new Dependency(
					filter("org{}slf4j"), "slf4j-api", versions.getProperty("slf4j"), null, Collections.emptyList()
			));
			dependencies.add(new Dependency(
					filter("org{}slf4j"), "slf4j-simple", versions.getProperty("slf4j"), null, Collections.emptyList()
			));
			relocations.add(new RelocationRule(filter("com{}zaxxer{}hikari"), plugin.getPackageName() + ".libs.hikari"));
			relocations.add(new RelocationRule(filter("org{}jdbi"), plugin.getPackageName() + ".libs.jdbi"));
			relocations.add(new RelocationRule(filter("org{}slf4j"), plugin.getPackageName() + ".libs.slf4j"));
			relocations.add(new RelocationRule(filter("org{}antlr"), plugin.getPackageName() + ".libs.antlr"));
			relocations.add(new RelocationRule(filter("com{}github{}benmanes{}caffeine"), plugin.getPackageName() + ".libs.caffeine"));
			relocations.add(new RelocationRule(filter("io{}leangen{}geantyref"), plugin.getPackageName() + ".libs.geantyref"));
		}
		
		if (usages.contains(LibraryUsage.MARIADB)) {
			dependencies.add(new Dependency(
					filter("org{}mariadb{}jdbc"), "mariadb-java-client", versions.getProperty("mariadb"), null, Collections.emptyList()
			));
			relocations.add(new RelocationRule(filter("org{}mariadb"), plugin.getPackageName() + ".libs.mariadb"));
		}
		
		if (usages.contains(LibraryUsage.MYSQL)) {
			dependencies.add(new Dependency(
					filter("mysql"), "mysql-connector-java", versions.getProperty("mysql"), null, Collections.emptyList()
			));
			relocations.add(new RelocationRule(filter("com{}mysql"), plugin.getPackageName() + ".libs.mysql"));
		}
		
		if (usages.contains(LibraryUsage.POSTGRESQL)) {
			dependencies.add(new Dependency(
					filter("org{}postgresql"), "postgresql", versions.getProperty("postgresql"), null, Collections.emptyList()
			));
			relocations.add(new RelocationRule(filter("org{}postgresql"), plugin.getPackageName() + ".libs.postgresql"));
		}
		
		if (usages.contains(LibraryUsage.SQLITE)) {
			// Load SQLite if somehow its not in the server
			try {
				Class.forName("org.sqlite.SQLiteDataSource");
			} catch (Exception ignored) {
				// Load SQLite
				dependencies.add(new Dependency(
						filter("org{}xerial"), "sqlite-jdbc", versions.getProperty("sqlite"), null, Collections.emptyList()
				));
			}
		}
		
		if (usages.contains(LibraryUsage.SCRIPT) && plugin.getJavaVersion() >= 15) {
			// Load new Nashorn if Java 15+
			dependencies.add(new Dependency(
					filter("org{}openjdk{}nashorn"), "nashorn-core", versions.getProperty("nashorn"), null, Collections.emptyList()
			));
			dependencies.add(new Dependency(
					filter("org{}ow2{}asm"), "asm", versions.getProperty("asm"), null, Collections.emptyList()
			));
			dependencies.add(new Dependency(
					filter("org{}ow2{}asm"), "asm-util", versions.getProperty("asm"), null, Collections.emptyList()
			));
			relocations.add(new RelocationRule(filter("org{}openjdk{}nashorn"), plugin.getPackageName() + ".libs.nashorn"));
			relocations.add(new RelocationRule(filter("org{}objectweb{}asm"), plugin.getPackageName() + ".libs.asm"));
			relocations.add(new RelocationRule(filter("org{}ow2{}asm"), plugin.getPackageName() + ".libs.asm"));
		}
		
		return new DependencyData(
				Collections.emptySet(),
				Collections.singleton(new Repository(new URL(SimpleMirrorSelector.CENTRAL_URL))),
				dependencies,
				relocations
		);
	}
	
	private static String filter(String str) {
		return str.replace("{}", ".");
	}
}
