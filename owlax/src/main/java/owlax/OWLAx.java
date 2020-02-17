package owlax;

import java.io.*;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;

public class OWLAx {
	
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
		
		NNF nnf = new NNF(ont);
		System.out.println(nnf.toString());
	}
	
	
	
}
