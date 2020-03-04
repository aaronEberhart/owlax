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
		// ODP - I fogret the website cogan knows 
		
		OWLAxEvaluation miscEvaluation = runEvalOnDir(new File("OWL/"));
		
		System.out.println(miscEvaluation);
		
		OWLAxEvaluation odpEvaluation = runEvalOnDir(new File("OWL/ODP"));
		
		System.out.println(odpEvaluation);
	}	
	
	/**
	 * run Evaluation once on all files in a directory
	 * 
	 * @param dir File
	 * @return OWLAxEvaluation
	 * @throws Exception
	 */
	public static OWLAxEvaluation runEvalOnDir(File dir) throws Exception {
		File[] files = dir.isFile() ? new File[]{dir} : dir.listFiles(a -> a.isFile());
		ArrayList<ArrayList<HashMap<String,Integer>>> results = new ArrayList<ArrayList<HashMap<String,Integer>>>();
		
		for (File owlfile : files) {

			try{
				OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
				
				results.add(new OWLAxMatcher(new NormalizeAndSortAxioms(ontology)).getMatches());
				
			}catch(UnloadableImportException e) {System.err.println(String.format("Import Error for File: %s",owlfile));}
		}
		
		return new OWLAxEvaluation(results);
	}
	
}
