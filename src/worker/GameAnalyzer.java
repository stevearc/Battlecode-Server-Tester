package worker;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import model.MatchResultImpl;
import model.ScrimmageMatchResult;

import org.apache.log4j.Logger;

import battlecode.serial.MatchHeader;
import battlecode.server.proxy.XStreamProxy;

public class GameAnalyzer {
	private static final Logger _log = Logger.getLogger(GameAnalyzer.class);
	private ObjectInputStream input;
	private List<MatchResultImpl> results;
	private List<String> observationList;
	private String teamA;
	private String teamB;
	private String[] maps;
	private StringBuilder observations = new StringBuilder();

	public GameAnalyzer(byte[] data) throws IOException, ClassNotFoundException {
		input = XStreamProxy.getXStream().createObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
	}

	public GameAnalyzer(String filename) throws IOException, ClassNotFoundException {
		input = XStreamProxy.getXStream().createObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));
	}

	public List<ScrimmageMatchResult> analyzeScrimmageMatches() throws IOException, ClassNotFoundException {
		List<ScrimmageMatchResult> scrimResults = new ArrayList<ScrimmageMatchResult>();
		List<MatchResultImpl> r = analyzeMatches();
		for (int i = 0; i < r.size(); i++) {
			MatchResultImpl m = r.get(i);
			ScrimmageMatchResult smr = new ScrimmageMatchResult(m);
			smr.setObservations(observationList.get(i));
			scrimResults.add(smr);
		}
		if (maps == null) {
			throw new IOException("Scrimmage match has no map data");
		}
		if (maps.length < scrimResults.size()) {
			throw new IOException("Scrimmage match has incorrect number of maps");
		}
		for (int i = 0; i < scrimResults.size(); i++) {
			scrimResults.get(i).setMap(maps[i]);
		}

		return scrimResults;
	}

	public String getTeamA() {
		return teamA;
	}

	public String getTeamB() {
		return teamB;
	}

	public List<MatchResultImpl> analyzeMatches() throws IOException, ClassNotFoundException {
		if (results != null) {
			return results;
		}
		results = new ArrayList<MatchResultImpl>();
		// Initialize first Game
		Object o = input.readObject();
		if (o == null || !(o instanceof MatchHeader)) {
			_log.error("Match in bad format");
			return results;
		}
		List<GameData> gameDatas = new ArrayList<GameData>();
		GameData gameData = new GameData();
		gameData.addData(o);
		gameDatas.add(gameData);

		try {
			while ((o = input.readObject()) != null) {
				if (o instanceof MatchHeader) {
					// New Game
					results.add(gameData.analyzeMatch());
					gameData = new GameData();
					gameDatas.add(gameData);
				}

				gameData.addData(o);
			}
		} catch (EOFException e) {
			// Aaaaaand we're done
		}
		results.add(gameData.analyzeMatch());
		// Pull the metadata out of the final match
		teamA = gameData.getTeamA();
		teamB = gameData.getTeamB();
		maps = gameData.getMaps();
		String header = "-------------------- Match Starting --------------------";
		StringBuilder sb;
		observationList = new ArrayList<String>();
		for (int i = 0; i < gameDatas.size(); i++) {
			sb = new StringBuilder();
			sb.append(header);
			sb.append("\n");
			String center = teamA + " vs. " + teamB + " on " + maps[i];
			for (int j = 0; j < (header.length() - center.length())/2; j++) {
				sb.append(" ");
			}
			sb.append(center);
			sb.append("\n");
			sb.append(gameDatas.get(i).getObservations());
			observations.append(sb.toString());
			observationList.add(sb.toString());
		}

		return results;
	}
	
	public String getObservations() {
		return observations.toString();
	}
	
	public void close() {
		try {
			input.close();
		} catch (IOException e) {
			_log.error("Error closing input stream", e);
		}
	}
}
