package evaluation;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;

import org.semanticweb.owlapi.model.AxiomType;

public class OWLAxEvaluation {
	
	private ArrayList<ArrayList<HashMap<String,Integer>>> allResults;
	private ArrayList<HashMap<String,Integer>> owlaxResults;
	private ArrayList<HashMap<String,Integer>> ontologyCompositions;
	private HashMap<String,Double> averageResult;
	private HashMap<String,Double> averageOntology;
	double totalCoverage;
	float totalAxioms;
	float totalclasses;
	float average_total;
	
	public OWLAxEvaluation(ArrayList<ArrayList<HashMap<String,Integer>>> resultsList) {
		
		// results are lists of 2 things: ontology stats and OWLAx evaluations
		allResults = resultsList;
		//System.out.println(allResults);
				
		//split the results list into ontology and owlax data
		owlaxResults = new ArrayList<HashMap<String,Integer>>();
		
		ontologyCompositions = new ArrayList<HashMap<String,Integer>>();
		allResults.forEach(a -> {owlaxResults.add(a.get(0));ontologyCompositions.add(a.get(1));});
		
		HashMap<String, Integer> val = owlaxResults.get(0);
		//total axioms in oxlax
		 totalAxioms = 0.0f;
		for (float f : val.values()) {
			totalAxioms += f;
		}
	    
		ArrayList<HashMap<String, Integer>> ontology_result = ontologyCompositions ;
		HashMap<String, Integer> val_ont = ontology_result.get(0);
		// total classes in ontology
		 totalclasses = 0.0f;
		for (float f : val_ont.values()) {
			totalclasses += f;
		}

		//average 
	   average_total = totalAxioms / totalclasses;
		
//		System.out.println(totalAxioms);
//		System.out.println( totalclasses);
//		System.out.println(average_total);
//		
		// calculating the averages
		averageResult = (HashMap<String,Double>)owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingInt(Map.Entry::getValue)));		
		averageOntology = (HashMap<String,Double>)ontologyCompositions.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingInt(Map.Entry::getValue)));
		//removes the labels from the average ontology
	    averageOntology.values().removeIf(a -> a < 0);
	    
	    //System.out.println(averageResult);
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
		sb.append(String.format("Total Owlax matched axioms: " +totalAxioms ));
		sb.append(String.format("\n\nTotal classes :- " + totalclasses));
		sb.append(String.format("\n\nAverage :- " + average_total));
		return sb.toString();
	}

	
}
