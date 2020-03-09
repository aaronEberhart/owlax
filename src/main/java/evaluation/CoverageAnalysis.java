package evaluation;

import ontologyTools.OntologyComposition;

public class CoverageAnalysis
{
	private MatchingResult      matchingResult;
	private OntologyComposition ontologyComposition;
	
	public CoverageAnalysis()
	{

	}

	public CoverageAnalysis(MatchingResult matchingResult, OntologyComposition ontologyComposition)
	{
		this.matchingResult = matchingResult;
		this.ontologyComposition = ontologyComposition;
	}

	public MatchingResult getMatchingResult()
	{
		return matchingResult;
	}

	public void setMatchingResult(MatchingResult matchingResult)
	{
		this.matchingResult = matchingResult;
	}

	public OntologyComposition getOntologyComposition()
	{
		return ontologyComposition;
	}

	public void setOntologyComposition(OntologyComposition ontologyComposition)
	{
		this.ontologyComposition = ontologyComposition;
	}
	
}
