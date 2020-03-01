package ontologyTools;

import java.util.*;

import org.eclipse.rdf4j.model.vocabulary.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;

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
			//range - ⊤ ⊑ ∀R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectAllValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),			
			//scoped range - A ⊑ ∀R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectAllValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//existential - A ⊑ ∃R.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectSomeValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//inverse existential - A ⊑ ∃R-.B
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectSomeValuesFromImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
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
	//hashmap keys
	private static final String[] axiomHashKeys = {"subclass","disjoint classes","role domain","scoped role domain","role range","scoped role range","existential","inverse existential","functional role","qualified functional role","scoped functional role","qualified scoped functional role","inverse functional role","inverse qualified functional role","inverse scoped functional role","inverse qualified scoped functional role","structural tautology"};
	private NormalizeAndSortAxioms normalizedAxioms;
	private HashMap<String,Integer> result;
	private HashMap<String,Integer> ontology;
	
	public OWLAxMatcher(NormalizeAndSortAxioms axioms) {
		normalizedAxioms = axioms;
		
		result = new HashMap<String,Integer>(){private static final long serialVersionUID = 1L;{
			int numClassAx = normalizedAxioms.getSimpleClassAxioms().size() + normalizedAxioms.getComplexClassAxioms().size();
			for (String key : axiomHashKeys) {
				int ran = new Random().nextInt((int)(numClassAx/10));
				put(key,ran);
				numClassAx = ran > numClassAx ? 0 : numClassAx - ran;
			}
		}};
		ontology = normalizedAxioms.getOntologyComposition();
	}
	
	public OWLAxMatcher(OWLOntology ontology) throws Exception {
		normalizedAxioms = new NormalizeAndSortAxioms(ontology);
	}
	
	public static int getMaxOWLAxAxiomSize() {
		return maxSize;
	}
	
	public List<OWLSubClassOfAxiom> getOWLAxAxioms() {
		return OWLAxAxioms;
	}
	
	public ArrayList<OWLSubClassOfAxiom> getSimpleClassAxioms() {
		return normalizedAxioms.getSimpleClassAxioms();
	}
	
	public ArrayList<OWLSubClassOfAxiom> getComplexClassAxioms() {
		return normalizedAxioms.getComplexClassAxioms();
	}
	
	public ArrayList<OWLPropertyAxiom> getRoleAxioms() {
		return normalizedAxioms.getRoleAxioms();
	}
	
	public String getOWLAxAxiomsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OWLAx Axioms:\n");
		for (OWLSubClassOfAxiom s : OWLAxAxioms) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),NormalizeAndSortAxioms.getSubClassOfAxiomSize(s)));
		}
		return sb.toString();
	}
	
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
