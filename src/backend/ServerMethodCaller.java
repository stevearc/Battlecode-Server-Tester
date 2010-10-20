package backend;

import networking.Packet;

import common.Config;
import common.MatchSet;

public class ServerMethodCaller {

	public static void queueRun(final MatchSet run) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().queueRun(run);
			}

		}).start();
	}

	public static void deleteRun(final int run_id) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().deleteRun(run_id);
			}

		}).start();
	}

	public static void pokeServer() {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().poke();
			}

		}).start();
	}

	public static void matchFinished(final ClientRepr client, final Packet p) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().matchFinished(client, p);
			}

		}).start();
	}

	public static void sendClientMatches(final ClientRepr client) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().sendClientMatches(client);
			}

		}).start();
	}

}
