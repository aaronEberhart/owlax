package evaluation;


import java.util.ArrayList;
import java.util.HashMap;

import ontologyTools.OWLAxAxiomTypes;
import ontologyTools.OntologyComposition;

public class OWLAxEvaluation
{

	private ArrayList<CoverageAnalysis>    allCoverageAnalyses;
	private ArrayList<MatchingResult>      owlaxResults;
	private ArrayList<OntologyComposition> ontologyCompositions;
	private HashMap<String, Double>        averageResult;
	private HashMap<String, Double>        averageOntology;
	double                                 totalCoverage;
	float                                  totalAxioms;
	float                                  totalclasses;
	float                                  average_total;
	double                                 array_size;
	HashMap<String, Integer>               val;
	HashMap<String, Integer>               val_ont;

	public OWLAxEvaluation(ArrayList<CoverageAnalysis> allCoverageAnalyses)
	{
		// All matching result - ontology composition pairs
		this.allCoverageAnalyses = allCoverageAnalyses;

		// split the results list into ontology and owlax data
		this.owlaxResults = new ArrayList<>();
		this.ontologyCompositions = new ArrayList<>();

		allCoverageAnalyses.forEach(a -> {
			this.owlaxResults.add(a.getMatchingResult());
			this.ontologyCompositions.add(a.getOntologyComposition());
		});
		
		//
		doEvaluation();
	}
	public void doEvaluation()
	{
		// calculating the averages
		averageResult = calculateAverageCoverage(allCoverageAnalyses);

//		averageOntology = (HashMap<String, Double>) ontologyCompositions.stream()
//				.flatMap(hashMap -> hashMap.entrySet().stream())
//				.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingInt(Map.Entry::getValue)));

		// removes the labels from the average ontology
		averageOntology.values().removeIf(a -> a < 0);

		// average coverage for average result ignoring other
		totalCoverage = calculateAverage(allCoverageAnalyses);
				
//		averageResult.values().stream().reduce(0.0,
//				(a, b) -> (averageResult.get("other") == a || averageResult.get("other") == b) ? 0 : a + b)
//				/ allResults.size();

	}

	public double calculateAverage(ArrayList<CoverageAnalysis> coverageAnalyses)
	{
		int covered = 0;
		int total = 0;
		for(CoverageAnalysis coverageAnalysis : coverageAnalyses)
		{
			MatchingResult matchingResult = coverageAnalysis.getMatchingResult();
			OntologyComposition ontologyComposition = coverageAnalysis.getOntologyComposition();
		
			covered += matchingResult.totalAxioms();
			total += ontologyComposition.totalAxioms();
		}
		
		double average = covered / total;
		
		return average;
	}

	public HashMap<String, Double> calculateAverageCoverage(ArrayList<CoverageAnalysis> coverageAnalyses)
	{
		HashMap<String, Double> averageCoverages = new HashMap<>();
		OntologyComposition coveredByType = new OntologyComposition();
		OntologyComposition totalByType = new OntologyComposition();
		// Compute Totals
		for(CoverageAnalysis coverageAnalysis : coverageAnalyses)
		{
			MatchingResult matchingResult = coverageAnalysis.getMatchingResult();
			OntologyComposition ontologyComposition = coverageAnalysis.getOntologyComposition();
			
			for (OWLAxAxiomTypes key : OWLAxAxiomTypes.values())
			{
				String axiomType = key.getAxiomType();
				coveredByType.incrementAxiomTypeCount(axiomType, matchingResult.getCountByType(axiomType));
				totalByType.incrementAxiomTypeCount(axiomType, ontologyComposition.getCountByType(axiomType));
			}
		}
		
		// Compute Average
		for (OWLAxAxiomTypes key : OWLAxAxiomTypes.values())
		{
			String axiomType = key.getAxiomType();

			int covered = coveredByType.getCountByType(axiomType);
			int total = totalByType.getCountByType(axiomType);
			
			double average = covered / total;
			averageCoverages.put(axiomType, average);
		}
		
		return averageCoverages;
	}
	
//	public HashMap<String, Double> getEvaluationResults()
//	{
//		return this.averageResult;
//	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Evaluation Results:\n\n"));
		// TODO
		sb.append(String.format(getvalue()));
		return sb.toString();
	}

	public String getvalue()
	{
//		array_size = owlaxResults.size();
//		StringBuilder newval = new StringBuilder();
//		for (int i = 0; i <= array_size - 1; i = i + 1)
//		{
//			val = owlaxResults.get(i);
//
//			// total axioms in oxlax
//			totalAxioms = 0.0f;
//			for (float f : val.values())
//			{
//				totalAxioms += f;
//			}
//
//			ArrayList<HashMap<String, Integer>> ontology_result = ontologyCompositions;
//			val_ont = ontology_result.get(i);
//
//			// total classes in ontology
//			totalclasses = 0.0f;
//			for (float f : val_ont.values())
//			{
//				totalclasses += f;
//			}
//
//			// average
//			average_total = totalAxioms / totalclasses;
//
//			newval.append(String.format("\n\nTotal Owlax matched axioms: " + totalAxioms));
//			newval.append(String.format("\n\nTotal classes :- " + totalclasses));
//			newval.append(String.format("\n\nAverage :- " + average_total));
//
//		}
//		return newval.toString();
		return null;
	}
}
