package evaluation;

import java.util.*;
import java.util.stream.*;

public class OWLAxEvaluation {
	
	// if you want to see the keys that will be used in the dictionary you can do this
	// possibly more will come later but these are the starting ones
	// 
	// String[] example = OWLAxMatcher.getAxiomHashKeys();  
	
	private ArrayList<HashMap<String,Integer>> allResults;
	private HashMap<String,Integer> averageResult;
	
	public OWLAxEvaluation(ArrayList<HashMap<String,Integer>> resultsList) {
		allResults = resultsList;
		// lambda calculating the averages
		averageResult = (HashMap<String,Integer>)resultsList.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue)));
		//removes the labels
		averageResult.values().removeIf(a -> a < 0);
	}
	
	public ArrayList<HashMap<String,Integer>> getAllResults(){
		return allResults;
	}
	
	public HashMap<String,Integer> getAverageResult() {
		return averageResult;
	}
}
