package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calculates some basic statistics on the input Ontologies
 * 
 * @author Sulogna Chowdhury, Aaron Eberhart
 * @author DaSe Lab
 */
public class OWLAxEvaluation {
	
	private ArrayList<ArrayList<HashMap<String,Double>>> allResults;
	private ArrayList<HashMap<String,Double>> owlaxResults;
	private ArrayList<HashMap<String,Double>> owlaxCoverage;
	private ArrayList<HashMap<String,Double>> ontologyCompositions;
	private ArrayList<String> unusedOWLAx;
	private ArrayList<String> unusedOntologyAxioms;
	private HashMap<String,Double> meanResult;
	private HashMap<String,Double> meanOntology;
	private HashMap<String,Double> medianResult;
	private HashMap<String,Double> medianOntology;
	private HashMap<String,Double> modeResult;
	private HashMap<String,Double> modeOntology;
	private HashMap<String,Double> stdDevResult;
	private HashMap<String,Double> stdDevOntology;
	private double totalCoverage;
	private double totalCoverageIgnoringSubclass;
	private double allHitOnlyOWLAx;
	private double meanCoverage;
	private double meanCoverageIgnoringSubclass;
	private double meanStdDev;
	private double meanMode;
	private double meanMedian;
	private double avgCovgPercentAll;
	private double avgCovgPercentClass;
	private double avgCovgPercentSimple;
	
