package com.iiitb.wtp;


import java.io.*;
import java.util.*;

/**
 * This class implements a memory Data Structure for storing graphs.</p><p>
 * 
 * A large amount of research has recently focused on the graph structure (or link structure)
 * of the World Wide Web, which has proven to be extremely useful for improving the
 * performance of search engines and other tools for navigating the web.
 * For example, the Pagerank algorithm of Brin and Page, used in the Google search
 * engine, and the HITS algorithm of Kleinberg both rank pages according to the 
 * number and importance of other pages that link to them.</p><p>
 * 
 * This class provides the methods needed to efficiently compute with graphs and to
 * experiment with such algorithms, using main memory for storage.
 *
 * @author Bruno Martins
 * modified by Sindhu Priyadarshini
 */
public class WebGraph implements Serializable {

	/**
	 * default serial id for serialising or deserialising the object
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A Map storing relationships from numeric identifiers to URLs, usefull for
	 * storing Web graphs
	 */
	private Map IdentifyerToURL;

	/**
	 * A Map storing relationships from URLs to numeric identifiers, usefull for
	 * storing Web graphs
	 */
	private Map URLToIdentifyer;

	
	/**
	 * A Map storing OutLinks. For each identifyer (the key), another Map is
	 * stored, containing for each inlink an associated "connection weight"
	 */
	private Map OutLinks;
	
	
	/**
	 * Document similarity of the url and the tag
	 */
	private Map<Integer, Map<String, Double>> DocSim = new HashMap<Integer, Map<String, Double>>();

	/** The number of nodes in the graph */
	private int nodeCount;

	//Seed data that requires to be stored in the file for which the crawling was run
	private String seed_url;
	private String tags;

	public String getSeed_url() {
		return seed_url;
	}

