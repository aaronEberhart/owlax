package evaluation;

import java.util.HashMap;

import ontologyTools.Averageable;
import ontologyTools.OWLAxAxiomTypes;

/** 
 * This class is a standalone container for the results of an OWLAxMatcher execution.
 * @author cogan
 * @author aaron
 */
public class MatchingResult implements Averageable
{
	private HashMap<String, Integer> result;
	
	public MatchingResult()
	{
		// Initialize the result container
		result = new HashMap<String, Integer>()
		{
			private static final long serialVersionUID = 2L;
			{
				for (OWLAxAxiomTypes axiomType : OWLAxAxiomTypes.values())
				{
					put(axiomType.getAxiomType(), 0);
				}
			}
		};
	}
	
	public void incrementMatchTypeCount(String axiomType)
	{
		this.result.replace(axiomType, this.result.get(axiomType) + 1);
	}

	public int getCountByType(String axiomType)
	{
		return this.result.get(axiomType);
	}
	
	public int totalAxioms()
	{
		int sum = 0;
		
		for (OWLAxAxiomTypes axiomType : OWLAxAxiomTypes.values())
		{
			sum += this.result.get(axiomType.getAxiomType());
		}
		
		return sum;
	}
}
