package main;

import java.util.HashSet;

import common.Config;

import server.Server;
import client.Client;

/**
 * Where everything starts
 * @author steven
 *
 */

public class Main {
	private static final String VERSION = "0.2";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HashSet<String> argSet = new HashSet<String>();
		for (String s: args)
			argSet.add(s);
		
		if (argSet.contains("-h") || argSet.contains("--help")) {
			System.out.println("Run with no arguments for the client, or with -s for the server");
			System.exit(0);
		}
		if (argSet.contains("-v")) {
			System.out.println("Verion: " + VERSION);
			System.exit(0);
		}
		try {
			if (argSet.contains("-s")) {
				new Server(new Config(true)).run();
			}
			else
				new Client(new Config(false)).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