	public void setSeed_url(String seed_url) {
		this.seed_url = seed_url;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	/**
	 * Constructor for WebGraph
	 * 
	 */
	public WebGraph() {
		IdentifyerToURL = new HashMap();
		URLToIdentifyer = new HashMap();
		// InLinks = new HashMap();
		OutLinks = new HashMap();
		nodeCount = 0;
	}

	/**
	 * Returns the identifyer associated with a given URL
	 * 
	 * @param URL
	 *            The URL
	 * @return The identifyer associated with the given URL
	 */
	public Integer URLToIdentifyer(String URL) {
		String host;
		String name;
		int index = 0, index2 = 0;
		if (URL.startsWith("http://"))
			index = 7;
		else if (URL.startsWith("ftp://"))
			index = 6;
		index2 = URL.substring(index).indexOf("/");
		if (index2 != -1) {
			name = URL.substring(index + index2 + 1);
			host = URL.substring(0, index + index2);
		} else {
			host = URL;
			name = "";
		}
		Map map = (Map) (URLToIdentifyer.get(URL));
		if (map == null)
			return null;
		return (Integer) (map.get(name));
	}

	/**
	 * Returns the URL associated with a given identifyer
	 * 
	 * @param id
	 *            The identifyer
	 * @return The URL associated with the given identifyer
	 */
	public String IdentifyerToURL(Integer id) {
		return (String) (IdentifyerToURL.get(id));
	}

	/**
	 * Adds a node to the graph
	 * 
	 * @param link
	 *            The URL associated with the added node
	 */
	public void addLink(String link) {
		Integer id = URLToIdentifyer(link);
		if (id == null) {
			id = new Integer(nodeCount++);
			String host;
			String name;
			int index = 0, index2 = 0;
			if (link.startsWith("http://"))
				index = 7;
			else if (link.startsWith("ftp://"))
				index = 6;
			index2 = link.substring(index).indexOf("/");
			if (index2 != -1) {
				name = link.substring(index + index2 + 1);
				host = link.substring(0, index + index2);
			} else {
				host = link;
				name = "";
			}
			Map map = (Map) (URLToIdentifyer.get(host));
			if (map == null)
				map = new HashMap();
			map.put(name, id);
			URLToIdentifyer.put(link, map);
			IdentifyerToURL.put(id, link);
			OutLinks.put(id, new HashMap());
		}
	}

	/**
	 * Adds an association between two given nodes in the graph. If the
	 * corresponding nodes do not exists, this method creates them. If the
	 * connection already exists, the strength value is updated.
	 * 
	 * @param fromLink
	 *            The URL for the source node in the graph
	 * @param fromLink
	 *            The URL for the target node in the graph
	 * @param fromLink
	 *            The strength to associate with the connection
	 * @return The strength associated with the connection
	 */
	public Double addLink(String fromLink, String toLink, Double weight) {
		addLink(fromLink);
		addLink(toLink);
		Integer id1 = URLToIdentifyer(fromLink);
		Integer id2 = URLToIdentifyer(toLink);
		return addLink(id1, id2, weight);
	}

	/**
	 * Adds an association between two given nodes in the graph. If the
	 * corresponding nodes do not exists, this method creates them. If the
	 * connection already exists, the strength value is updated.
	 * 
	 * @param fromLink
	 *            The identifyer for the source node in the graph
	 * @param fromLink
	 *            The identifyer for the target node in the graph
	 * @param fromLink
	 *            The strength to associate with the connection
	 * @return The strength associated with the connection
	 */
	public Double addLink(Integer fromLink, Integer toLink, Double weight) {
		Double aux;
		Map map2 = (Map) (OutLinks.get(fromLink));// B
		aux = (Double) (map2.get(toLink));
		if (aux == null)
			map2.put(toLink, weight);
		else if (aux.doubleValue() > weight.doubleValue())
			map2.put(toLink, weight);

		OutLinks.put(fromLink, map2);
		return weight;

	}

	/**
	 * Returns a map of the tag and document similarity for the given url
	 * @param link
	 * @return
	 */
	public Map<String, Double> DocSim(Integer link) {
		if (link == null)
			return new HashMap();
		Map aux = (Map) (DocSim.get(link));
		return (aux == null) ? new HashMap() : aux;
	}

	/**
	 * Returns a Map of the nodes that are connected from a given node in the
	 * graph. Each mapping contains the identifyer for a node and the associated
	 * connection strength.
	 * 
	 * @param URL
	 *            The URL for the node in the graph
	 * @return A Map of the nodes that are connected from the given node in the
	 *         graph.
	 */
	public Map outLinks(String URL) {
		Integer id = URLToIdentifyer(URL);
		return outLinks(id);
	}

	/**
	 * Returns a Map of the nodes that are connected from a given node in the
	 * graph. Each mapping contains the identifyer for a node and the associated
	 * connection strength.
	 * 
	 * @param link
	 *            The URL for the node in the graph
	 * @return A Map of the nodes that are connected from the given node in the
	 *         graph.
	 */
	public Map outLinks(Integer link) {
		if (link == null)
			return new HashMap();
		Map aux = (Map) (OutLinks.get(link));
		return (aux == null) ? new HashMap() : aux;
	}

	/**
	 * Returns the connection strength between two nodes, assuming there is a
	 * connection from the first to the second. If no connection exists, a link
	 * strength of zero is returned.
	 * 
	 * @param fromLink
	 *            The source link
	 * @param toLink
	 *            The target link
	 * @return The strenght for the connection between fromLink and toLink (
	 *         fromLink -> toLink )
	 * @see outLink
	 */
	public Double outLink(String fromLink, String toLink) {
		Integer id1 = URLToIdentifyer(fromLink);
		Integer id2 = URLToIdentifyer(toLink);
		return outLink(id1, id2);
	}

	/**
	 * Returns the connection strength between two nodes, assuming there is a
	 * connection from the first to the second. If no connection exists, a link
	 * strength of zero is returned.
	 * 
	 * @param fromLink
	 *            An identifyer for the source link
	 * @param toLink
	 *            An identifyer for the target link
	 * @return The strenght for the connection between fromLink and toLink (
	 *         fromLink -> toLink )
	 * @see inLink
	 */
	public Double outLink(Integer fromLink, Integer toLink) {
		Map aux = outLinks(fromLink);
		if (aux == null)
			return new Double(0);
		Double weight = (Double) (aux.get(toLink));
		return (weight == null) ? new Double(0) : weight;
	}

	/**
	 * Transforms a bi-directional graph to an uni-directional equivalent. The
	 * connection strenght between two nodes A and B that are inter-connected in
	 * the bi-directional graph is transformed into
	 * MAX(weight_inlink(A,B),weight_outlink(A,B))
	 */
	public void transformUnidirectional() {
		Iterator it = OutLinks.keySet().iterator();
		while (it.hasNext()) {
			Integer link1 = (Integer) (it.next());
			Map auxMap = (Map) (OutLinks.get(link1));
			Iterator it2 = auxMap.keySet().iterator();
			while (it2.hasNext()) {
				Integer link2 = (Integer) (it.next());
				Double weight = (Double) (auxMap.get(link2));
				addLink(link2, link1, weight);
			}
		}
	}

	/**
	 * Returns the number of nodes in the graph
	 * 
	 * @return The number of nodes in the graph
	 */
	public int numNodes() {
		return nodeCount;
	}

	/**
	 * Store the document similarity score for the corresponding url and tag
	 * @param link
	 * @param tag
	 * @param docSimScore
	 */
	public void addVector(String link, String tag, Double docSimScore) {
		Map<String, Double> tagSim = new HashMap();
		addLink(link);
		Integer id = URLToIdentifyer(link);
		tagSim = DocSim(id);
		tagSim.put(tag, docSimScore);
		DocSim.put(id, tagSim);
	}

}
