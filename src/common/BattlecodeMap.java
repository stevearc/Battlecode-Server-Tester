package common;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Representation of map information to be sent over network between Worker and Server
 * @author stevearc
 *
 */
public class BattlecodeMap implements Serializable {
	private static final long serialVersionUID = -3033262234181516847L;
	public String map;
	public int height;
	public int width;
	public int rounds;
	public int points;

	/**
	 * Reads file and parses out necessary information
	 * @param map
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public BattlecodeMap(File map) throws ParserConfigurationException, SAXException, IOException {
		this.map = map.getName().substring(0, map.getName().length() - 4);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(map);
		doc.getDocumentElement().normalize();
		NodeList nodeLst = doc.getElementsByTagName("map");

		Node n = nodeLst.item(0);
		NamedNodeMap nl = n.getAttributes();
		width = Integer.parseInt(nl.getNamedItem("width").getNodeValue());
		height = Integer.parseInt(nl.getNamedItem("height").getNodeValue());

		nodeLst = doc.getElementsByTagName("game");
		n = nodeLst.item(0);
		nl = n.getAttributes();
		rounds = Integer.parseInt(nl.getNamedItem("rounds").getNodeValue());
		points = Integer.parseInt(nl.getNamedItem("points").getNodeValue());
	}
	
	/**
	 * Manually specify necessary information
	 * @param map
	 * @param height
	 * @param width
	 * @param rounds
	 * @param points
	 */
	public BattlecodeMap(String map, int height, int width, int rounds, int points) {
		this.map = map;
		this.height = height;
		this.width = width;
		this.rounds = rounds;
		this.points = points;
	}
	
	/**
	 * 
	 * @return The area of the map
	 */
	public int getSize() {
		return width * height;
	}
	
	/**
	 * 
	 * @return The size classifier for the map (small, medium, large)
	 */
	public String getSizeClass() {
		int area = getSize();
		if (area < Config.getConfig().map_cutoff_small)
			return "small";
		else if (area < Config.getConfig().map_cutoff_medium)
			return "medium";
		else
			return "large";
	}
	
	@Override
	public int hashCode() {
		return map.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BattlecodeMap) {
			BattlecodeMap bm = (BattlecodeMap) o;
			return bm.map.equals(map);
		}
		return false;
	}

	@Override
	public String toString() {
		return map;
	}
}