	/**
	 * Constructor that performs a simple evaluation on a list of ontology data and
	 * owlax usage data.
	 * 
	 * @param resultsList ArrayList&lt;ArrayList&lt;HashMap&lt;String,Integer&gt;&gt;&gt;
	 */
	public OWLAxEvaluation(ArrayList<ArrayList<HashMap<String,Double>>> resultsList) {
		
		// results are lists of 2 things: ontology stats and OWLAx evaluations
		allResults = resultsList;
		
		//split the results list into ontology and owlax data
		owlaxResults = new ArrayList<HashMap<String,Double>>();
		owlaxCoverage = new ArrayList<HashMap<String,Double>>();
		ontologyCompositions = new ArrayList<HashMap<String,Double>>();
		allResults.forEach(a -> {owlaxResults.add(a.get(0));ontologyCompositions.add(a.get(1));owlaxCoverage.add(a.get(2));});
		
		HashMap<String,Double> avgs = (HashMap<String,Double>)owlaxCoverage.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingDouble(Map.Entry::getValue)));
		avgCovgPercentAll = avgs.get("percent expressibility all axioms");
		avgCovgPercentClass = avgs.get("percent expressibility all class axioms");
		avgCovgPercentSimple = avgs.get("percent expressibility only simple class axioms");
		
		//precalc numerators and denominators
		double all = owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingDouble(Map.Entry::getValue))).values().stream().collect(Collectors.summingDouble(a -> a));
		double hits = all - owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getKey().equals("miss"))).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingDouble(Map.Entry::getValue))).values().stream().collect(Collectors.summingDouble(a -> a));
		double totalSubclass = owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getKey().equals("subclass"))).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingDouble(Map.Entry::getValue))).values().stream().collect(Collectors.summingDouble(a -> a));
		
		//find percents covered
		allHitOnlyOWLAx = (owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getKey().equals("scoped role domain") || a.getKey().equals("scoped role range")  || a.getKey().equals("existential")|| a.getKey().equals("inverse existential")  || a.getKey().equals("qualified functional role")  || a.getKey().equals("scoped functional role")  || a.getKey().equals("qualified scoped functional role") || a.getKey().equals("inverse qualified functional role")  || a.getKey().equals("inverse scoped functional role")  || a.getKey().equals("inverse qualified scoped functional role")  || a.getKey().equals("structural tautology"))).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.summingDouble(Map.Entry::getValue))).values().stream().collect(Collectors.summingDouble(a -> a))) / all;
		totalCoverage = hits / all;		
		totalCoverageIgnoringSubclass  = (hits - totalSubclass) / (all - totalSubclass);
	    
		// calculating the means
		meanResult = (HashMap<String,Double>)owlaxResults.stream().flatMap(hashMap -> hashMap.entrySet().stream()).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingDouble(Map.Entry::getValue)));		
		meanOntology = (HashMap<String,Double>)ontologyCompositions.stream().flatMap(hashMap -> hashMap.entrySet().stream().filter(a -> a.getValue() >= 0)).collect(Collectors.groupingBy(Map.Entry::getKey,Collectors.averagingDouble(Map.Entry::getValue)));
		
		// check out the unused ones
		unusedOWLAx = findUnused(owlaxResults);
		unusedOntologyAxioms = findUnused(ontologyCompositions);
		
		// average expressibility for average result ignoring other
		meanCoverage = (meanResult.values().stream().collect(Collectors.summingDouble(a -> a)) - meanResult.get("miss")) / meanResult.size();
		meanCoverageIgnoringSubclass = (meanResult.values().stream().collect(Collectors.summingDouble(a -> a)) - meanResult.get("miss") - meanResult.get("subclass")) / meanResult.size();
		
		// calculating the modes
		modeResult = owlaxResults.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow(IllegalArgumentException::new);
		modeOntology = ontologyCompositions.stream().collect(Collectors.groupingBy(a -> a, Collectors.counting())).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow(IllegalArgumentException::new);
		modeOntology.values().removeIf(a -> a < 0);
		
		// get average mode
		meanMode = modeResult.values().stream().collect(Collectors.summingDouble(a -> a)) / modeResult.size();
		
		// calculating std devs
		stdDevResult = stdDevs(owlaxResults,meanResult);	
		stdDevOntology = stdDevs(ontologyCompositions,meanOntology);
		
		// get average std dev
		meanStdDev = stdDevResult.values().stream().collect(Collectors.summingDouble(a -> a)) / stdDevResult.size();
		
	}

	/**
	 * calculate the medians of a list of hashmaps
	 */
	@SuppressWarnings("unused")
	private static HashMap<String,Double> medians(ArrayList<HashMap<String,Double>> maps) {
		HashMap<String,Double> median = new HashMap<String,Double>();
		
		//check all keys
		for (String key : maps.get(0).keySet()) {
			//skip any labels
			if (maps.get(0).get(key) == null || maps.get(0).get(key) < 0) {continue;}
			//add matches with sorting
			Collections.sort(maps, (HashMap<String, Double> one, HashMap<String, Double> two) -> one.get(key).compareTo(two.get(key)));
			if(maps.size()%2 == 0) {
				median.put(key, (maps.get(maps.size()/2).get(key) + maps.get((maps.size()/2) - 1).get(key)) / 2.0);
			}else {
				median.put(key, (double)maps.get((maps.size()/2)).get(key));
			}			
		}
		return median;
	}
	
	
	/**
	 * calculate the Standard Deviations of the list of hashmaps
	 */
	private static HashMap<String,Double> stdDevs(ArrayList<HashMap<String,Double>> maps,HashMap<String,Double> means) {
		HashMap<String,Double> stdDev = new HashMap<String,Double>();
		double val = 0.0;
		//check all keys
		for (String key : maps.get(0).keySet()) {
			//skip any labels
			if (maps.get(0).get(key) == null || maps.get(0).get(key) < 0) {continue;}
			//add matches
			for (HashMap<String,Double> entry : maps) {
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
	private static ArrayList<String> findUnused(ArrayList<HashMap<String,Double>> maps) {
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
	
	/**
	 * Gets the owlax results from the evaluation
	 * 
	 * @return ArrayList&lt;HashMap&lt;String,Integer&gt;&gt;
	 */
	public ArrayList<HashMap<String,Double>> getOWLAxResults(){
		return owlaxResults;
	}
	
	/**
	 * gets the ontology composition data from the evaluation
	 * 
	 * @return ArrayList&lt;HashMap&lt;String,Integer&gt;&gt;
	 */
	public ArrayList<HashMap<String,Double>> getOntologyCompositions(){
		return ontologyCompositions;
	}
	
	/**
	 * get all results used for the evaluation
	 * 
	 * @return ArrayList&lt;ArrayList&lt;HashMap&lt;String,Integer&gt;&gt;&gt;
	 */
	public ArrayList<ArrayList<HashMap<String,Double>>> getAllResults(){
		return allResults;
	}
	
	/**
	 * get the mean owlax result
	 * 
	 * @return HashMap&lt;String,Double&gt; 
	 */
	public HashMap<String,Double> getMeanOWLAxResult() {
		return meanResult;
	}
	
	/**
	 * get the mean ontology result
	 * 
	 * @return HashMap&lt;String,Double&gt; 
	 */
	public HashMap<String,Double> getMeanOntology() {
		return meanOntology;
	}
	
	/**
	 * get the standard deviations of the owlax results
	 * 
	 * @return HashMap&lt;String,Double&gt; 
	 */
	public HashMap<String,Double> getStdDevOWLAxResult() {
		return stdDevResult;
	}
	
	/**
	 * get the standard deviations of the ontology results
	 * 
	 * @return HashMap&lt;String,Double&gt;
	 */
	public HashMap<String,Double> getStdDevOntology() {
		return stdDevOntology;
	}
	
	/**
	 * get the modes of the owlax results
	 * 
	 * @return HashMap&lt;String,Integer&gt;
	 */
	public HashMap<String,Double> getModeOWLAxResult() {
		return modeResult;
	}
	
	/**
	 * get the modes of the ontology results
	 * 
	 * @return HashMap&lt;String,Integer&gt;
	 */
	public HashMap<String,Double> getModeOntology() {
		return modeOntology;
	}
	
	/**
	 * get the medians of the owlax results
	 * 
	 * @return HashMap&lt;String,Double&gt;
	 */
	public HashMap<String,Double> getMedianOWLAxResult() {
		return medianResult;
	}
	
	/**
	 * get the medians of the ontology results
	 * 
	 * @return HashMap&lt;String,Double&gt;
	 */
	public HashMap<String,Double> getMedianOntology() {
		return medianOntology;
	}
	
	/**
	 * gets the unused owlax axioms
	 * 
	 * @return ArrayList&lt;String&gt;
	 */
	public ArrayList<String> getUnusedOWLAxAxioms(){
		return unusedOWLAx;
	}
	
	/**
	 * gets the unused ontology axioms
	 * 
	 * @return ArrayList&lt;String&gt;
	 */
	public ArrayList<String> getUnusedOntologyAxioms(){
		return unusedOntologyAxioms;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Evaluation Results:\n\nAll Axioms Combined:\nTotal Percent Expressibility: %f\nTotal Percent Expressibility Ignoring Subclass: %f\nTotal Percent Axioms OWLAx Unique: %f\nTotal Percent Missed: %fAverage Ontology Expressibility"
				+ "\nAverage Axiom-Type Count: %f\nAverage Axiom-Type Count Ignoring Subclass: %f\nAverage Miss Count: %f\nAverage Axiom-Type Standard Deviation: %f\nAverage Axiom-Type Mode: %f\nAverage Axiom-Type Median: %f\n\n",totalCoverage,totalCoverageIgnoringSubclass,allHitOnlyOWLAx,1.0-totalCoverage,meanCoverage,meanCoverageIgnoringSubclass,meanResult.get("miss")/meanResult.size(),meanStdDev,meanMode,meanMedian));
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
		for (HashMap<String,Double> result : getOWLAxResults()) {
			sb.append(String.format("\t%s\n\n",result.toString()));
		}
		sb.append(String.format("Raw Ontology Compositions:\n\n"));
		for (HashMap<String,Double> result : getOntologyCompositions()) {
			sb.append(String.format("\t%s\n\n",result.toString()));
		}
		return sb.toString();
	}
	
	public String toCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("All Axioms Combined\nAverage Ontology Expressibility All Axioms,%f\nAverage Ontology Expressibility Class Axioms,%f\nAverage Ontology Expressibility Simple Class Axioms,%f\nTotal Percent Expressibility,%f\nTotal Percent Missed,"
				+ "%f\nTotal Percent Expressibility Ignoring Subclass,%f\nTotal Percent Missed Ignoring Subclass,%f\nTotal Percent Axioms OWLAx Unique,%f\nAverage Axiom-Type Count,"
				+ "%f\nAverage Axiom-Type Count Ignoring Subclass,%f\nAverage Miss Count,%f\nAverage Axiom-Type Count Standard Deviation,%f\nAverage Axiom-Type Count Mode,%f\nAverage Axiom-Type Count Median,%f",
				avgCovgPercentAll,avgCovgPercentClass,avgCovgPercentSimple,totalCoverage,1.0-totalCoverage,totalCoverageIgnoringSubclass,1.0-totalCoverageIgnoringSubclass,allHitOnlyOWLAx,meanCoverage,meanCoverageIgnoringSubclass,
				meanResult.get("miss")/meanResult.size(),meanStdDev,meanMode,meanMedian));
		sb.append("\n\n");
		
		Set<String> keys = new TreeSet<>(getMeanOWLAxResult().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nMean OWLAx Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getMeanOWLAxResult().get(key)));
		}
		sb.append("\n\n");
		keys = new TreeSet<>(getModeOWLAxResult().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nMode OWLAx Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getModeOWLAxResult().get(key)));
		}
		/*
		sb.append("\n\n");
		keys = new TreeSet<>(getMedianOWLAxResult().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nMedian OWLAx Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getMedianOWLAxResult().get(key)));
		}
		*/
		sb.append("\n\n");
		keys = new TreeSet<>(getStdDevOWLAxResult().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nStandard Deviation OWLAx Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getStdDevOWLAxResult().get(key)));
		}
		
		sb.append("\n\n\n");
		keys = new TreeSet<>(getMeanOntology().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nMean OWLAPI Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getMeanOntology().get(key)));
		}
		
		sb.append("\n\n");
		keys = new TreeSet<>(getModeOntology().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nMode OWLAPI Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getModeOntology().get(key)));
		}
		
		/*
		sb.append("\n\n");
		keys = new TreeSet<>(getMedianOntology().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nMedian OWLAPI Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getMedianOntology().get(key)));
		}
		*/
		
		sb.append("\n\n");
		keys = new TreeSet<>(getStdDevOntology().keySet());
		for(String key : keys) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\nStandard Deviation OWLAPI Count");
		for(String key : keys) {
			sb.append(String.format(",%f",getStdDevOntology().get(key)));
		}
		
		
		sb.append(String.format("\n\nUnused OWLAx Axioms"));
		for (String result : getUnusedOWLAxAxioms()) {
			sb.append(String.format(",%s",result));
		}
		
		sb.append(String.format("\nUnused OWLAPI Axioms"));
		for (String result : getUnusedOntologyAxioms()) {
			sb.append(String.format(",%s",result));
		}
		
		sb.append(String.format("\n\nRaw OWLAx Counts"));
		int i = 0;
		List<String> keys1 = new ArrayList<>(getOWLAxResults().get(0).keySet());
		for(String key : keys1) {
			sb.append(String.format(",%s",key));
		}
		List<String> keys2 = new ArrayList<>(owlaxCoverage.get(0).keySet());
		for(String key : keys2) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\n");
		for (int j = 0; j < getOWLAxResults().size(); j++) {
			HashMap<String,Double> result = getOWLAxResults().get(j);
			HashMap<String,Double> result2 = owlaxCoverage.get(j);
			sb.append(String.format("Ontology %d",i++));
			for(String key : keys1) {
				sb.append(String.format(",%f",result.get(key)));
			}
			for(String key : keys2) {
				sb.append(String.format(",%f",result2.get(key)));
			}
			sb.append("\n");
		}
		i = 0;
		sb.append(String.format("\nRaw OWLAPI Counts"));
		HashMap<String,Double> first = getOntologyCompositions().get(0);
		first.values().removeIf(a -> a < 0);
		keys1 = new ArrayList<>(first.keySet());
		for(String key : keys1) {
			sb.append(String.format(",%s",key));
		}
		sb.append("\n");
		for (int j = 0; j < getOntologyCompositions().size(); j++) {
			HashMap<String,Double> result = getOntologyCompositions().get(j);
			result.values().removeIf(a -> a < 0);
			sb.append(String.format("Ontology %d",i++));
			for(String key : keys1) {
				sb.append(String.format(",%f",result.get(key)));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
