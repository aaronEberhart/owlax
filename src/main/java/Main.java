
import java.io.*;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;

import ontologyTools.*;

public class Main {
	
	public static void main(String[] args) throws Exception {		
		
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("OWL/enslavedv2.owl"));
		
		OWLAxMatcher matcher = new OWLAxMatcher(new NNF(ontology));
		
		System.out.println(matcher.toString());
		System.out.println(matcher.getOWLAxAxiomsString());
	}
	
	
	
}
