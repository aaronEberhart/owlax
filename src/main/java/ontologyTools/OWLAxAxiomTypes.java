package ontologyTools;

public enum OWLAxAxiomTypes
{
	SUBCLASS("subclass"),
	DISJOINT_CLASSES("disjoint classes"),
	ROLE_DOMAIN("role domain"),
	SCOPED_ROLE_DOMAIN("scoped role domain"),
	ROLE_CHANGE("role range"),
	SCOPED_ROLE_CHANGE("scoped role range"),
	EXISTENTIAL("existential"),
	INVERSE_EXISTENTIAL("inverse existential"),
	FUNCTIONAL_ROLE("functional role"),
	QUALIFIED_FUNCTIONAL_ROLE("qualified functional role"),
	SCOPED_FUNCTIONAL_ROLE("scoped functional role"),
	QUALIFIED_SCOPED_FUNCTIONAL_ROLE("qualified scoped functional role"),
	INVERSE_FUNCTIONAL_ROLE("inverse functional role"),
	INVERSE_QUALIFIED_FUNCTIONAL_ROLE("inverse qualified functional role"),
	INVERSE_SCOPED_FUNCTIONAL_ROLE("inverse scoped functional role"),
	INVERSE_QUALIFIED_SCOPED_FUNCTIONAL_ROLE("inverse qualified scoped functional role"),
	STRUCTURAL_TAUTOLOGY("structural tautology"),
	OTHER("other");
	
	private String axiomType;
	
	OWLAxAxiomTypes(String axiomType)
	{
		this.axiomType = axiomType;
	}
	
	public String getAxiomType()
	{
		return this.axiomType;
	}
}
