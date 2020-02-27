package main;

import java.io.*;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;

import ontologyTools.*;

/**
 * Test Class for OWLAx evaluation program
 * 
 * @author DaSe Lab
 */
public class Main {
	
	public static void main(String[] args) throws Exception {		
		
		File owlfile = new File("OWL/enslavedv2.owl");
		
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
		
		OWLAxMatcher matcher = new OWLAxMatcher(new NormalizeAndSortAxioms(ontology));
		
		System.out.println(matcher.getOWLAxAxiomsString());
		System.out.println(matcher.toString());
		
	}
	
	
	
}
