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
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2Profile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;

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

		ArrayList<ArrayList<HashMap<String,Double>>> allResults = new ArrayList<ArrayList<HashMap<String,Double>>>();
		
		// OWL File Sources
		//
		// https://bioportal.bioontology.org/ontologies - GO GFO
		// https://docs.enslaved.org/ontology/
		// wherever GMO and GBO are from
		// ODP - I forget the website cogan knows 
		
		File out = new File("output/");
		
		if (!out.exists()) {out.mkdir();}
		
		//hydrography benchmarks
		allResults.addAll(runEval(new File("OWL/hydrographyBenchmarks")));
		
		//anatomy benchmarks
		allResults.addAll(runEval(new File("OWL/anatomyBenchmarks")));
				
		//conference benchmarks
		allResults.addAll(runEval(new File("OWL/conferenceBenchmarks")));

		//ODPs
		allResults.addAll(runEval(new File("OWL/ODPs")));
		
		//ontobee
		allResults.addAll(runEval(new File("OWL/Ontobee")));
		
		//misc files by themselves (sizes very different)
		//for (File file : new File("OWL/").listFiles(a -> a.isFile())) {
			//runEval(file);
		//}
		
		// misc files together
		allResults.addAll(runEval(new File("OWL/")));
		
		OWLAxEvaluation eval = new OWLAxEvaluation(allResults);
		
		//write to file
		try {
			Files.writeString(Paths.get("output/allResults.csv"),eval.toCSV());
		}catch (IOException e) {System.err.println(e);}
		
		System.out.println("DONE");
	}	
	
	/**
	 * run Evaluation once on all files in a directory or a single file.
	 * Will write results to text file
	 * 
	 * @param dir File
	 * @return OWLAxEvaluation
	 */
	public static ArrayList<ArrayList<HashMap<String,Double>>> runEval(File dir) {
		//check if it's a file of a dir of files
		File[] files = dir.isFile() ? new File[]{dir} : dir.listFiles(a -> a.isFile());
		ArrayList<ArrayList<HashMap<String,Double>>> results = new ArrayList<ArrayList<HashMap<String,Double>>>();
		//System.out.println("Starting evaluation of "+dir.getName());
		
		//look at all the ontology files
		for (File owlfile : files) {
			try{
				OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
				
				results.add(new OWLAxMatcher(new NormalizeAndSortAxioms(ontology)).getMatches());
				
				// Available profiles: DL, EL, QL, RL, OWL2 (Full)
				boolean[] profiles = {new OWL2DLProfile().checkOntology(ontology).isInProfile(),new OWL2ELProfile().checkOntology(ontology).isInProfile(),new OWL2QLProfile().checkOntology(ontology).isInProfile(),new OWL2RLProfile().checkOntology(ontology).isInProfile(),new OWL2Profile().checkOntology(ontology).isInProfile()};
				String[] profileNames = {"DL","EL","QL","RL","OWL2 (Full)"};
				
				String out = "";
				
				for (int i = 0; i < profiles.length; i++) {
					if (profiles[i]) {
						out += ",X";
					}else {
						out+= ",";
					}
				}
				
				System.out.println(owlfile.getName() + out);
				
				
				
			}catch(UnloadableImportException e) {}//System.err.println(String.format("Import Error for File: %s",owlfile));}
			catch(Exception e) {System.err.println(owlfile.getName() + ",,,,,");}
		}
		
		if (!results.isEmpty()) {
			
			//do the evaluation
			OWLAxEvaluation eval = new OWLAxEvaluation(results);
			
			//write to file
			try {
				Files.writeString(Paths.get(String.format("output/%s.csv",dir.getName())),eval.toCSV());
			}catch (IOException e) {System.err.println(e);}
			
			return results;
		}
		else { return results; }
	}
	
}
