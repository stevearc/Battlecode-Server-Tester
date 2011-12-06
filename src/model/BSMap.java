package model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import common.Config;

/**
 * Representation of map information to be sent over network between Worker and Server
 * @author stevearc
 *
 */
@Entity
public class BSMap implements Serializable {
	private static final long serialVersionUID = -3033262234181516847L;
	private Long id;
	private String mapName;
	private Long height;
	private Long width;
	private Long rounds;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}

	@Column(nullable=false,updatable=false,unique=true)
	public String getMapName() {
		return mapName;
	}

	@Column(nullable=false)
	public Long getHeight() {
		return height;
	}

	@Column(nullable=false)
	public Long getWidth() {
		return width;
	}

	@Column(nullable=false)
	public Long getRounds() {
		return rounds;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public void setHeight(Long height) {
		this.height = height;
	}

	public void setWidth(Long width) {
		this.width = width;
	}

	public void setRounds(Long rounds) {
		this.rounds = rounds;
	}

	public BSMap() {
		
	}
	
	/**
	 * Reads file and parses out necessary information
	 * @param map
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public BSMap(File map) throws ParserConfigurationException, SAXException, IOException {
		this.mapName = map.getName().substring(0, map.getName().length() - 4);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(map);
		doc.getDocumentElement().normalize();
		NodeList nodeLst = doc.getElementsByTagName("map");

		Node n = nodeLst.item(0);
		NamedNodeMap nl = n.getAttributes();
		width = new Long(Integer.parseInt(nl.getNamedItem("width").getNodeValue()));
		height = new Long(Integer.parseInt(nl.getNamedItem("height").getNodeValue()));

		nodeLst = doc.getElementsByTagName("game");
		n = nodeLst.item(0);
		nl = n.getAttributes();
		rounds = new Long(Integer.parseInt(nl.getNamedItem("rounds").getNodeValue()));
	}
	
	/**
	 * Manually specify necessary information
	 * @param map
	 * @param height
	 * @param width
	 * @param rounds
	 * @param points
	 */
	public BSMap(String map, Long height, Long width, Long rounds) {
		this.mapName = map;
		this.height = height;
		this.width = width;
		this.rounds = rounds;
	}
	
	/**
	 * 
	 * @return The area of the map
	 */
	public Long calculateSize() {
		return width * height;
	}
	
	/**
	 * 
	 * @return The size classifier for the map (small, medium, large)
	 */
	public String calculateSizeClass() {
		Long area = calculateSize();
		if (area < Config.getConfig().map_cutoff_small)
			return "small";
		else if (area < Config.getConfig().map_cutoff_medium)
			return "medium";
		else
			return "large";
	}
	
	@Override
	public int hashCode() {
		return mapName.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BSMap) {
			BSMap bm = (BSMap) o;
			return bm.mapName.equals(mapName);
		}
		return false;
	}

	@Override
	public String toString() {
		return mapName;
	}
}
