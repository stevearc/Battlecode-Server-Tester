package model;


public class BSUserPrefs {
	private int[][] analysisViewLines = {
			{1,0,0,0}, 
			{1,0,0,0}};

	public int[][] getAnalysisViewLines() {
		return analysisViewLines;
	}

	public void setAnalysisViewLines(int[][] analysisViewLines) {
		this.analysisViewLines = analysisViewLines;
	}
	
	public String toJavascript() {
		StringBuilder sb = new StringBuilder();
		sb.append("<script type='text/javascript'>");
		sb.append("var viewLines = [");
		for (int i = 0; i < analysisViewLines.length; i++) {
			sb.append("[");
			for (int j = 0; j < analysisViewLines[i].length; j++) {
				sb.append(analysisViewLines[i][j]).append(",");
			}
			sb.append("],");
		}
		sb.append("];\n");
		sb.append("</script>");
		return sb.toString();
	}

}
