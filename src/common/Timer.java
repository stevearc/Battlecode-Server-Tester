package common;

public class Timer {
	private long millis;
	
	public Timer(long millis) {
		this.millis = millis;
	}
	
	@Override
	public String toString() {
		long secs = millis/1000;
		long s = secs % 60;
		long m = secs/60;
		return m + ":" + s;
	}

}
