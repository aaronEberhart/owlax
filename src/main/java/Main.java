
import java.io.*;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;

import ontologyTools.*;

public class Main {
	
	public static void main(String[] args) throws Exception {		
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();	
		OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("OWL/enslavedv2.owl"));
		
		NNF nnf = new NNF(ont);
		System.out.println(nnf.toString());
		
		OWLAxMatcher mat = new OWLAxMatcher(nnf.getTBox(),nnf.getRBox());
		System.out.println(mat.getOWLAxiomsString());
	}
	
	
	
}
