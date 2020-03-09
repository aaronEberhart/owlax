package ontologyTools;

import java.util.HashMap;

public class OntologyComposition
{
	private String ontologyFormat;
	private String ontologyID;
	private int    nClassesInSignature;
	private int    nObjectPropertiesInSignature;
	private int    nDataPropertiesInSignature;
	private int    nSimpleClassAxioms;
	private int    nComplexClassAxioms;
	private int    nRoleAxioms;

	private HashMap<String, Integer> ontologyComposition;

	public OntologyComposition()
	{
		this.ontologyComposition = new HashMap<String, Integer>()
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

	public int totalAxioms()
	{
		int sum = 0;

		for (OWLAxAxiomTypes axiomType : OWLAxAxiomTypes.values())
		{
			sum += this.ontologyComposition.get(axiomType.getAxiomType());
		}

		return sum;
	}

	public void incrementAxiomTypeCount(String axiomType)
	{
		this.incrementAxiomTypeCount(axiomType, 1);
	}

	public void incrementAxiomTypeCount(String axiomType, int n)
	{
		this.ontologyComposition.replace(axiomType, this.ontologyComposition.get(axiomType) + n);
	}

	public int getCountByType(String axiomType)
	{
		return this.ontologyComposition.get(axiomType);
	}

	public String getOntologyFormat()
	{
		return ontologyFormat;
	}

	public void setOntologyFormat(String ontologyFormat)
	{
		this.ontologyFormat = ontologyFormat;
	}

	public String getOntologyID()
	{
		return ontologyID;
	}

	public void setOntologyID(String ontologyID)
	{
		this.ontologyID = ontologyID;
	}

	public int getnClassesInSignature()
	{
		return nClassesInSignature;
	}

	public void setnClassesInSignature(int nClassesInSignature)
	{
		this.nClassesInSignature = nClassesInSignature;
	}

	public int getnObjectPropertiesInSignature()
	{
		return nObjectPropertiesInSignature;
	}

	public void setnObjectPropertiesInSignature(int nObjectPropertiesInSignature)
	{
		this.nObjectPropertiesInSignature = nObjectPropertiesInSignature;
	}

	public int getnDataPropertiesInSignature()
	{
		return nDataPropertiesInSignature;
	}

	public void setnDataPropertiesInSignature(int nDataPropertiesInSignature)
	{
		this.nDataPropertiesInSignature = nDataPropertiesInSignature;
	}

	public int getnSimpleClassAxioms()
	{
		return nSimpleClassAxioms;
	}

	public void setnSimpleClassAxioms(int nSimpleClassAxioms)
	{
		this.nSimpleClassAxioms = nSimpleClassAxioms;
	}

	public int getnComplexClassAxioms()
	{
		return nComplexClassAxioms;
	}

	public void setnComplexClassAxioms(int nComplexClassAxioms)
	{
		this.nComplexClassAxioms = nComplexClassAxioms;
	}

	public int getnRoleAxioms()
	{
		return nRoleAxioms;
	}

	public void setnRoleAxioms(int nRoleAxioms)
	{
		this.nRoleAxioms = nRoleAxioms;
	}

	public HashMap<String, Integer> getOntologyComposition()
	{
		return ontologyComposition;
	}

	public void setOntologyComposition(HashMap<String, Integer> ontologyComposition)
	{
		this.ontologyComposition = ontologyComposition;
	}
}
