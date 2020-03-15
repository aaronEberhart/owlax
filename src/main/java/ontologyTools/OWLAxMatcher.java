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
 * Checks the normalized axioms in an ontology to see if they match 
 * the OWLAx axioms
 * 
 * @author Aaron Eberhart
 * @author DaSe Lab
 */
public class OWLAxMatcher {
	
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
	// max size of all the axioms (should be 3...)
	private static final int maxSize = OWLAxAxioms.stream().mapToInt(a -> NormalizeAndSortAxioms.getSubClassOfAxiomSize(a)).max().getAsInt();
	private static final String[] axiomHashKeys = {"subclass","disjoint classes","role domain","scoped role domain","role range","scoped role range","existential","inverse existential","functional role","qualified functional role","scoped functional role","qualified scoped functional role","inverse functional role","inverse qualified functional role","inverse scoped functional role","inverse qualified scoped functional role","structural tautology","other"};
	private NormalizeAndSortAxioms normalizedAxioms;
	private HashMap<String,Integer> result;
	private HashMap<String,Integer> ontology;
	
	/**
	 * Checks appropriate class axioms from the normalized to see if they match OWLAx axioms
	 * 
	 * @param axioms NormalizeAndSortAxioms
	 */
	public OWLAxMatcher(NormalizeAndSortAxioms axioms) {
		normalizedAxioms = axioms;
		
		result = new HashMap<String,Integer>(){private static final long serialVersionUID = 2L;{for (String key : axiomHashKeys) {put(key,0);}}};
		
		ontology = normalizedAxioms.getOntologyComposition();
		
		matchAxioms(normalizedAxioms.getSimpleClassAxioms());
	}

	/**
	 * Checks the ontology to see if any of its axioms match OWLAx axioms
	 * 
	 * @param inputOntology - OWLOntology
	 */
	public OWLAxMatcher(OWLOntology inputOntology) {
		normalizedAxioms = new NormalizeAndSortAxioms(inputOntology);
		
		result = new HashMap<String,Integer>(){private static final long serialVersionUID = 1L;{for (String key : axiomHashKeys) {put(key,0);}}};
		
		ontology = normalizedAxioms.getOntologyComposition();
		
		matchAxioms(normalizedAxioms.getSimpleClassAxioms());
	}
	
