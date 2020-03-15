package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.UnloadableImportException;

import evaluation.OWLAxEvaluation;
import ontologyTools.NormalizeAndSortAxioms;
import ontologyTools.OWLAxMatcher;

/**
 * Test Class for OWLAx evaluation program
 * 
 * @author Sulogna Chowdhury, Aaron Eberhart
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
		
		//misc files by themselves (sizes very different)
		for (File file : new File("OWL/").listFiles(a -> a.isFile())) {
			runEval(file);
		}
		
		// misc files together
		runEval(new File("OWL/"));
		
		//ODPs
		runEval(new File("OWL/ODP"));
		
		System.out.println("DONE");
	}	
	
	/**
	 * run Evaluation once on all files in a directory or a single file.
	 * Will write results to text file
	 * 
	 * @param dir File
	 * @return OWLAxEvaluation
	 */
	public static OWLAxEvaluation runEval(File dir) {
		//check if it's a file of a dir of files
		File[] files = dir.isFile() ? new File[]{dir} : dir.listFiles(a -> a.isFile());
		ArrayList<ArrayList<HashMap<String,Integer>>> results = new ArrayList<ArrayList<HashMap<String,Integer>>>();
		
		System.out.println("Starting evaluation of "+dir.getName());
		
		//look at all the ontology files
		for (File owlfile : files) {
			try{
				OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
				
				results.add(new OWLAxMatcher(new NormalizeAndSortAxioms(ontology)).getMatches());
				
			}catch(UnloadableImportException e) {System.err.println(String.format("Import Error for File: %s",owlfile));}
			catch(Exception e) {System.err.println(e);}
		}
		
		//do the evaluation
		OWLAxEvaluation eval = new OWLAxEvaluation(results);
		
		//write to file
		try {
			Files.writeString(Paths.get(String.format("%s.txt",dir.getName())),eval.toString());
		}catch (IOException e) {System.err.println(e);}
		
		return eval;
	}
	
}
