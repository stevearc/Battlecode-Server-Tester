package model;

public enum BSRobotType {
	ARCHON,
	SOLDIER,
	SCOUT,
	DISRUPTER,
	SCORCHER,
	TOWER;

	public String toString() {
		switch(this) {
		case ARCHON:
			return "Archon";
		case DISRUPTER:
			return "Disrupter";
		case SCORCHER:
			return "Scorcher";
		case SCOUT:
			return "Scout";
		case SOLDIER:
			return "Soldier";
		case TOWER:
			return "Tower";
		default:
			return "Unknown";
		}
	}
}