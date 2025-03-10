package com.alessiodp.core.common.storage.sql.connection;

import com.alessiodp.core.common.ADPPlugin;
import com.alessiodp.core.common.storage.StorageType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class SQLiteConnectionFactory implements ConnectionFactory {
	private final ADPPlugin plugin;
	@Setter private String databaseUrl;
	@Setter @Getter private String tablePrefix = "";
	@Getter private DataSource dataSource;
	@Getter private Jdbi jdbi;
	@Getter private boolean failed;
	
	public SQLiteConnectionFactory(ADPPlugin plugin, @NonNull String databaseUrl) {
		this.plugin = plugin;
		this.databaseUrl = databaseUrl;
	}
	
	public SQLiteConnectionFactory(ADPPlugin plugin, @NonNull Path databasePath) {
		this.plugin = plugin;
		this.databaseUrl = "jdbc:sqlite:" + databasePath.toString();
	}
	
	@Override
	public void init() {
		failed = true;
		try {
			Class<?> dataSourceClass = getClass().getClassLoader().loadClass("org.sqlite.SQLiteDataSource");
			dataSource = (DataSource) dataSourceClass.getConstructor().newInstance();
			dataSourceClass.getMethod("setUrl", String.class).invoke(dataSource, databaseUrl);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw new RuntimeException(e);
		}
		
		jdbi = Jdbi.create(dataSource);
		jdbi.installPlugin(new SqlObjectPlugin());
		jdbi.define("prefix", tablePrefix);
		
		failed = false;
	}
	
	@Override
	public void stop() {
		// Nothing to do
	}
	
	@Override
	public StorageType getType() {
		return StorageType.SQLITE;
	}
}
