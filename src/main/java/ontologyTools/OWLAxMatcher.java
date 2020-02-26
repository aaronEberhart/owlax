package ontologyTools;

import java.util.*;

import org.eclipse.rdf4j.model.vocabulary.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;

public class OWLAxMatcher {
	
	/**
	 * 
	 * AXIOMS
	 * 
	 * 01 A ⊑ B
	 * 02 A ⊓ B ⊑ ⊥
	 * 03 ∃R.⊤ ⊑ A
	 * 04 ∃R.B ⊑ A
	 * 05 ⊤ ⊑ ∀R.B
	 * 06 A ⊑ ∀R.B
	 * 07 A ⊑ ∃R.B
	 * 08 A ⊑ ∃R-.B
	 * 09 ⊤ ⊑ <=1R.T
	 * 10 ⊤ ⊑ <=1R.B
	 * 11 A ⊑ <=1R.T
	 * 12 A ⊑ <=1R.B
	 * 13 ⊤ ⊑ <=1R-.T
	 * 14 ⊤ ⊑ <=1R-.B
	 * 15 A ⊑ <=1R-.T
	 * 16 A ⊑ <=1R-.B
	 * 17 A ⊑ >=0R.B
	 *  
	 **/
	private static List<OWLSubClassOfAxiom> owlaxioms = Arrays.asList(
			//subclass
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLClassImpl(IRI.create("B")), Collections.emptyList()),
			//disjoint class
			new OWLSubClassOfAxiomImpl(new OWLObjectIntersectionOfImpl(new ArrayList<OWLClassExpression>(Arrays.asList(new OWLClassImpl(IRI.create("A")),new OWLClassImpl(IRI.create("B")))).stream()), new OWLClassImpl(IRI.create(OWL.NOTHING.toString())), Collections.emptyList()),
			//domain
			new OWLSubClassOfAxiomImpl(new OWLObjectSomeValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create(OWL.THING.toString()))), new OWLClassImpl(IRI.create("A")), Collections.emptyList()),
			new OWLSubClassOfAxiomImpl(new OWLObjectSomeValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), new OWLClassImpl(IRI.create("A")), Collections.emptyList()),
			//range
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectAllValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),			
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectAllValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//existential
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectSomeValuesFromImpl(new OWLObjectPropertyImpl(IRI.create("R")), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectSomeValuesFromImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))), new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//functional
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),			
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),	
			//inverse functional
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),			
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()),
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMaxCardinalityImpl(new OWLObjectInverseOfImpl(new OWLObjectPropertyImpl(IRI.create("R"))),1,new OWLClassImpl(IRI.create("B"))), Collections.emptyList()),
			//tautology
			new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create("A")), new OWLObjectMinCardinalityImpl(new OWLObjectPropertyImpl(IRI.create("R")),0,new OWLClassImpl(IRI.create("B"))), Collections.emptyList())	
			);
	// max size of all the axioms (should be 3...)
	protected static final int maxSize = owlaxioms.stream().mapToInt(a -> NNF.getSubClassOfAxiomSize(a)).max().getAsInt();
	private ArrayList<OWLSubClassOfAxiom> tbox;
	private ArrayList<OWLObjectPropertyAxiom> rbox;
		
	public OWLAxMatcher(ArrayList<OWLSubClassOfAxiom> classAxioms,ArrayList<OWLObjectPropertyAxiom> roleAxioms) {
		tbox = classAxioms;
		rbox = roleAxioms;
	}
	
	public String getOWLAxiomsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OWLAx Axioms:\n");
		for (OWLSubClassOfAxiom s : owlaxioms) {
			sb.append(String.format("Axiom Size: %d\nAxiom: %s\n",NNF.getSubClassOfAxiomSize(s),s.toString()));
		}
		return sb.toString();
	}
	
	public List<OWLSubClassOfAxiom> getOWLAxaxioms() {
		return owlaxioms;
	}
	
	public ArrayList<OWLSubClassOfAxiom> getTBox() {
		return tbox;
	}
	
	public ArrayList<OWLObjectPropertyAxiom> getRBox() {
		return rbox;
	}
}
