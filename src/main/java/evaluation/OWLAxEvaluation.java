package evaluation;

<<<<<<< HEAD
import java.util.*;
import java.util.stream.*;

public class OWLAxEvaluation {
	
	// if you want to see the keys that will be used in the dictionary you can do this
	// possibly more will come later but these are the starting ones
	// 
	// String[] example = OWLAxMatcher.getAxiomHashKeys();  
	
=======
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OWLAxEvaluation {
	
>>>>>>> bc4caa800a1f3ebabdb8732c2de34be375bd02b8
	private ArrayList<HashMap<String,Integer>> allResults;
	private HashMap<String,Integer> averageResult;
	
	public OWLAxEvaluation(ArrayList<HashMap<String,Integer>> resultsList) {
		allResults = resultsList;
<<<<<<< HEAD
		// lambda calculating the averages
		averageResult = (HashMap<String,Integer>)resultsList.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue)));
		//removes the labels
		averageResult.values().removeIf(a -> a < 0);
=======
		averageResult = (HashMap<String,Integer>)resultsList.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue)));
>>>>>>> bc4caa800a1f3ebabdb8732c2de34be375bd02b8
	}
	
	public ArrayList<HashMap<String,Integer>> getAllResults(){
		return allResults;
	}
	
	public HashMap<String,Integer> getAverageResult() {
		return averageResult;
	}
}
