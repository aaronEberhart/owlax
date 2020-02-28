package main;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;

import evaluation.OWLAxEvaluation;
import ontologyTools.*;

/**
 * Test Class for OWLAx evaluation program
 * 
 * @author DaSe Lab
 */
public class Main {
	
	public static void main(String[] args) throws Exception {		
		
<<<<<<< HEAD
		File[] owlfiles = new File("OWL/").listFiles();
		ArrayList<HashMap<String,Integer>> resultsList = new ArrayList<HashMap<String,Integer>>();		
=======
		ArrayList<HashMap<String,Integer>> resultsList = new ArrayList<HashMap<String,Integer>>();
		List<String> owlpaths = Arrays.asList("OWL/enslavedv2.owl");
>>>>>>> bc4caa800a1f3ebabdb8732c2de34be375bd02b8
		
		for (File owlfile : owlfiles) {
			
			OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
			
			OWLAxMatcher matcher = new OWLAxMatcher(new NormalizeAndSortAxioms(ontology));			
			
			resultsList.add(matcher.getMatches());
		}
		
		OWLAxEvaluation evaluation = new OWLAxEvaluation(resultsList);
<<<<<<< HEAD
=======
		
		System.out.println(evaluation.getAverageResult());
>>>>>>> bc4caa800a1f3ebabdb8732c2de34be375bd02b8
		
		for (HashMap<String,Integer> result : evaluation.getAllResults()) {
			System.out.println(result);
		}
		System.out.println(evaluation.getAverageResult());
	}	
	
}
