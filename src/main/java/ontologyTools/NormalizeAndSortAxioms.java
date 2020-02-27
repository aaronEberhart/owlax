package ontologyTools;

import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import uk.ac.manchester.cs.owl.owlapi.*;

/**
 * Normalizes and sorts the axioms in an ontology so that 
 * they can be evaluated for OWLAx coverage
 * 
 * @author Aaron Eberhart
 * @author DaSe Lab
 *
 */
public class NormalizeAndSortAxioms  {
	
	private ArrayList<OWLSubClassOfAxiom> classAxioms;
	private ArrayList<OWLSubClassOfAxiom> complexClassAxioms;
	private ArrayList<OWLObjectPropertyAxiom> roleAxioms;
	
	/**
	 * Gets axioms from the ontology and then sorts them
	 * 
	 * @param ontology OWLOntology
	 * @throws Exception
	 */
	public NormalizeAndSortAxioms(OWLOntology ontology) throws Exception {
		
		//initialize everything
		roleAxioms = new ArrayList<OWLObjectPropertyAxiom>();
		classAxioms = new ArrayList<OWLSubClassOfAxiom>();
		complexClassAxioms = new ArrayList<OWLSubClassOfAxiom>();

		//sort the axioms from the ontology
		sortAxiomsByType(getAxioms(ontology));
	}	

	/**
	 * Gets all axioms from the ontology, ignoring annotations and declarations
	 */
	private ArrayList<OWLAxiom> getAxioms(OWLOntology ontology){
		return ontology.axioms().filter(a -> correctType(a.getAxiomType().getName())).collect(Collectors.toCollection(ArrayList<OWLAxiom>::new));
	}
	
	/**
	 * False if the type string of an axiom is Annotation or Declaration, True otherwise
	 */
	private boolean correctType(String type) {
		return !(type.equals("AnnotationAssertion") || type.equals("Declaration"));
	}
		
	/**
	 * Sorts all axioms in ontology into a TBox and RBox, also diverts complex complex class expressions.
	 */
	private void sortAxiomsByType(ArrayList<OWLAxiom> axioms) throws Exception {		
				
		//look through all the axioms
		for (OWLAxiom axiom: axioms) {	
			
			//check what type they are
			String type = axiom.getAxiomType().getName();
			
			// Role chain
			if (type.equals("SubPropertyChainOf")) {
				roleAxioms.add((OWLSubPropertyChainOfAxiom)axiom);
			// Role Inclusion
			}else if (type.equals("SubObjectPropertyOf")) {
				roleAxioms.add((OWLSubObjectPropertyOfAxiom)axiom);
			// Inverse Role
			}else if (type.equals("InverseObjectProperties")) {
				roleAxioms.add((OWLInverseObjectPropertiesAxiom)axiom);
			// Subclass
			}else if (type.equals("SubClassOf")) {
				//these are nested so parse them separately
				parseSubClassOfAxiom((OWLSubClassOfAxiom)axiom);
			// oops forgot one
			}else {
				throw new Exception("Axiom not handeled:\n\t"+axiom.toString());
			}
		}
	}
	
