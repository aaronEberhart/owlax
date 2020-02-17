package owlax;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

public class OWLAx<R> {
	
	/**
	 * AXIOMS
	 * 
	 *  A ⊑ B
	 *  A ⊓ B ⊑ ⊥
	 *  ∃R.⊤ ⊑ A
	 *  ∃R.B ⊑ A
	 *  ⊤ ⊑ ∀R.B
	 *  A ⊑ ∀R.B
	 *  A ⊑ ∃R.B
	 *  A ⊑ ∃R-.B
	 *  ⊤ ⊑ <=1R.T
	 *  ⊤ ⊑ <=1R.B
	 *  A ⊑ <=1R.T
	 *  A ⊑ <=1R.B
	 *  ⊤ ⊑ <=1R-.T
	 *  ⊤ ⊑ <=1R-.B
	 *  A ⊑ <=1R-.T
	 *  A ⊑ <=1R-.B
	 *  A ⊑ >=0R.B
	 *  
	 **/	
	public static void main(String[] args) throws Exception {		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();	
		OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("enslavedv2.owl"));
		String type;
		
		for (OWLAxiom ax : getAxioms(ont)) {
			
			type = ax.getAxiomType().getName();
			
			if (correctType(type)) {
				System.out.println(String.format("%s\n",ax.toString()));
			}
			
		}
		
	}
	
	public static boolean correctType(String type) {
		return !(type.equals("AnnotationAssertion") || type.equals("Declaration"));
	}
	
	public static ArrayList<OWLAxiom> getAxioms(OWLOntology ont){
		return ont.axioms().collect(Collectors.toCollection(ArrayList<OWLAxiom>::new));
	}
	
}
