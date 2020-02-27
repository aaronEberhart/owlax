package main;

import java.io.*;
import java.util.*;

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
		
		List<HashMap<String,Integer>> results = new ArrayList<HashMap<String,Integer>>();
		List<String> owlpaths = Arrays.asList("OWL/enslavedv2.owl");
		
		for (String owlpath : owlpaths) {
			
			File owlfile = new File(owlpath);
			
			OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
			
			OWLAxMatcher matcher = new OWLAxMatcher(new NormalizeAndSortAxioms(ontology));
			
			results.add(matcher.getMatches());
		}
		
		HashMap<String,Integer> result = results.stream().reduce();
		
	}	
}
