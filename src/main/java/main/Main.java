package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2Profile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;

import evaluation.OWLAxEvaluation;
import ontologyTools.NormalizeAndSortAxioms;
import ontologyTools.OWLAxMatcher;

/**
 * Test Class for OWLAx evaluation program
 * 
 * @author 
 * @author 
 */
public class Main {
	
	private static ArrayList<String> iris;
	
	static void doNothing(Object o) { }
	
	public static String inProfileString(boolean inProfile) {
		return inProfile ? ",X" : ",";
	}
	
	public static boolean usedIRIBefore(OWLOntology ontology) {
		for (String iri : iris) {
			if (iri.equals(ontology.getOntologyID().getOntologyIRI().get().toString())) { return true; }
		}
		return false;
	}
	
	/**
	 * run Evaluation once on all files in a directory or a single file.
	 * Will write results to text file
	 * 
	 * @param dir File
	 * @return OWLAxEvaluation
	 */
	public static ArrayList<ArrayList<HashMap<String,Double>>> runEval(File dir,int timeout) {
		
		//check if it's a file of a dir of files
		File[] files = dir.isFile() ? new File[]{dir} : dir.listFiles(a -> a.isFile());
		ArrayList<ArrayList<HashMap<String,Double>>> results = new ArrayList<ArrayList<HashMap<String,Double>>>();
		//System.out.println("Starting evaluation of "+dir.getName());
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(String.format("output/%sUnreadable.csv",dir.getName()))));
			BufferedWriter p = new BufferedWriter(new FileWriter(new File(String.format("output/%sProfiles.csv",dir.getName()))));

			p.write(",DL,EL,QL,RL,Full\n");
		
		//look at all the ontology files
		for (File owlfile : files) {
			
			
			
			ExecutorService executor = Executors.newCachedThreadPool();
			Callable<Integer> task = new Callable<Integer>() {
			   public Integer call() throws IOException, OWLOntologyCreationException {
			    	
				   System.out.println(owlfile.getName());
				   
			    	OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlfile);
					
					if (!usedIRIBefore(ontology)){
						
						iris.add(ontology.getOntologyID().getOntologyIRI().get().toString());
						
						results.add(new OWLAxMatcher(new NormalizeAndSortAxioms(ontology)).getMatches());
						
						String profiles = inProfileString(new OWL2DLProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2ELProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2QLProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2RLProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2Profile().checkOntology(ontology).isInProfile());
						
						p.write(owlfile.getName() + profiles + "\n");
					}
					return 0;
			    }
			};
			
			Future<Integer> future = executor.submit(task);
			try {
			   future.get(timeout, TimeUnit.SECONDS); 
			} catch (InterruptedException e) { 
				w.write(owlfile.getName() + "\n");
			} catch (ExecutionException e) { 
				w.write(owlfile.getName() + "\n");
			}catch(TimeoutException e) { 
				w.write(owlfile.getName() + "\n");
			}catch(UnloadableImportException e) {w.write(owlfile.getName() + "\n");
			}catch(Exception e) {p.write(owlfile.getName() + ",,,,,\n");w.write(owlfile.getName() + "\n");
			}finally {future.cancel(true);executor.shutdownNow();}
			
		}
			
		w.close();
		p.close();
		
		} catch (IOException e1) {System.err.println(e1);}
		
		if (!results.isEmpty()) {
			
			//do the evaluation
			OWLAxEvaluation eval = new OWLAxEvaluation(results);
			
			//write to file
			try {
				Files.writeString(Paths.get(String.format("output/%sData.csv",dir.getName())),eval.toCSV());
			}catch (IOException e) {System.err.println(e);}
			
			return results;
		}
		else { return results; }
	}
	
	/*
	 * run Evaluation once on all links read from file.
	 * Will write results to text file
	 * 
	 * @param dir File
	 * @return OWLAxEvaluation
	 */
	public static ArrayList<ArrayList<HashMap<String,Double>>> runEval(String filename) {

		ArrayList<ArrayList<HashMap<String,Double>>> results = new ArrayList<ArrayList<HashMap<String,Double>>>();

		try {
			
			Scanner s = new Scanner(new File(filename));
			ArrayList<String> files = new ArrayList<String>();
			
			while(s.hasNextLine()) {
				files.add(s.nextLine());			
			}
			
			s.close();
			
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(String.format("output/%sUnreadable.csv",filename))));
			BufferedWriter p = new BufferedWriter(new FileWriter(new File(String.format("output/%sProfiles.csv",filename))));

			p.write(",DL,EL,QL,RL,Full\n");			
		
		//look at all the ontology files
		for (String owlfile : files) {
			
			try {
				
				System.out.println(owlfile);
			   
		    	OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create(owlfile));
				
				if (!usedIRIBefore(ontology)){
					
					iris.add(ontology.getOntologyID().getOntologyIRI().get().toString());
					
					results.add(new OWLAxMatcher(new NormalizeAndSortAxioms(ontology)).getMatches());
					
					String profiles = inProfileString(new OWL2DLProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2ELProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2QLProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2RLProfile().checkOntology(ontology).isInProfile())+inProfileString(new OWL2Profile().checkOntology(ontology).isInProfile());
					
					p.write(owlfile + profiles + "\n");
				}
					
			}catch(UnloadableImportException e) {w.write(owlfile + "\n");
			}catch(Exception e) {p.write(owlfile + ",,,,,\n");w.write(owlfile + "\n");}
			
		}
		
		p.close();
		w.close();
		
		} catch (IOException e1) {System.err.println(e1);}
		
		if (!results.isEmpty()) {
			
			//do the evaluation
			OWLAxEvaluation eval = new OWLAxEvaluation(results);
			
			//write to file
			try {
				Files.writeString(Paths.get(String.format("output/%sData.csv",filename)),eval.toCSV());
			}catch (IOException e) {System.err.println(e);}
			
			return results;
		}
		else { return results; }
	}
	
	public static void main(String[] args) throws Exception {	
		
		File out = new File("output/");
		
		if (!out.exists()) {out.mkdir();}
		
		iris = new ArrayList<String>();
		
		//Oxford
		//runEval("oxLinks.txt");
		
		iris = new ArrayList<String>();
		
		ArrayList<ArrayList<HashMap<String,Double>>> allResults = new ArrayList<ArrayList<HashMap<String,Double>>>();
		
		//LOV
		//allResults.addAll(runEval(new File("OWL/LOV"),60));
		
		//hydrography benchmarks
		//allResults.addAll(runEval(new File("OWL/hydrographyBenchmarks"),Integer.MAX_VALUE));
		
		//anatomy benchmarks
		//allResults.addAll(runEval(new File("OWL/anatomyBenchmarks"),Integer.MAX_VALUE));
				
		//conference benchmarks
		//allResults.addAll(runEval(new File("OWL/conferenceBenchmarks"),Integer.MAX_VALUE));
				
		//ODPs
		//allResults.addAll(runEval(new File("OWL/ODPs"),Integer.MAX_VALUE));
		
		//ontobee
		allResults.addAll(runEval(new File("OWL/Ontobee"),Integer.MAX_VALUE));
				
		// misc files together
		allResults.addAll(runEval(new File("OWL/"),Integer.MAX_VALUE));
		
		OWLAxEvaluation eval = new OWLAxEvaluation(allResults);
		
		//write to file
		try {
			Files.writeString(Paths.get("output/allResults.csv"),eval.toCSV());
		}catch (IOException e) {System.err.println(e);}
		
		System.out.println("DONE");
	}	
	
	
}
