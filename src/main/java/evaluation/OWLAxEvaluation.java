package evaluation;

import java.util.*;
import java.util.stream.*;

public class OWLAxEvaluation {
	
	private ArrayList<ArrayList<HashMap<String,Integer>>> allResults;
	private ArrayList<HashMap<String,Integer>> owlaxResults;
	private ArrayList<HashMap<String,Integer>> ontologyCompositions;
	private HashMap<String,Double> averageResult;
	private HashMap<String,Double> averageOntology;
	
	public OWLAxEvaluation(ArrayList<ArrayList<HashMap<String,Integer>>> resultsList) {
		// results are lists of 2 things: ontology stats and OWLAx evaluations
		//
		// there are 2 unique (key,value) pairs in each ontology that have the name or the format as a key and a unique negative number as value
		// these are just so we can identify each result later if we need to, so don't worry about them
		allResults = resultsList;
		owlaxResults = new ArrayList<HashMap<String,Integer>>(){{allResults.forEach(a -> add(a.get(0)));}private static final long serialVersionUID = 5L;};
		ontologyCompositions = new ArrayList<HashMap<String,Integer>>(){{allResults.forEach(a -> add(a.get(1)));}private static final long serialVersionUID = 4L;};
		// lambda calculating the averages
		averageResult = (HashMap<String,Double>)owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingInt(Map.Entry::getValue)));		
		averageOntology = (HashMap<String,Double>)ontologyCompositions.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingInt(Map.Entry::getValue)));
		//removes the labels
	    averageOntology.values().removeIf(a -> a < 0);
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
