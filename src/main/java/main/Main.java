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
		
		// OWL File Sources
		//
		// https://bioportal.bioontology.org/ontologies - GO GFO
		// https://docs.enslaved.org/ontology/
		// wherever GMO and GBO are from
		File[] owlfiles = new File("OWL/").listFiles();
		ArrayList<ArrayList<HashMap<String,Integer>>> resultsList = new ArrayList<ArrayList<HashMap<String,Integer>>>();
		
		for (File owlfile : owlfiles) {
			
			OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
			
			resultsList.add(new OWLAxMatcher(new NormalizeAndSortAxioms(ontology)).getMatches());
		}
		
		OWLAxEvaluation evaluation = new OWLAxEvaluation(resultsList);
		
		System.out.println("OWLAx Results");
		for (HashMap<String,Integer> result : evaluation.getOWLAxResults()) {
			System.out.println(result);
		}
		System.out.println("\nOntology Compositions");
		for (HashMap<String,Integer> result : evaluation.getOntologyCompositions()) {
			System.out.println(result);
		}

		System.out.println("\nAverage evaluation\n"+evaluation.getAverageOWLAxResult());
		System.out.println("\nAverage ontology composition\n"+evaluation.getAverageOntology());
	}	
	
}
