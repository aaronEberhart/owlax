package evaluation;

import java.util.*;
import java.util.stream.*;

public class OWLAxEvaluation {
	
	private ArrayList<ArrayList<HashMap<String,Integer>>> allResults;
	private ArrayList<HashMap<String,Integer>> owlaxResults;
	private ArrayList<HashMap<String,Integer>> ontologyCompositions;
	private HashMap<String,Double> averageResult;
	private HashMap<String,Double> averageOntology;
	double totalCoverage;
	
	public OWLAxEvaluation(ArrayList<ArrayList<HashMap<String,Integer>>> resultsList) {
		
		// results are lists of 2 things: ontology stats and OWLAx evaluations
		allResults = resultsList;
				
		//split the results list into ontology and owlax data
		owlaxResults = new ArrayList<HashMap<String,Integer>>();		
		ontologyCompositions = new ArrayList<HashMap<String,Integer>>();
		allResults.forEach(a -> {owlaxResults.add(a.get(0));ontologyCompositions.add(a.get(1));});
		
		// calculating the averages
		averageResult = (HashMap<String,Double>)owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingInt(Map.Entry::getValue)));		
		averageOntology = (HashMap<String,Double>)ontologyCompositions.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingInt(Map.Entry::getValue)));
		//removes the labels from the average ontology
	    averageOntology.values().removeIf(a -> a < 0);
	    
	    // average coverage for average result ignoring other
	    totalCoverage = averageResult.values().stream().reduce(0.0,(a,b) -> (averageResult.get("other") == a || averageResult.get("other") == b) ? 0 : a+b) / allResults.size();
	}
	
	public ArrayList<HashMap<String,Integer>> getOWLAxResults(){
		return owlaxResults;
	}
	
	public ArrayList<HashMap<String,Integer>> getOntologyCompositions(){
		return ontologyCompositions;
	}
	
	public ArrayList<ArrayList<HashMap<String,Integer>>> getAllResults(){
		return allResults;
	}
	
	public HashMap<String,Double> getAverageOWLAxResult() {
		return averageResult;
	}
	
	public HashMap<String,Double> getAverageOntology() {
		return averageOntology;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Evaluation Results:\n\n"));
		//TODO
		return sb.toString();
	}
}
