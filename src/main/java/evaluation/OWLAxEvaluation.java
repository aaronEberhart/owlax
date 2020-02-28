package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OWLAxEvaluation {
	
	private ArrayList<HashMap<String,Integer>> allResults;
	private HashMap<String,Integer> averageResult;
	
	public OWLAxEvaluation(ArrayList<HashMap<String,Integer>> resultsList) {
		allResults = resultsList;
		averageResult = (HashMap<String,Integer>)resultsList.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue)));
	}
	
	public ArrayList<HashMap<String,Integer>> getAllResults(){
		return allResults;
	}
	
	public HashMap<String,Integer> getAverageResult() {
		return averageResult;
	}
}