	/**
	 * Sorts TBox statements based on their NNF
	 */
	private void parseSubClassOfAxiom(OWLSubClassOfAxiom axiom) throws Exception {
		
		//see how big it is
		int size = getSubClassOfAxiomSize(axiom);
		
		//is it the right size, but not nnf?
		if (!axiom.getNNF().equals(axiom) && size <= OWLAxMatcher.maxSize) {

			//get the antecedent and consequent
			OWLClassExpression superClass = ((OWLSubClassOfAxiom)axiom).getSuperClass();
			OWLClassExpression subClass = ((OWLSubClassOfAxiom)axiom).getSubClass();
			
			//is it an object exact cardinality?
			if (superClass.getClassExpressionType().getName().equals("ObjectExactCardinality")) {				
				parseObjectExactCardinality(axiom,subClass,superClass);				
			//is it a data exact cardinality?
			}else if (superClass.getClassExpressionType().getName().equals("DataExactCardinality")) {				
				parseDataExactCardinality(axiom,subClass,superClass);	
			}			
			//uh oh
			else{				
				throw new Exception(String.format("\nUNHANDLED AXIOM:\n%s\nNNF:\n%s\n",axiom.toString(),axiom.getNNF().toString()));	
			}
		}
		// is it too big for now?
		else if (size > OWLAxMatcher.maxSize) {
			complexClassAxioms.add(axiom);
		}	
		//it was nnf and the right size! woohoo!
		else {
			classAxioms.add(axiom);
		}
	}
	
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseObjectExactCardinality(OWLSubClassOfAxiom axiom,OWLClassExpression subClass,OWLClassExpression superClass) throws Exception {
		
		//get stuff from old axiom
		superClass = ((OWLObjectExactCardinalityImpl)superClass);		
		List<OWLObjectProperty> properties = superClass.objectPropertiesInSignature().collect(Collectors.toList());
		List<OWLClass> classes = superClass.classesInSignature().collect(Collectors.toList());
		List<OWLAnnotation> annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));
		
		//check if axiom is the right size
		if(properties.size() > 1) {
			throw new Exception("Too many properties in consequent of \n"+axiom.toString());
		}else if(classes.size() > 1) {
			throw new Exception("Too many classes in consequent of \n"+axiom.toString());
		}
		
		//split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(subClass,new OWLObjectMaxCardinalityImpl(properties.get(0), 1, classes.get(0)), annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(subClass,new OWLObjectSomeValuesFromImpl(properties.get(0), classes.get(0)), annotations);
		
		//add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);		
	}
	
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseDataExactCardinality(OWLSubClassOfAxiom axiom,OWLClassExpression subClass,OWLClassExpression superClass) throws Exception {
		
		//get stuff from old axiom
		superClass = ((OWLDataExactCardinalityImpl)superClass);		
		List<OWLDataProperty> properties = superClass.dataPropertiesInSignature().collect(Collectors.toList());
		List<OWLDatatype> datatypes = superClass.datatypesInSignature().collect(Collectors.toList());
		List<OWLAnnotation> annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));
		
		//check if axiom is the right size
		if(properties.size() > 1) {
			throw new Exception("Too many properties in consequent of \n"+axiom.toString());
		}else if(datatypes.size() > 1) {
			throw new Exception("Too many datatypes in consequent of \n"+axiom.toString());
		}
		
		//split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(subClass,new OWLDataMaxCardinalityImpl(properties.get(0), 1, datatypes.get(0)), annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(subClass,new OWLDataSomeValuesFromImpl(properties.get(0), datatypes.get(0)), annotations);
		
		//add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);		
	}
	
	/**
	 * Gets the size of a subclass axiom
	 * 
	 * @param axiom OWLSubClassOfAxiom
	 */
	public static int getSubClassOfAxiomSize(OWLSubClassOfAxiom axiom) {
		return getSubClassOfAxiomSize(((OWLSubClassOfAxiom)axiom).getSubClass(),((OWLSubClassOfAxiom)axiom).getSuperClass());
	}
	
	/**
	 * Gets the size of the subclass axiom by adding the sizes of its components
	 */
	private static int getSubClassOfAxiomSize(OWLClassExpression subClass,OWLClassExpression superClass) {
		return getClassExpressionSize(subClass) + getClassExpressionSize(superClass);
	}
	
	/**
	 * Gets the size of an owl expression
	 */
	private static int getClassExpressionSize(OWLClassExpression expression) {
		
		//look at its type
		String type = expression.getClassExpressionType().getName();
		
		// class = 1
		if (type.equals("Class")) {
			return 1;
		// negation = 0
		} else if (type.equals("ObjectComplementOf")) {
			return getClassExpressionSize(expression.getComplementNNF());
		// quantifier = | class expression | + 1
		}else if (type.equals("ObjectSomeValuesFrom")) {
			return getClassExpressionSize(((OWLObjectSomeValuesFrom)expression).getFiller()) + 1;
		}else if (type.equals("ObjectAllValuesFrom")) {
			return getClassExpressionSize(((OWLObjectAllValuesFrom)expression).getFiller()) + 1;
		}else if (type.equals("ObjectMaxCardinality")) {
			return getClassExpressionSize(((OWLObjectMaxCardinality)expression).getFiller()) + 1;
		}else if (type.equals("ObjectMinCardinality")) {
			return getClassExpressionSize(((OWLObjectMinCardinality)expression).getFiller()) + 1;
		}else if (type.equals("ObjectExactCardinality")) {
			return getClassExpressionSize(((OWLObjectExactCardinality)expression).getFiller()) + 1;
		// data = same as quantifier just not nested
		}else if (type.equals("DataSomeValuesFrom") || type.equals("DataAllValuesFrom") || type.equals("DataMaxCardinality") || type.equals("DataMinCardinality") || type.equals("DataExactCardinality")) {
			return 2;
		// self = always exactly one thing
		}else if (type.equals("ObjectHasSelf")) {
			return 1;
		// conjunction = sum of conjuncts
		}else if (type.equals("ObjectIntersectionOf")) {
			return ((OWLObjectIntersectionOf)expression).conjunctSet().mapToInt(a -> getClassExpressionSize(a)).sum();
		// disjunction = sum of disjuncts
		}else if (type.equals("ObjectUnionOf")) {
			return ((OWLObjectUnionOf)expression).disjunctSet().mapToInt(a -> getClassExpressionSize(a)).sum();
		}
		// don't want to throw an exception but this should be obviously wrong
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Gets the size of a property axiom
	 * 
	 * @param axiom OWLObjectPropertyAxiom
	 */
	public static int getObjectPropertyAxiomSize(OWLObjectPropertyAxiom axiom) {
		
		// look at its type
		String type = axiom.getAxiomType().getName();
		
		// chain = | chain | + 1
		if (type.equals("SubPropertyChainOf")) {
			return ((OWLSubPropertyChainOfAxiom)axiom).getPropertyChain().size() + 1;
		// property = 2 (no chain)
		}else if (type.equals("SubObjectPropertyOf")) {
			return 2;
		// inverse = 2 (same as property)
		}else if (type.equals("InverseObjectProperties")) {
			return 2;
		}
		//don't want to throw an exception but this should be obviously wrong
		return Integer.MAX_VALUE;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Class Axioms:\n");
		for (OWLSubClassOfAxiom s : classAxioms) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),getSubClassOfAxiomSize(s)));
		}
		sb.append("\nComplex Class Axioms:\n");
		for (OWLSubClassOfAxiom s : complexClassAxioms) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),getSubClassOfAxiomSize(s)));
		}
		sb.append("\nRole Axioms:\n");
		for (OWLObjectPropertyAxiom s : roleAxioms) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),getObjectPropertyAxiomSize(s)));
		}
		return sb.toString();
	}

	public ArrayList<OWLObjectPropertyAxiom> getRBox() {
		return roleAxioms;
	}
	
	public ArrayList<OWLSubClassOfAxiom> getTBox() {
		return classAxioms;
	}
	
	public ArrayList<OWLSubClassOfAxiom> getComplexClassAxioms() {
		return complexClassAxioms;
	}
	
}
