
import java.io.*;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;

import ontologyTools.*;

public class Main {
	
	public static void main(String[] args) throws Exception {		
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("OWL/enslavedv2.owl"));
		
		NNF nnf = new NNF(ontology);
		System.out.println(nnf.toString());
		
		OWLAxMatcher matcher = new OWLAxMatcher(nnf);
		System.out.println(matcher.getOWLAxiomsString());
	}
	
	
	
}