	/**
	 * Matches simple class axioms axioms to OWLAx axioms 
	 */
	private void matchAxioms(List<OWLSubClassOfAxiom> axioms) {
		
		for (OWLSubClassOfAxiom axiom : axioms) {
			
			//subclass
			if (NormalizeAndSortAxioms.getSubClassOfAxiomSize(axiom) == 2) {
				result.replace("subclass", result.get("subclass") + 1);
			//disjoint classes
			}else if(axiom.getSuperClass().isBottomEntity()) {
				result.replace("disjoint classes", result.get("disjoint classes") + 1);
			//domain
			}else if(axiom.getSubClass().getClassExpressionType().getName().equals("ObjectSomeValuesFrom") && axiom.getSuperClass().isOWLClass()) {
				//regular
				if (((OWLObjectSomeValuesFrom)axiom.getSubClass()).getFiller().isTopEntity()) {
					result.replace("scoped role domain", result.get("scoped role domain") + 1);
				//scoped
				}else {
					result.replace("role domain", result.get("role domain") + 1);
				}
			//existential
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectSomeValuesFrom") && axiom.getSubClass().isOWLClass()) {
				//is is specifically NOT an inverse
				if(!isInverse(((OWLObjectSomeValuesFrom)axiom.getSuperClass()).getProperty())) {
					result.replace("existential", result.get("existential") + 1);
				//must be regular
				}else {
					result.replace("inverse existential", result.get("inverse existential") + 1);
				}
			//range
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectAllValuesFrom") && axiom.getSubClass().isOWLClass()) {
				//regular
				if (axiom.getSubClass().isTopEntity()) {
					result.replace("role range", result.get("role range") + 1);
				//scoped
				}else {
					result.replace("scoped role range", result.get("scoped role range") + 1);
				}
			//functional
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectMaxCardinality") && ((OWLObjectMaxCardinality)axiom.getSuperClass()).getCardinality() == 1) {
				if (!isInverse(((OWLObjectMaxCardinality)axiom.getSuperClass()).getProperty())) {
					//regular
					if(axiom.getSubClass().isTopEntity() && ((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {
						result.replace("functional role", result.get("functional role") + 1);
					//scoped
					}else if(axiom.getSubClass().isTopEntity()) {
						result.replace("qualified functional role", result.get("qualified functional role") + 1);
					//qualified
					}else if(((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {
						result.replace("scoped functional role", result.get("scoped functional role") + 1);
					//scoped qualified
					}else {
						result.replace("qualified scoped functional role", result.get("qualified scoped functional role") + 1);
					}
				//inverse functional
				}else {
					//regular
					if(axiom.getSubClass().isTopEntity() && ((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {				
						result.replace("inverse functional role", result.get("inverse functional role") + 1);
					//scoped
					}else if(axiom.getSubClass().isTopEntity()) {
						result.replace("inverse qualified functional role", result.get("inverse qualified functional role") + 1);
					//qualified
					}else if(((OWLObjectMaxCardinality)axiom.getSuperClass()).getFiller().isTopEntity()) {
						result.replace("inverse scoped functional role", result.get("inverse scoped functional role") + 1);
					//scoped qualified
					}else {
						result.replace("inverse qualified scoped functional role", result.get("inverse qualified scoped functional role") + 1);
					}
				}
			//tautology
			}else if(axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectMinCardinality") && ((OWLObjectMinCardinality)axiom.getSuperClass()).getCardinality() == 0) {
				result.replace("structural tautology", result.get("structural tautology") + 1);
			}else {
				result.replace("other", result.get("other") + 1);
			}
		}
	}
	
	/**
	 * Tests to see whether a property is an inverse
	 */
	private boolean isInverse(OWLObjectPropertyExpression expression) {
		return !expression.getNamedProperty().toString().equals(expression.getSimplified().toString());
	}
	
	/**
	 * Gets the OWLAx axioms
	 * 
	 * @return List&lt;OWLSubClassOfAxiom>
	 */
	public List<OWLSubClassOfAxiom> getOWLAxAxioms() {
		return OWLAxAxioms;
	}
	
	/**
	 * Gets the axioms from the ontology that are smaller than or the same size 
	 * as the maximum size in OWLAx
	 * 
	 * @return ArrayList&lt;OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getSimpleClassAxioms() {
		return normalizedAxioms.getSimpleClassAxioms();
	}
	
	/**
	 * Gets the axioms from the ontology that are bigger than the maximum size in OWLAx
	 * 
	 * @return ArrayList&lt;OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getComplexClassAxioms() {
		return normalizedAxioms.getComplexClassAxioms();
	}
	
	/**
	 * Gets the role axioms from the ontology
	 * 
	 * @return ArrayList&lt;OWLPropertyAxiom>
	 */
	public ArrayList<OWLPropertyAxiom> getRoleAxioms() {
		return normalizedAxioms.getRoleAxioms();
	}
	
	/**
	 * Get a string representation of the OWLAx axioms
	 * 
	 * @return String
	 */
	public String getOWLAxAxiomsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OWLAx Axioms:\n");
		for (OWLSubClassOfAxiom s : OWLAxAxioms) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),NormalizeAndSortAxioms.getSubClassOfAxiomSize(s)));
		}
		return sb.toString();
	}

	/**
	 * Gets the maximum size of an OWLAx axiom
	 * 
	 * @return int
	 */
	public static int getMaxOWLAxAxiomSize() {
		return maxSize;
	}
	
	/**
	 * Returns a matching result paired with its ontology
	 * 
	 * @return ArrayList&lt;HashMap&lt;String,Integer>>
	 */
	public ArrayList<HashMap<String,Integer>> getMatches(){
		return new ArrayList<HashMap<String,Integer>>(Arrays.asList(result,ontology));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Class Axioms:\n");
		for (OWLSubClassOfAxiom s : getSimpleClassAxioms()) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),NormalizeAndSortAxioms.getSubClassOfAxiomSize(s)));
		}
		sb.append("\nComplex Class Axioms:\n");
		for (OWLSubClassOfAxiom s : getComplexClassAxioms()) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),NormalizeAndSortAxioms.getSubClassOfAxiomSize(s)));
		}
		sb.append("\nRole Axioms:\n");
		for (OWLPropertyAxiom s : getRoleAxioms()) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),NormalizeAndSortAxioms.getObjectPropertyAxiomSize(s)));
		}
		return sb.toString();
	}
}
