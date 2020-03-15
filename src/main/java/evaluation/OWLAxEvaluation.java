package evaluation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;



public class OWLAxEvaluation {
	
	private ArrayList<ArrayList<HashMap<String,Integer>>> allResults;
	private ArrayList<HashMap<String,Integer>> owlaxResults;
	private ArrayList<HashMap<String,Integer>> ontologyCompositions;
	private ArrayList<String> unusedOWLAx;
	private ArrayList<String> unusedOntologyAxioms;
	private HashMap<String,Double> meanResult;
	private HashMap<String,Double> meanOntology;
	private HashMap<String,Double> medianResult;
	private HashMap<String,Double> medianOntology;
	private HashMap<String,Integer> modeResult;
	private HashMap<String,Integer> modeOntology;
	private HashMap<String,Double> stdDevResult;
	private HashMap<String,Double> stdDevOntology;
	double totalCoverage;
	double totalCoverageIgnoringSubclass;
	double allHitOnlyOWLAx;
	double meanCoverage;
	double meanCoverageIgnoringSubclass;
	double meanStdDev;
	double meanMode;
	double meanMedian;
	
	public OWLAxEvaluation(ArrayList<ArrayList<HashMap<String,Integer>>> resultsList) {
		
		// results are lists of 2 things: ontology stats and OWLAx evaluations
		allResults = resultsList;
		
		//split the results list into ontology and owlax data
		owlaxResults = new ArrayList<HashMap<String,Integer>>();		
		ontologyCompositions = new ArrayList<HashMap<String,Integer>>();
		allResults.forEach(a -> {owlaxResults.add(a.get(0));ontologyCompositions.add(a.get(1));});
		
		int all = owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue))).values().stream().collect(Collectors.summingInt(a -> a));
		int allSubclassHits = owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getKey().equals("subclass"))).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue))).values().stream().collect(Collectors.summingInt(a -> a));
	    int allMisses = owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getKey().equals("other"))).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue))).values().stream().collect(Collectors.summingInt(a -> a));
	    allHitOnlyOWLAx = (double)(owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getKey().equals("scoped role domain") || a.getKey().equals("scoped role range")  || a.getKey().equals("existential")|| a.getKey().equals("inverse existential")  || a.getKey().equals("qualified functional role")  || a.getKey().equals("scoped functional role")  || a.getKey().equals("qualified scoped functional role") || a.getKey().equals("inverse qualified functional role")  || a.getKey().equals("inverse scoped functional role")  || a.getKey().equals("inverse qualified scoped functional role")  || a.getKey().equals("structural tautology"))).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingInt(Map.Entry::getValue))).values().stream().collect(Collectors.summingInt(a -> a))) / all;
	    
	    totalCoverage = (double)(all - allMisses) / all;
	    totalCoverageIgnoringSubclass  = (double)(all - allMisses - allSubclassHits) / all;
	    
		// calculating the means
		meanResult = (HashMap<String,Double>)owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingDouble(Map.Entry::getValue)));		
		meanOntology = (HashMap<String,Double>)ontologyCompositions.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getValue() >= 0)).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingDouble(Map.Entry::getValue)));

		// check out the unused ones
		unusedOWLAx = findUnused(owlaxResults);
		unusedOntologyAxioms = findUnused(ontologyCompositions);
		
		// average coverage for average result ignoring other
	    meanCoverage = (meanResult.values().stream().collect(Collectors.summingDouble(a -> a)) - meanResult.get("other")) / meanResult.size();
	    meanCoverageIgnoringSubclass = (meanResult.values().stream().collect(Collectors.summingDouble(a -> a)) - meanResult.get("other") - meanResult.get("subclass")) / meanResult.size();
		
	    // calculating the modes
		modeResult = owlaxResults.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow(IllegalArgumentException::new);
		modeOntology = ontologyCompositions.stream().collect(Collectors.groupingBy(a -> a, Collectors.counting())).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow(IllegalArgumentException::new);
	    modeOntology.values().removeIf(a -> a < 0);
		
	    // get average mode
		meanMode = modeResult.values().stream().collect(Collectors.summingInt(a -> a)) / modeResult.size();
		
		// calculating the medians
		medianResult = medians(owlaxResults);	
		medianOntology = medians(ontologyCompositions);
		
		// get average median
		meanMedian = medianResult.values().stream().collect(Collectors.summingDouble(a -> a)) / medianResult.size();
		
		// calculating std devs
		stdDevResult = stdDevs(owlaxResults,meanResult);	
		stdDevOntology = stdDevs(ontologyCompositions,meanOntology);
		
		// get average std dev
		meanStdDev = stdDevResult.values().stream().collect(Collectors.summingDouble(a -> a)) / stdDevResult.size();
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
	
	public HashMap<String,Double> getMeanOWLAxResult() {
		return meanResult;
	}
	
	public HashMap<String,Double> getMeanOntology() {
		return meanOntology;
	}
	
	public HashMap<String,Double> getStdDevOWLAxResult() {
		return stdDevResult;
	}
	
	public HashMap<String,Double> getStdDevOntology() {
		return stdDevOntology;
	}
	
	public HashMap<String,Integer> getModeOWLAxResult() {
		return modeResult;
	}
	
	public HashMap<String,Integer> getModeOntology() {
		return modeOntology;
	}
	
	public HashMap<String,Double> getMedianOWLAxResult() {
		return medianResult;
	}
	
	public HashMap<String,Double> getMedianOntology() {
		return medianOntology;
	}
	
	public ArrayList<String> getUnusedOWLAxAxioms(){
		return unusedOWLAx;
	}
	
	public ArrayList<String> getUnusedOntologyAxioms(){
		return unusedOntologyAxioms;
	}
	
	/**
	 * Gets the medians of a list of hashmaps
	 */
	private static HashMap<String,Double> medians(ArrayList<HashMap<String,Integer>> maps) {
		HashMap<String,Double> median = new HashMap<String,Double>();
		
		//check all keys
		for (String key : maps.get(0).keySet()) {
			//skip any labels
			if (maps.get(0).get(key) == null || maps.get(0).get(key) < 0) {continue;}
			//add matches with sorting
			Collections.sort(maps, (HashMap<String, Integer> one, HashMap<String, Integer> two) -> one.get(key).compareTo(two.get(key)));
			if(maps.size()%2 == 0) {
				median.put(key, (maps.get(maps.size()/2).get(key) + maps.get((maps.size()/2) + 1).get(key)) / 2.0);
			}else {
				median.put(key, (double)maps.get((maps.size()/2)+1).get(key));
			}			
		}
		return median;
	}
	
	/**
	 * Gets the Standard Deviations of the list of hashmaps
	 */
	private static HashMap<String,Double> stdDevs(ArrayList<HashMap<String,Integer>> maps,HashMap<String,Double> means) {
		HashMap<String,Double> stdDev = new HashMap<String,Double>();
		double val = 0.0;
		//check all keys
		for (String key : maps.get(0).keySet()) {
			//skip any labels
			if (maps.get(0).get(key) == null || maps.get(0).get(key) < 0) {continue;}
			//add matches
			for (HashMap<String,Integer> entry : maps) {
				val+=(entry.get(key)-means.get(key))*(entry.get(key)-means.get(key));
			}
			stdDev.put(key,Math.sqrt(val/maps.size()));
			val = 0.0;
		}
		return stdDev;
	}
	
	/**
	 * Finds any unused keys in a list of hashmaps
	 */
	private static ArrayList<String> findUnused(ArrayList<HashMap<String,Integer>> maps) {
		ArrayList<String> a = new ArrayList<String>();
		
		//check all keys
		for (String key : maps.get(0).keySet()) {
			//skip any labels
			if (maps.get(0).get(key) == null || maps.get(0).get(key) < 0) {continue;}
			//add matches
			if ( maps.stream().allMatch(map -> map.get(key) == 0)) {a.add(key);}
		}
		
		return a;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Evaluation Results:\n\nAll Axioms Combined:\nTotal Percent Coverage: %f\nTotal Percent Coverage Ignoring Subclass: %f\nTotal Percent Axioms OWLAx Unique: %f\nTotal Percent Missed: %f\nAverage Per-Axiom Coverage: %f\nAverage Per-Axiom Coverage Ignoring Subclass: %f\nAverage Per-Axiom Misses: %f\nAverage Per-Axiom Standard Deviation: %f\nAverage Per-Axiom Mode: %f\nAverage Per-Axiom Median: %f\n\n",
				totalCoverage,totalCoverageIgnoringSubclass,allHitOnlyOWLAx,1.0-totalCoverage,meanCoverage,meanCoverageIgnoringSubclass,meanResult.get("other")/meanResult.size(),meanStdDev,meanMode,meanMedian));
		sb.append(String.format("Mean OWLAx Result:\n\t%s\n\n",getMeanOWLAxResult().toString()));
		sb.append(String.format("Mode OWLAx Result:\n\t%s\n\n",getModeOWLAxResult().toString()));
		sb.append(String.format("Median OWLAx Result:\n\t%s\n\n",getMedianOWLAxResult().toString()));
		sb.append(String.format("Standard Deviation OWLAx Result:\n\t%s\n\n",getStdDevOWLAxResult().toString()));
		sb.append(String.format("\nMean Ontology Composition:\n\t%s\n\n",getMeanOntology().toString()));
		sb.append(String.format("Mode Ontology Composition:\n\t%s\n\n",getModeOntology().toString()));
		sb.append(String.format("Median Ontology Composition:\n\t%s\n\n",getMedianOntology().toString()));
		sb.append(String.format("Standard Deviation Ontology Composition:\n\t%s\n\n",getStdDevOntology().toString()));
		sb.append(String.format("Unused OWLAx Axioms:\n\n"));
		for (String result : getUnusedOWLAxAxioms()) {
			sb.append(String.format("\t%s\n\n",result));
		}
		sb.append(String.format("Unused Ontology Axioms:\n\n"));
		for (String result : getUnusedOntologyAxioms()) {
			sb.append(String.format("\t%s\n\n",result));
		}
		sb.append(String.format("Raw OWLAx Results:\n\n"));
		for (HashMap<String,Integer> result : getOWLAxResults()) {
			sb.append(String.format("\t%s\n\n",result.toString()));
		}
		sb.append(String.format("Raw Ontology Compositions:\n\n"));
		for (HashMap<String,Integer> result : getOntologyCompositions()) {
			sb.append(String.format("\t%s\n\n",result.toString()));
		}
		return sb.toString();
	}
	
}
