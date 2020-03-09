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
		
		OWLAxEvaluation miscEvaluation = runEvalOnDir(new File("OWL/en"));
		
		System.out.println("Owl name:- Enslaved\n" + miscEvaluation + "\n");
		
        OWLAxEvaluation gboEvaluation = runEvalOnDir(new File("OWL/gbo"));
		
		System.out.println("Owl Name:-GBO \n"+gboEvaluation + "\n");
		
        OWLAxEvaluation gmoEvaluation = runEvalOnDir(new File("OWL/gmo"));
		
		System.out.println("Owl name:- GMO\n" + gmoEvaluation +"\n");
		
        OWLAxEvaluation goplusEvaluation = runEvalOnDir(new File("OWL/go_plus"));
		
		System.out.println("Owl name:- Go_plus\n" + goplusEvaluation +"\n");
		
		
		OWLAxEvaluation gfoEvaluation = runEvalOnDir(new File("OWL/gfo"));
		
		System.out.println("Owl name:- GFO\n" + gfoEvaluation+ "\n");
		
        //OWLAxEvaluation owlxmlEvaluation = runEvalOnDir(new File("OWL/owlxml"));
		
		//System.out.println("Owl name:- GFO\n" + owlxmlEvaluation+ "\n");
		
		
       // OWLAxEvaluation lovEvaluation = runEvalOnDir(new File("OWL/lov"));
		
		//System.out.println("Owl name:- LOV\n" + gfoEvaluation+ "\n");
		
        OWLAxEvaluation odpEvaluation = runEvalOnDir(new File("OWL/ODP"));
		
		System.out.println("Owl name:- ODP\n" + odpEvaluation+ "\n");
		
        
		
		
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
