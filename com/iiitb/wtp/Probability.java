package com.iiitb.wtp;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author Puneeth Narayana
 *
 */
public class Probability {
	private double alpha = 0.15; //probability to teleport to another page

	/**
	 * Calculate the probability matrix for every link 
	 * @param wg
	 */
	public void updateDefaultProb(WebGraph wg) {
		int numNodes = wg.numNodes();
		Double weight, weight1,weight2;
		Map outlinks = new HashMap<>();
		
		weight = alpha / numNodes;
		
		for (int node = 0; node < numNodes; node++) {
			outlinks = wg.outLinks(node);
			weight1 = weight + (1-alpha)/outlinks.size();
			
			
		if (outlinks.isEmpty()) {
				weight2 = (double) (1.0/numNodes);
				for (int i = 0; i < numNodes; i++)
					wg.addLink(node, i, weight2);
			}
			
			else {
				
				for (int i = 0; i < numNodes; i++) {
					if(outlinks.containsKey(i)){
						
						wg.addLink(node, i, weight1);
					}
					else
						wg.addLink(node, i, weight);
				}
			}
			
		}
	}

}
