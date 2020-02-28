package evaluation;

import java.util.*;
import java.util.stream.*;

public class OWLAxEvaluation {
	
	private ArrayList<HashMap<String,Integer>> allResults;
	private HashMap<String,Integer> averageResult;
	
	public OWLAxEvaluation(ArrayList<HashMap<String,Integer>> resultsList) {
		// there are 2 unique (key,value) pairs in each result that have the ontology name or the format as a key and a unique negative number as value
		// these are just so we can identify each result later if we need to so don't worry about them
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
