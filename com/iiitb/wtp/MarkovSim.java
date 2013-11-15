package com.iiitb.wtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.bag.HashBag;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.iiitb.model.SeedHandler;
/**
 * 
 * @co-author Puneeth Narayana and Sindhu Priyadarshini
 *
 */
public class MarkovSim {

	public void runSimulation(WebGraph wg) throws SQLException,
			ClientProtocolException, IOException {
		SeedHandler db = new SeedHandler();
		Map<String, String> outputTags = new HashMap<>();
		Double combinedScore = 0.0;
		Double sum = 0.0;
		Double sim = 0.0;
		Map<Integer, Map<Integer, Double>> temp = new HashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> temp1 = new HashMap<>();
		String[] multitag = wg.getTags().split(",");
		for (String tag : multitag) {
			System.out.println("Extracting for tag " + tag);
			int mixingTime = 10;

			for (int i = 0; i < wg.numNodes(); i++) {
				for (int k = 0; k < wg.numNodes(); k++) {
					sim = wg.DocSim(k).get(tag);
					if (sim == null)
						sim = 0.0;
					combinedScore = sim * wg.outLink(i, k);
				
					sum = sum + combinedScore;
				}
				for (int j = 0; j < wg.numNodes(); j++) {
					sim = wg.DocSim(j).get(tag);
					if (sim == null)
						sim = 0.0;
					combinedScore = sim * wg.outLink(i, j);
					combinedScore = combinedScore / sum;

					temp1.put(j, combinedScore);
					temp.put(i, temp1);
				}

			}
			DocumentVector hitsvector1 = new DocumentVector();
			DocumentVector hitsvector2 = new DocumentVector();
			double stdDist;

			do {
				Map<Integer, Double> temp2 = new HashMap<>();
				ArrayList<Integer> random = new ArrayList<>();
				ArrayList<Integer> hits1 = new ArrayList<>();
				ArrayList<Integer> hits2 = new ArrayList<>();

				int currentNode1 = 0;
				int currentNode2 = 0;
				for (int l = 0; l < 10000; l++) {

					for (int i = 0; i < mixingTime; i++) {

						temp2 = temp.get(currentNode1);
						for (int j = 0; j < wg.numNodes(); j++) {
							int randWt = (int) (temp2.get(j) * 1000000);
							for (int k = 0; k < randWt; k++) {
								random.add(j);
							}
						}
						Random rand = new Random();
						// System.out.println(random.size());
						if (random.size() > 0) {
							int randomNumber = rand.nextInt(random.size());
							// System.out.println("rand " + randomNumber);
							currentNode1 = random.get(randomNumber);
							random.clear();
						}
					}
					hits1.add(currentNode1);

					for (int i = 0; i < mixingTime; i++) {

						temp2 = temp.get(currentNode2);
						for (int j = 0; j < wg.numNodes(); j++) {
							int randWt = (int) (temp2.get(j) * 1000000);
							for (int k = 0; k < randWt; k++) {
								random.add(j);
							}
						}
						Random rand = new Random();
						if (random.size() > 0) {
							int randomNumber = rand.nextInt(random.size());
							// System.out.println("rand2 " + randomNumber);
							currentNode2 = random.get(randomNumber);
							random.clear();
						}
					}
					hits2.add(currentNode2);
				}
				hitsvector1.clear();
				hitsvector2.clear();
				for (Integer urlID : hits1) {
					hitsvector1.incCount(urlID.toString());
				}
				for (Integer urlID : hits2) {
					hitsvector2.incCount(urlID.toString());
				}
				stdDist = hitsvector1.getCosineSimilarityWith(hitsvector2);
				System.out.println(stdDist + " standard dist");
				System.out.println(mixingTime + " mixing time");
				if (stdDist == 0)
					stdDist = 1;
				if (mixingTime >= 10000)
					mixingTime = mixingTime * 2;
				else {
					mixingTime = (int) Math.pow(mixingTime, 2);
				}
			} while (stdDist < 0.9);
			// System.out.println("out of do while");
			int count;
			String tags = "";
			for (Integer i = 0; i < wg.numNodes(); i++) {
				count = hitsvector1.getCount(i.toString());
				System.out.println(count + " "+ wg.IdentifyerToURL(i));
				if (count > 4 * (10000 / wg.numNodes())) {
					if (outputTags.containsKey(wg.IdentifyerToURL(i))) {
						tags = outputTags.get(wg.IdentifyerToURL(i)) + ", "
								+ tag;
						outputTags.put(wg.IdentifyerToURL(i), tags);
						db.insertHITSData(wg.IdentifyerToURL(i), tags);
					} else {
						outputTags.put(wg.IdentifyerToURL(i), tag);
						db.insertHITSData(wg.IdentifyerToURL(i), tag);
					}
				}
			}

		}
		System.out.println("Total Number of Nodes : " + wg.numNodes());
	}

	// db.serializeJavaObjectToFile(outputTags);

	// }

	public DocumentVector getdocVector(String content) {
		DocumentVector dv = new DocumentVector();

		try {

			Document doc = Jsoup.parse(content);
			String token = "";
			Elements ele = doc.getElementsByTag("p");
			Document doc1 = Jsoup.parse(ele.toString());

			String body = doc1.body().text();
			StringTokenizer strtok = new StringTokenizer(body);
			Stopwords sw = new Stopwords();
			while (strtok.hasMoreTokens()) {
				token = strtok.nextToken().replaceAll("[^\\p{Alpha}]+", "");

				if (!token.equals("") && !sw.is(token)) {
					token = token.toLowerCase();
					dv.incCount(token);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dv;
	}

	public String getContent(String url) throws ClientProtocolException,
			IOException {
		String line;
		String sourceLine = "";
		try {
			System.out.println("get content of url " + url);
			HttpHost proxy = new HttpHost("192.168.3.254", 8080, "http");
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
			HttpGet get = new HttpGet(url);

			HttpResponse response = client.execute(get);
			InputStreamReader is = new InputStreamReader(response.getEntity()
					.getContent(), "UTF8");
			BufferedReader rd = new BufferedReader(is);

			while ((line = rd.readLine()) != null) {
				sourceLine = sourceLine + line;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";

		}
		return sourceLine;

	}

	public Double getDocSimilarity(DocumentVector webgraphNode,
			DocumentVector tagvector) throws SQLException {

		// System.out.println("Similarity = " + v1.getCosineSimilarityWith(v2));
		return tagvector.getCosineSimilarityWith(webgraphNode);
	}

}
