package com.iiitb.wtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * 
 * @author Sindhu Priyadarshini
 * 
 */
public class URLConnect {

	String tag = "";
	MarkovSim ms = new MarkovSim();
	Parser ObjParse = new Parser();

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * Start the webcrawling and extract the weblinks
	 * @param urlLinks
	 * 			The links in each hop
	 * @param tagDV
	 * 			Document Vector for the tags in the seed list
	 */
	public void connect(ArrayList<String> urlLinks,
			Map<String, DocumentVector> tagDV) {

		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < Global.ITERATIONSTEPS; i++) {
			list.clear();
			for (String url : urlLinks) {

				try {
					HttpHost proxy = new HttpHost("192.16.3.254", 8080, "http");
					HttpClient client = new DefaultHttpClient();
					client.getParams().setParameter(
							ConnRoutePNames.DEFAULT_PROXY, proxy);
					HttpGet get = new HttpGet(url);

					HttpResponse response = client.execute(get);
					InputStreamReader is = new InputStreamReader(response
							.getEntity().getContent(), "UTF8");
					BufferedReader rd = new BufferedReader(is);

					String line;
					String sourceLine = "";

					while ((line = rd.readLine()) != null) {
						sourceLine = sourceLine + line;
					}

					list.addAll(ObjParse.parse(sourceLine, url, tagDV));

					rd.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			urlLinks.clear();
			urlLinks.addAll(list);

		}
		//Create the document vector for the remaining links
		try {
			String content;

			for (String string : list) {
				content = ms.getContent(string);
				ObjParse.addvector(string, content, tagDV);
			}
			//serialise the webgraph object
			ObjParse.serialiseWG();
			//Calculate the probability matrix
			ObjParse.calcProb();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
}