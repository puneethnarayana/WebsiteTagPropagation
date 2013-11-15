package com.iiitb.wtp;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * Ref : http://www.stackoverflow.com
 *
 */
public class DocumentVector {
	Map<String, Integer> wordMap = new HashMap<String, Integer>();
	
	//Get the frequency of the words in the document
	public void incCount(String word) {
		Integer oldCount = wordMap.get(word);
		wordMap.put(word, oldCount == null ? 1 : oldCount + 1);
	}

	//Get cosine similarity between two vectors
	double getCosineSimilarityWith(DocumentVector otherVector) {
		double innerProduct = 0;
		double cosSim = 0.0;
		for (String w : this.wordMap.keySet()) {
			innerProduct += this.getCount(w) * otherVector.getCount(w);
		}
		cosSim = innerProduct / (this.getNorm() * otherVector.getNorm());
		if (Double.isNaN(cosSim)) {
			return 0.0;
		} else
			return cosSim;

	}

	double getNorm() {
		double sum = 0;
		for (Integer count : wordMap.values()) {
			sum += count * count;
		}
		return Math.sqrt(sum);
	}

	int getCount(String word) {
		return wordMap.containsKey(word) ? wordMap.get(word) : 0;
	}

	void clear() {
		wordMap.clear();
	}

}