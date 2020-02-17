package owlax;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

@SuppressWarnings("deprecation")
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
	public static void main(String[] args) {		
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();	
			OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("enslavedv2.owl"));
			System.out.println(ont.getAxioms().size());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
}
