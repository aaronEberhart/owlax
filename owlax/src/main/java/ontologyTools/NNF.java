package ontologyTools;

import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import uk.ac.manchester.cs.owl.owlapi.*;

public class NNF  {
	
	private OWLOntology ont;
	private ArrayList<OWLAxiom> axioms;
	private ArrayList<OWLClassAxiom> tbox;
	private ArrayList<OWLPropertyAxiom> rbox;
	
	public NNF(OWLOntology o) throws Exception {
		ont = o;
		axioms = getAxioms(ont);
		rbox = new ArrayList<OWLPropertyAxiom>();
		tbox = new ArrayList<OWLClassAxiom>();
		sortAxiomTypes();
	}
	
	/**
	 * Sorts all axioms in ontology into a TBox and RBox.
	 * 
	 * @throws Exception
	 */
	private void sortAxiomTypes() throws Exception {		
		String type;
		for (OWLAxiom ax : axioms) {	
			type = ax.getAxiomType().getName();
			if (type.equals("SubPropertyChainOf")) {
				rbox.add((OWLSubPropertyChainOfAxiom)ax);
			}else if (type.equals("SubObjectPropertyOf")) {
				rbox.add((OWLSubObjectPropertyOfAxiom)ax);
			}else if (type.equals("InverseObjectProperties")) {
				rbox.add((OWLInverseObjectPropertiesAxiom)ax);
			}else if (type.equals("SubClassOf")) {
				parseClassAxiom((OWLSubClassOfAxiom)ax);
			}else {
				throw new Exception("Axiom not handeled:\n\t"+ax.toString());
			}
		}
	}
	
	/**
	 * Sorts TBox statements based on their NNF
	 * 
	 * @param OWLSubClassOfAxiom ax
	 * @throws Exception
	 */
	private void parseClassAxiom(OWLSubClassOfAxiom ax) throws Exception {
		
		//is it already in nnf?
		if (!ax.getNNF().equals(ax)) {

			//get the antecedent and consequent
			OWLClassExpression sup = ((OWLSubClassOfAxiom)ax).getSuperClass();
			OWLClassExpression sub = ((OWLSubClassOfAxiom)ax).getSubClass();
			
			//is it an object cardinality?
			if (sup.getClassExpressionType().getName().equals("ObjectExactCardinality")) {				
				parseObjectExactCardinality(ax,sub,sup);				
			//is it a data cardinality?
			}else if (sup.getClassExpressionType().getName().equals("DataExactCardinality")) {				
				parseDataExactCardinality(ax,sub,sup);				
			//uh oh
			}else{
				throw new Exception(String.format("\nUNHANDLED AXIOM:\n%s\nNNF:\n%s\n",ax.toString(),ax.getNNF().toString()));	
			}
			
		//it was nnf woohoo
		}else {
			tbox.add((OWLSubClassOfAxiom)ax);
		}
	}
	
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 * 
	 * @param OWLSubClassOfAxiom ax
	 * @param OWLClassExpression sub
	 * @param OWLClassExpression sup
	 * @throws Exception
	 */
	private void parseObjectExactCardinality(OWLSubClassOfAxiom ax,OWLClassExpression sub,OWLClassExpression sup) throws Exception {
		
		//get stuff from old axiom
		sup = ((OWLObjectExactCardinalityImpl)sup);		
		List<OWLObjectProperty> props = sup.objectPropertiesInSignature().collect(Collectors.toList());
		List<OWLClass> cls = sup.classesInSignature().collect(Collectors.toList());
		List<OWLAnnotation> nots = ax.annotations().collect(Collectors.toCollection(ArrayList::new));
		
		//check if axiom is the right size
		if(props.size() > 1) {
			throw new Exception("Too many properties in consequent of \n"+ax.toString());
		}else if(cls.size() > 1) {
			throw new Exception("Too many classes in consequent of \n"+ax.toString());
		}
		
		//split into new axioms
		ax = new OWLSubClassOfAxiomImpl(sub,new OWLObjectMaxCardinalityImpl(props.get(0), 1, cls.get(0)), nots);
		OWLSubClassOfAxiom ax2 = new OWLSubClassOfAxiomImpl(sub,new OWLObjectSomeValuesFromImpl(props.get(0), cls.get(0)), nots);
		
		//add to tbox
		tbox.add(ax);
		tbox.add(ax2);
		
	}
	
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 * 
	 * @param OWLSubClassOfAxiom ax
	 * @param OWLClassExpression sub
	 * @param OWLClassExpression sup
	 * @throws Exception
	 */
	private void parseDataExactCardinality(OWLSubClassOfAxiom ax,OWLClassExpression sub,OWLClassExpression sup) throws Exception {
		
		//get stuff from old axiom
		sup = ((OWLDataExactCardinalityImpl)sup);		
		List<OWLDataProperty> props = sup.dataPropertiesInSignature().collect(Collectors.toList());
		List<OWLDatatype> dat = sup.datatypesInSignature().collect(Collectors.toList());
		List<OWLAnnotation> nots = ax.annotations().collect(Collectors.toCollection(ArrayList::new));
		
		//check if axiom is the right size
		if(props.size() > 1) {
			throw new Exception("Too many properties in consequent of \n"+ax.toString());
		}else if(dat.size() > 1) {
			throw new Exception("Too many datatypes in consequent of \n"+ax.toString());
		}
		
		//split into new axioms
		ax = new OWLSubClassOfAxiomImpl(sub,new OWLDataMaxCardinalityImpl(props.get(0), 1, dat.get(0)), nots);
		OWLSubClassOfAxiom ax2 = new OWLSubClassOfAxiomImpl(sub,new OWLDataMinCardinalityImpl(props.get(0), 1, dat.get(0)), nots);
		
		//add to tbox
		tbox.add(ax);
		tbox.add(ax2);
		
	}
	
	/**
	 * False if the type string of an axiom is Annotation or Declaration, True otherwise
	 * 
	 * @param String type
	 * @return boolean
	 */
	private boolean correctType(String type) {
		return !(type.equals("AnnotationAssertion") || type.equals("Declaration"));
	}
	
	/**
	 * Gets all axioms from the ontology, ignoring annotations and declarations
	 * 
	 * @param OWLOntology ont
	 * @return ArrayList<OWLAxiom>
	 */
	private ArrayList<OWLAxiom> getAxioms(OWLOntology ont){
		return ont.axioms().filter(a -> correctType(a.getAxiomType().getName())).collect(Collectors.toCollection(ArrayList<OWLAxiom>::new));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (OWLClassAxiom s : tbox) {
			sb.append(String.format("%s\n",s.toString()));
		}
		for (OWLPropertyAxiom s : rbox) {
			sb.append(String.format("%s\n",s.toString()));
		}
		return sb.toString();
	}

	public ArrayList<OWLPropertyAxiom> getRBox() {
		return rbox;
	}
	
	public ArrayList<OWLClassAxiom> getTBox() {
		return tbox;
	}
	
	public ArrayList<OWLAxiom> getAxioms() {
		return axioms;
	}
}
