package ontologyTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import evaluation.CoverageAnalysis;
import evaluation.MatchingResult;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectInverseOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

/**
 * Checks the normalized axioms in an ontology to see if they match the OWLAx
 * axioms
 * 
 * @author Aaron Eberhart
 * @author DaSe Lab
 */
public class OWLAxMatcher
{
	//@formatter:off
	// all axioms used by OWLAx
	private static final List<OWLSubClassOfAxiom> OWLAxAxioms = Arrays.asList(
			//subclass - A ⊑ B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLClassImpl(IRI.create("B")), Collections.emptyList()),
			//disjoint classes - A ⊓ B ⊑ ⊥
			new OWLSubClassOfAxiomImpl(new OWLObjectIntersectionOfImpl(new ArrayList<OWLClassExpression>(Arrays.asList(new OWLClassImpl(IRI.create("A")),new OWLClassImpl(IRI.create("B")))).stream()), new OWLClassImpl(IRI.create(OWL.NOTHING.toString())), Collections.emptyList()),
			//domain - ∃R.⊤ ⊑ A
			new OWLSubClassOfAxiomImpl(new OWLObjectSomeValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create(OWL.THING.toString()))), new OWLClassImpl(IRI.create("A")), Collections.emptyList()),
			//scoped domain - ∃R.B ⊑ A
			new OWLSubClassOfAxiomImpl(new OWLObjectSomeValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), new OWLClassImpl(IRI.create("A")), Collections.emptyList()),
			//existential - A ⊑ ∃R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectSomeValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//inverse existential - A ⊑ ∃R-.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectSomeValuesFromImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//range - ⊤ ⊑ ∀R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectAllValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),			
			//scoped range - A ⊑ ∀R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectAllValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//functional - ⊤ ⊑ <=1R.T
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			//qualified functional - ⊤ ⊑ <=1R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),			
			//scoped functional - A ⊑ <=1R.T
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			//qualified scoped functional - A ⊑ <=1R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),	
			//inverse functional - ⊤ ⊑ <=1R-.T
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			//inverse qualified functional - ⊤ ⊑ <=1R-.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),			
			//inverse scoped functional - A ⊑ <=1R-.T
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			//inverse qualified scoped functional - A ⊑ <=1R-.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//structural tautology - A ⊑ >=0R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMinCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),0,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()));
	//@formatter:on
	// max size of all the axioms (should be 3...)
	private static final int         maxSize       = OWLAxAxioms.stream()
			.mapToInt(a -> NormalizeAndSortAxioms.getSubClassOfAxiomSize(a)).max().getAsInt();
	private NormalizeAndSortAxioms   normalizedAxioms;
	
	private MatchingResult matchingResult;
	private OntologyComposition ontologyComposition;
	
	/**
	 * Checks appropriate class axioms from the normalized to see if they match
	 * OWLAx axioms
	 * 
	 * @param normalizedAxioms NormalizeAndSortAxioms
	 */
	public OWLAxMatcher(NormalizeAndSortAxioms normalizedAxioms) throws Exception
	{
		this.normalizedAxioms = normalizedAxioms;

		this.ontologyComposition = this.normalizedAxioms.getOntologyComposition();
		this.matchingResult = new MatchingResult();

		matchAxioms(this.normalizedAxioms.getSimpleClassAxioms());
	}

	/**
	 * Checks the ontology to see if any of its axioms match OWLAx axioms
	 * 
	 * @param inputOntology OWLOntology
	 */
	public OWLAxMatcher(OWLOntology inputOntology) throws Exception
	{
		this(new NormalizeAndSortAxioms(inputOntology));
	}

	//@formatter:off
	/**
	 * Matches simple class axioms axioms to OWLAx axioms 
	 */
	private void matchAxioms(List<OWLSubClassOfAxiom> axioms) throws Exception {
		
		for (OWLSubClassOfAxiom axiom : axioms) {
			//subclass
			if (NormalizeAndSortAxioms.getSubClassOfAxiomSize(axiom) == 2) {
				matchingResult.incrementMatchTypeCount("subclass");
			//disjoint classes
			}else if(axiom.getSuperClass().isBottomEntity()) {
				matchingResult.incrementMatchTypeCount("disjoint classes");
			//domain
			}else if(axiom.getSubClass().getClassExpressionType().getName().equals("ObjectSomeValuesFrom") && axiom.getSuperClass().isOWLClass()) {
				//regular
				if (((OWLObjectSomeValuesFrom)axiom.getSubClass()).getFiller().isTopEntity()) {
					matchingResult.incrementMatchTypeCount("scoped role domain");
				//scoped
				}else {
					matchingResult.incrementMatchTypeCount("role domain");
				}
			//existential
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectSomeValuesFrom") && axiom.getSubClass().isOWLClass()) {
				//is is specifically NOT an inverse
				if(!isInverse(((OWLObjectSomeValuesFrom)axiom.getSuperClass()).getProperty())) {
					matchingResult.incrementMatchTypeCount("existential");
				//must be regular
				}else {
					matchingResult.incrementMatchTypeCount("inverse existential");
				}
			//range
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectAllValuesFrom") && axiom.getSubClass().isOWLClass()) {
				//regular
				if (axiom.getSubClass().isTopEntity()) {
					matchingResult.incrementMatchTypeCount("role range");
				//scoped
				}else {
					matchingResult.incrementMatchTypeCount("scoped role range");
				}
			//functional
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectMaxCardinality") && ((OWLObjectMaxCardinality)axiom.getSuperClass()).getCardinality() == 1) {
				if (!isInverse(((OWLObjectMaxCardinality)axiom.getSuperClass()).getProperty())) {
					//regular
					if(axiom.getSubClass().isTopEntity() && ((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {
						matchingResult.incrementMatchTypeCount("functional role");
					//scoped
					}else if(axiom.getSubClass().isTopEntity()) {
						matchingResult.incrementMatchTypeCount("qualified functional role");
					//qualified
					}else if(((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {
						matchingResult.incrementMatchTypeCount("scoped functional role");
					//scoped qualified
					}else {
						matchingResult.incrementMatchTypeCount("qualified scoped functional role");
					}
				//inverse functional
				}else {
					//regular
					if(axiom.getSubClass().isTopEntity() && ((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {				
						matchingResult.incrementMatchTypeCount("inverse functional role");
					//scoped
					}else if(axiom.getSubClass().isTopEntity()) {
						matchingResult.incrementMatchTypeCount("inverse qualified functional role");
					//qualified
					}else if(((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {
						matchingResult.incrementMatchTypeCount("inverse scoped functional role");
					//scoped qualified
					}else {
						matchingResult.incrementMatchTypeCount("inverse qualified scoped functional role");
					}
				}
			//tautology
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectMinCardinality") && ((OWLObjectMinCardinality)axiom.getSuperClass()).getCardinality() == 0) {
				matchingResult.incrementMatchTypeCount("structural tautology");
			}else {
				matchingResult.incrementMatchTypeCount("other");
			}
		}
	}
	//@formatter:on

	/**
	 * Tests to see whether a property is an inverse
	 */
	private boolean isInverse(OWLObjectPropertyExpression expression)
	{
		return !expression.getNamedProperty().toString().equals(expression.getSimplified().toString());
	}

	/**
	 * Gets the OWLAx axioms
	 * 
	 * @return List<OWLSubClassOfAxiom>
	 */
	public List<OWLSubClassOfAxiom> getOWLAxAxioms()
	{
		return OWLAxAxioms;
	}

	/**
	 * Gets the axioms from the ontology that are smaller than or the same size as
	 * the maximum size in OWLAx
	 * 
	 * @return axioms ArrayList<OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getSimpleClassAxioms()
	{
		return normalizedAxioms.getSimpleClassAxioms();
	}

	/**
	 * Gets the axioms from the ontology that are bigger than the maximum size in
	 * OWLAx
	 * 
	 * @return axioms ArrayList<OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getComplexClassAxioms()
	{
		return normalizedAxioms.getComplexClassAxioms();
	}

	/**
	 * Gets the role axioms from the ontology
	 * 
	 * @return axioms ArrayList<OWLPropertyAxiom>
	 */
	public ArrayList<OWLPropertyAxiom> getRoleAxioms()
	{
		return normalizedAxioms.getRoleAxioms();
	}

	/**
	 * Get a string representation of the OWLAx axioms
	 * 
	 * @return axioms String
	 */
	public String getOWLAxAxiomsString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("OWLAx Axioms:\n");
		for (OWLSubClassOfAxiom s : OWLAxAxioms)
		{
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n", s.toString(),
					NormalizeAndSortAxioms.getSubClassOfAxiomSize(s)));
		}
		return sb.toString();
	}

	/**
	 * Gets the maximum size of an OWLAx axiom
	 * 
	 * @return maxSize int
	 */
	public static int getMaxOWLAxAxiomSize()
	{
		return maxSize;
	}

	/**
	 * Returns a matching result paired with its ontology
	 * 
	 * @return matches,ontologies ArrayList<HashMap<String,Integer>>
	 */
	public CoverageAnalysis doAnalysis()
	{
		CoverageAnalysis coverageAnalysis = new CoverageAnalysis(matchingResult, ontologyComposition);
		return coverageAnalysis;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Class Axioms:\n");
		for (OWLSubClassOfAxiom s : getSimpleClassAxioms())
		{
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n", s.toString(),
					NormalizeAndSortAxioms.getSubClassOfAxiomSize(s)));
		}
		sb.append("\nComplex Class Axioms:\n");
		for (OWLSubClassOfAxiom s : getComplexClassAxioms())
		{
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n", s.toString(),
					NormalizeAndSortAxioms.getSubClassOfAxiomSize(s)));
		}
		sb.append("\nRole Axioms:\n");
		for (OWLPropertyAxiom s : getRoleAxioms())
		{
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n", s.toString(),
					NormalizeAndSortAxioms.getObjectPropertyAxiomSize(s)));
		}
		return sb.toString();
	}
}
