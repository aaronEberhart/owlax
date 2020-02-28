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
		
		File[] owlfiles = new File("OWL/").listFiles();
		ArrayList<HashMap<String,Integer>> resultsList = new ArrayList<HashMap<String,Integer>>();
		
		for (File owlfile : owlfiles) {
			
			OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
			
			OWLAxMatcher matcher = new OWLAxMatcher(new NormalizeAndSortAxioms(ontology));			
			
			resultsList.add(matcher.getMatches());
		}
		
		OWLAxEvaluation evaluation = new OWLAxEvaluation(resultsList);
		
		for (HashMap<String,Integer> result : evaluation.getAllResults()) {
			System.out.println(result);
		}
		System.out.println(evaluation.getAverageResult());
	}	
	
}
