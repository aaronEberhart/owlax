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
	
	private void parseClassAxiom(OWLSubClassOfAxiom ax) throws Exception {
		
		//is it not in nnf?
		if (!ax.getNNF().equals(ax)) {
			
			OWLClassExpression sup = ((OWLSubClassOfAxiom)ax).getSuperClass();
			OWLClassExpression sub = ((OWLSubClassOfAxiom)ax).getSubClass();
			
			//is it a role cardinality?
			if (sup.getClassExpressionType().getName().equals("ObjectExactCardinality")) {
				
				sup = ((OWLObjectExactCardinalityImpl)sup);
				
				List<OWLObjectProperty> props = sup.objectPropertiesInSignature().collect(Collectors.toList());
				List<OWLClass> cls = sup.classesInSignature().collect(Collectors.toList());
				List<OWLAnnotation> nots = ax.annotations().collect(Collectors.toCollection(ArrayList::new));
				
				if(props.size() > 1) {
					throw new Exception("Too many properties in consequent of \n"+ax.toString());
				}else if(cls.size() > 1) {
					throw new Exception("Too many classes in consequent of \n"+ax.toString());
				}
				
				ax = new OWLSubClassOfAxiomImpl(sub,new OWLObjectMaxCardinalityImpl(props.get(0), 1, cls.get(0)), nots);
				OWLSubClassOfAxiom ax2 = new OWLSubClassOfAxiomImpl(sub,new OWLObjectSomeValuesFromImpl(props.get(0), cls.get(0)), nots);
				
				tbox.add(ax);
				tbox.add(ax2);
				
			//is it a data cardinality?
			}else if (sup.getClassExpressionType().getName().equals("DataExactCardinality")) {
				
				sup = ((OWLDataExactCardinalityImpl)sup);
				
				List<OWLDataProperty> props = sup.dataPropertiesInSignature().collect(Collectors.toList());
				List<OWLDatatype> dat = sup.datatypesInSignature().collect(Collectors.toList());
				List<OWLAnnotation> nots = ax.annotations().collect(Collectors.toCollection(ArrayList::new));
				
				if(props.size() > 1) {
					throw new Exception("Too many properties in consequent of \n"+ax.toString());
				}else if(dat.size() > 1) {
					throw new Exception("Too many datatypes in consequent of \n"+ax.toString());
				}
				
				ax = new OWLSubClassOfAxiomImpl(sub,new OWLDataMaxCardinalityImpl(props.get(0), 1, dat.get(0)), nots);
				OWLSubClassOfAxiom ax2 = new OWLSubClassOfAxiomImpl(sub,new OWLDataMinCardinalityImpl(props.get(0), 1, dat.get(0)), nots);
				
				tbox.add(ax);
				tbox.add(ax2);
				
			//uh oh
			}else{
				System.err.println(String.format("\nHARD AXIOM:\n%s\nNNF:\n%s\n",ax.toString(),ax.getNNF().toString()));	
			}
			
		//it was nnf woohoo
		}else {
			tbox.add((OWLSubClassOfAxiom)ax);
		}
	}
	
	private boolean correctType(String type) {
		return !(type.equals("AnnotationAssertion") || type.equals("Declaration"));
	}
	
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
