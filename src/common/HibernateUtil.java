package common;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import model.BSMap;
import model.BSMatch;
import model.BSPlayer;
import model.BSRun;
import model.BSUser;
import model.MatchResult;
import model.TeamMatchResult;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;


public class HibernateUtil {
	private static final String[][] properties = {
		{"hibernate.id.new_generator_mappings", "true"},
		{"hibernate.dialect", "org.hibernate.dialect.HSQLDialect"},
		{"hibernate.connection.driver_class", "org.hsqldb.jdbcDriver"},
		{"hibernate.connection.username", "sa"},
		{"hibernate.connection.password", ""},
		{"hibernate.connection.url", "jdbc:hsqldb:file:./BSTesterDB"},
		{"hibernate.hbm2ddl.auto", "validate"},
		{"hibernate.search.autoregister_listeners", "false"},
	};
	private static final Configuration config = buildConfiguration();
	private static final SessionFactory sessionFactory = buildSessionFactory();
	private static final EntityManagerFactory entityManagerFactory = buildEntityManagerFactory();

	private static Configuration buildConfiguration() {
		try {
			Configuration cfg = new Configuration();
			cfg.addAnnotatedClass(BSRun.class);
			cfg.addAnnotatedClass(BSMap.class);
			cfg.addAnnotatedClass(BSUser.class);
			cfg.addAnnotatedClass(BSMatch.class);
			cfg.addAnnotatedClass(BSPlayer.class);
			cfg.addAnnotatedClass(MatchResult.class);
			for (String[] keyVal: properties) {
				cfg.setProperty(keyVal[0], keyVal[1]);
			}
			if (Config.DEBUG) {
				cfg.setProperty("hibernate.show_sql", Config.SHOW_SQL ? "true" : "false");
			}
			return cfg;
		}
		catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("SEVERE: Initial Hibernate configuration creation failed." + ex);
			ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static SessionFactory buildSessionFactory() {
		try {
			SessionFactory sf;
			try {
				sf = config.buildSessionFactory();
				return sf;
			} catch (HibernateException e) {
				SchemaExport schema = new SchemaExport(config);
				schema.create(true, true);
			}
			sf = config.buildSessionFactory();
			return sf;
		} catch (Throwable ex) {
			System.err.println("SEVERE: SessionFactory creation failed." + ex);
			ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static EntityManagerFactory buildEntityManagerFactory() {
		Ejb3Configuration cfg = new Ejb3Configuration();
		cfg.addAnnotatedClass(BSRun.class);
		cfg.addAnnotatedClass(BSMap.class);
		cfg.addAnnotatedClass(BSUser.class);
		cfg.addAnnotatedClass(BSMatch.class);
		cfg.addAnnotatedClass(BSPlayer.class);
		cfg.addAnnotatedClass(MatchResult.class);
		cfg.addAnnotatedClass(TeamMatchResult.class);
		for (String[] keyVal: properties) {
			cfg.setProperty(keyVal[0], keyVal[1]);
		}
		if (Config.DEBUG) {
			cfg.setProperty("hibernate.show_sql", Config.SHOW_SQL ? "true" : "false");
		}
		return cfg.buildEntityManagerFactory();
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}

}
