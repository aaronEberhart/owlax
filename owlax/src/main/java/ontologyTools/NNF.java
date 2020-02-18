package ontologyTools;

import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

public class NNF  {
	
	private OWLOntology ont;
	private ArrayList<OWLAxiom> axioms;
	private ArrayList<OWLClassAxiom> tbox;
	private ArrayList<OWLPropertyAxiom> rbox;
	
	public NNF(OWLOntology o) throws Exception {
		ont = o;
		axioms = getAxioms(ont);
		rbox = new ArrayList<OWLPropertyAxiom>();
		tbox = new ArrayList<OWLClassAxiom>();
		sortAxiomTypes();
		//Existential mincard = exact card
	}
	
	private void sortAxiomTypes() throws Exception {		
		for (OWLAxiom ax : axioms) {			
			if (ax.getAxiomType().getName().equals("SubPropertyChainOf")) {
				rbox.add((OWLSubPropertyChainOfAxiom)ax);
			}else if (ax.getAxiomType().getName().equals("SubObjectPropertyOf")) {
				rbox.add((OWLSubObjectPropertyOfAxiom)ax);
			}else if (ax.getAxiomType().getName().equals("InverseObjectProperties")) {
				rbox.add((OWLInverseObjectPropertiesAxiom)ax);
			}else if (ax.getAxiomType().getName().equals("SubClassOf")) {
				if (!ax.getNNF().equals(ax)) {
					System.out.println(String.format("%s\n%s\n",ax.toString(),ax.getNNF().toString()));				}
				tbox.add((OWLSubClassOfAxiom)ax.getNNF());
			}else {
				throw new Exception("Axiom not handeled:\n\t"+ax.toString());
			}
		}
	}
	
	private boolean correctType(String type) {
		return !(type.equals("AnnotationAssertion") || type.equals("Declaration"));
	}
	
	private ArrayList<OWLAxiom> getAxioms(OWLOntology ont){
		return ont.axioms().filter(a -> correctType(a.getAxiomType().getName())).collect(Collectors.toCollection(ArrayList<OWLAxiom>::new));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (OWLClassAxiom s : tbox) {
			sb.append(String.format("%s\n",s.toString()));
		}
		for (OWLPropertyAxiom s : rbox) {
			sb.append(String.format("%s\n",s.toString()));
		}
		return sb.toString();
	}

	public ArrayList<OWLPropertyAxiom> getRBox() {
		return rbox;
	}
	
	public ArrayList<OWLClassAxiom> getTBox() {
		return tbox;
	}
	
	public ArrayList<OWLAxiom> getAxioms() {
		return axioms;
	}
}
