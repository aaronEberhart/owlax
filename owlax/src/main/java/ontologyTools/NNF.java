package ontologyTools;

import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import uk.ac.manchester.cs.owl.owlapi.*;

public class NNF  {
	
	private static final int maxSize = 3;
	private OWLOntology ont;
	private ArrayList<OWLAxiom> axioms;
	private ArrayList<OWLSubClassOfAxiom> tbox;
	private ArrayList<OWLSubClassOfAxiom> complex;
	private ArrayList<OWLObjectPropertyAxiom> rbox;
	
	public NNF(OWLOntology o) throws Exception {
		ont = o;
		axioms = getAxioms(ont);
		rbox = new ArrayList<OWLObjectPropertyAxiom>();
		tbox = new ArrayList<OWLSubClassOfAxiom>();
		complex = new ArrayList<OWLSubClassOfAxiom>();
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
				parseSubClassOfAxiom((OWLSubClassOfAxiom)ax);
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
	private void parseSubClassOfAxiom(OWLSubClassOfAxiom ax) throws Exception {
		
		//see how big it is
		int size = getSubClassOfAxiomSize(ax);
		
		//is it the right size, but not nnf?
		if (!ax.getNNF().equals(ax) && size <= maxSize) {

			//get the antecedent and consequent
			OWLClassExpression sup = ((OWLSubClassOfAxiom)ax).getSuperClass();
			OWLClassExpression sub = ((OWLSubClassOfAxiom)ax).getSubClass();
			
			//is it an object exact cardinality?
			if (sup.getClassExpressionType().getName().equals("ObjectExactCardinality")) {				
				parseObjectExactCardinality(ax,sub,sup);				
			//is it a data exact cardinality?
			}else if (sup.getClassExpressionType().getName().equals("DataExactCardinality")) {				
				parseDataExactCardinality(ax,sub,sup);	
			}			
			//uh oh
			else{				
				throw new Exception(String.format("\nUNHANDLED AXIOM:\n%s\nNNF:\n%s\n",ax.toString(),ax.getNNF().toString()));	
			}
		}
		//too big for now
		else if (size > maxSize) {
			complex.add(ax);
		}	
		//it was nnf woohoo
		else {
			tbox.add(ax);
		}
	}
	
	/**
	 * gets the size of a subclass axiom
	 */
	public static int getSubClassOfAxiomSize(OWLSubClassOfAxiom ax) {
		return getSubClassOfAxiomSize(((OWLSubClassOfAxiom)ax).getSubClass(),((OWLSubClassOfAxiom)ax).getSuperClass());
	}
	
	/**
	 * Gets the size of the subclass axiom by adding the sizes of its components
	 */
	private static int getSubClassOfAxiomSize(OWLClassExpression sub,OWLClassExpression sup) {
		return getClassExpressionSize(sub) + getClassExpressionSize(sup);
	}
	
	/**
	 * gets the size of an owl expression
	 */
	private static int getClassExpressionSize(OWLClassExpression ex) {
		String type = ex.getClassExpressionType().getName();
		// class = 1
		if (type.equals("Class")) {
			return 1;
		//negation = 0
		} else if (type.equals("ObjectComplementOf")) {
			return getClassExpressionSize(ex.getComplementNNF());
		// quantifier = | class expression | + 1
		}else if (type.equals("ObjectSomeValuesFrom")) {
			return getClassExpressionSize(((OWLObjectSomeValuesFrom)ex).getFiller()) + 1;
		}else if (type.equals("ObjectAllValuesFrom")) {
			return getClassExpressionSize(((OWLObjectAllValuesFrom)ex).getFiller()) + 1;
		}else if (type.equals("ObjectMaxCardinality")) {
			return getClassExpressionSize(((OWLObjectMaxCardinality)ex).getFiller()) + 1;
		}else if (type.equals("ObjectMinCardinality")) {
			return getClassExpressionSize(((OWLObjectMinCardinality)ex).getFiller()) + 1;
		}else if (type.equals("ObjectExactCardinality")) {
				return getClassExpressionSize(((OWLObjectExactCardinality)ex).getFiller()) + 1;
		//data = same as quantifier just not nested
		}else if (type.equals("DataSomeValuesFrom") || type.equals("DataAllValuesFrom") || type.equals("DataMaxCardinality") || type.equals("DataMinCardinality") || type.equals("DataExactCardinality")) {
			return 2;
		}else if (type.equals("ObjectHasSelf")) {
			return 2;
		// conjunction = sum of conjuncts
		}else if (type.equals("ObjectIntersectionOf")) {
			return ((OWLObjectIntersectionOf)ex).conjunctSet().mapToInt(a -> getClassExpressionSize(a)).sum();
		// disjunction = sum of disjuncts
		}else if (type.equals("ObjectUnionOf")) {
			return ((OWLObjectUnionOf)ex).disjunctSet().mapToInt(a -> getClassExpressionSize(a)).sum();
		}
		return -1;
	}
	
	/**
	 * gets the size of a property axiom
	 */
	public static int getObjectPropertyAxiomSize(OWLObjectPropertyAxiom ax) {
		String type = ax.getAxiomType().getName();
		if (type.equals("SubPropertyChainOf")) {
			return ((OWLSubPropertyChainOfAxiom)ax).getPropertyChain().size() + 1;
		}else if (type.equals("SubObjectPropertyOf")) {
			return 2;
		}else if (type.equals("InverseObjectProperties")) {
			return 2;
		}
		return -1;
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
		OWLSubClassOfAxiom ax2 = new OWLSubClassOfAxiomImpl(sub,new OWLDataSomeValuesFromImpl(props.get(0), dat.get(0)), nots);
		
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TBox:\n");
		for (OWLSubClassOfAxiom s : tbox) {
			sb.append(String.format("Axiom Size: %d \nAxiom: %s\n",getSubClassOfAxiomSize(s),s.toString()));
		}
		sb.append("\nComplex Class Axioms:\n");
		for (OWLSubClassOfAxiom s : complex) {
			sb.append(String.format("Axiom Size: %d\nAxiom: %s\n",getSubClassOfAxiomSize(s),s.toString()));
		}
		sb.append("\nRBox:\n");
		for (OWLObjectPropertyAxiom s : rbox) {
			sb.append(String.format("Axiom Size: %d\nAxiom: %s\n",getObjectPropertyAxiomSize(s),s.toString()));
		}
		return sb.toString();
	}

	public ArrayList<OWLObjectPropertyAxiom> getRBox() {
		return rbox;
	}
	
	public ArrayList<OWLSubClassOfAxiom> getTBox() {
		return tbox;
	}
	
	public ArrayList<OWLAxiom> getAxioms() {
		return axioms;
	}
}
