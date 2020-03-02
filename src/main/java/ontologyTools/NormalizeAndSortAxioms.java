package ontologyTools;

import java.util.*;
import java.util.stream.*;

import org.eclipse.rdf4j.model.vocabulary.*;
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
	
	private static int ontologyIndex = -1;
	private static final String[] ontologyHashKeys = {"subclass","equivalent classes","disjoint classes","disjoint union","subrole","subdata","equivalent roles","equivalent data","disjoint roles","disjoint data","subrole chain","inverse role","symmetric role","asymmetric role","functional role","functional data","inverse functional role","transitive role","role domain","data domain","role range","data range"};
	private ArrayList<OWLSubClassOfAxiom> classAxioms;
	private ArrayList<OWLSubClassOfAxiom> complexClassAxioms;
	private ArrayList<OWLPropertyAxiom> roleAxioms;
	private HashMap<String,Integer> ontologyComposition;
	
	/**
	 * Gets axioms from the ontology and then sorts them
	 * 
	 * @param ontology OWLOntology
	 * @throws Exception
	 */
	public NormalizeAndSortAxioms(OWLOntology ontology) throws Exception {
		
		//initialize everything
		roleAxioms = new ArrayList<OWLPropertyAxiom>();
		classAxioms = new ArrayList<OWLSubClassOfAxiom>();
		complexClassAxioms = new ArrayList<OWLSubClassOfAxiom>();
		
		//save the quantities of these things for later
		ontologyComposition = new HashMap<String,Integer>(){private static final long serialVersionUID = 3L;{
			put("number of classes",(int)ontology.classesInSignature().count());
			put("number of roles",(int)ontology.objectPropertiesInSignature().count());
			put("number of data properties",(int)ontology.dataPropertiesInSignature().count());
			put(ontology.getFormat().toString(),ontologyIndex);
			put(ontology.getOntologyID().toString(),ontologyIndex--);
			for (String key : ontologyHashKeys){put(key,0);}
		}};
		
		//sort the axioms from the ontology
		getAxioms(ontology).forEach(a -> {try {sortAxiomByType(a.getAxiomWithoutAnnotations());} catch (Exception e) {e.printStackTrace();}});
		
		ontologyComposition.put("simple class axioms", classAxioms.size());
		ontologyComposition.put("complex class axioms", complexClassAxioms.size());
		ontologyComposition.put("role axioms", roleAxioms.size());
	}

	/**
	 * Gets all axioms from the ontology, ignoring annotations, class assertions, and declarations
	 */
	private Stream<OWLAxiom> getAxioms(OWLOntology ontology){
		return ontology.axioms().filter(a -> correctType(a.getAxiomType().getName()));
	}
	
	/**
	 * False if the type string of an axiom is Annotation, class assertion, Different individuals, or Declaration, True otherwise
	 */
	private boolean correctType(String type) {
		return !(type.equals("DifferentIndividuals") || type.equals("Rule") || type.equals("AnnotationAssertion") || type.equals("Declaration") || type.contentEquals("SubAnnotationPropertyOf") || type.equals("ClassAssertion"));
	}
	
	/**
	 * Sorts one axiom by its type
	 */
	private void sortAxiomByType(OWLAxiom axiom) throws Exception {
		
		String type = axiom.getAxiomType().getName();
		
		// Sub Class
		if (type.equals("SubClassOf")) {
			ontologyComposition.replace("subclass", ontologyComposition.get("subclass") + 1);
			//these are nested so parse them separately
			parseSubClassOfAxiom((OWLSubClassOfAxiom)axiom);
		// equivalent classes
		}else if (type.equals("EquivalentClasses")) {
				ontologyComposition.replace("equivalent classes", ontologyComposition.get("equivalent classes") + 1);
				((OWLEquivalentClassesAxiom)axiom).asOWLSubClassOfAxioms().forEach(a -> { try { parseSubClassOfAxiom((OWLSubClassOfAxiom)a); } catch (Exception e) {e.printStackTrace();}});
		// disjoint classes
		}else if (type.equals("DisjointClasses")) {			
			ontologyComposition.replace("disjoint classes", ontologyComposition.get("disjoint classes") + 1);
			((OWLDisjointClassesAxiom)axiom).asOWLSubClassOfAxioms().forEach(a -> {try {parseSubClassOfAxiom(a);} catch (Exception e) {e.printStackTrace();}});
		// disjoint union
		}else if (type.equals("DisjointUnion")) {
			ontologyComposition.replace("disjoint union", ontologyComposition.get("disjoint union") + 1);
			((OWLDisjointUnionAxiom)axiom).getOWLEquivalentClassesAxiom().asOWLSubClassOfAxioms().forEach(a -> { try { parseSubClassOfAxiom((OWLSubClassOfAxiom)a); } catch (Exception e) {e.printStackTrace();}});
			((OWLDisjointUnionAxiom)axiom).getOWLDisjointClassesAxiom().asOWLSubClassOfAxioms().forEach(a -> { try { parseSubClassOfAxiom((OWLSubClassOfAxiom)a); } catch (Exception e) {e.printStackTrace();}});
		// Sub Role
		}else if (type.equals("SubObjectPropertyOf")) {
			ontologyComposition.replace("subrole", ontologyComposition.get("subrole") + 1);
			roleAxioms.add((OWLSubObjectPropertyOfAxiom)axiom);
		// Sub Data
		}else if (type.equals("SubDataPropertyOf")) {
			ontologyComposition.replace("subdata", ontologyComposition.get("subdata") + 1);
			roleAxioms.add((OWLSubDataPropertyOfAxiom)axiom);
		// Equivalent Roles
		}else if (type.equals("EquivalentObjectProperties")) {			
			ontologyComposition.replace("equivalent roles", ontologyComposition.get("equivalent roles") + 1);
			ontologyComposition.replace("subrole", ontologyComposition.get("subrole") - ((OWLEquivalentObjectPropertiesAxiom)axiom).asSubObjectPropertyOfAxioms().size());
			((OWLEquivalentObjectPropertiesAxiom)axiom).asSubObjectPropertyOfAxioms().forEach(a -> {try {sortAxiomByType(a);} catch (Exception e) {e.printStackTrace();}});
		// Equivalent Data
		}else if (type.equals("EquivalentDataProperties")) {
			ontologyComposition.replace("equivalent data", ontologyComposition.get("equivalent data") + 1);
			ontologyComposition.replace("subdata", ontologyComposition.get("subdata") - ((OWLEquivalentDataPropertiesAxiom)axiom).asSubDataPropertyOfAxioms().size());
			((OWLEquivalentDataPropertiesAxiom)axiom).asSubDataPropertyOfAxioms().forEach(a -> {try {sortAxiomByType(a);} catch (Exception e) {e.printStackTrace();}});
		// Disjoint Roles
		}else if (type.equals("DisjointObjectProperties")) {
			ontologyComposition.replace("disjoint roles", ontologyComposition.get("disjoint roles") + 1);
			throw new Exception("Disjoint Properties Normalization Undefined For Axiom:\n\t"+axiom.toString());
		// Disjoint Data
		}else if(type.equals("DisjointDataProperties")) {
			ontologyComposition.replace("disjoint data", ontologyComposition.get("disjoint data") + 1);
			throw new Exception("Disjoint Properties Normalization Undefined For Axiom:\n\t"+axiom.toString());
		// Sub Role chain
		}else if (type.equals("SubPropertyChainOf")) {
			ontologyComposition.replace("subrole chain", ontologyComposition.get("subrole chain") + 1);
			roleAxioms.add((OWLSubPropertyChainOfAxiom)axiom);				
		// Inverse Role
		}else if (type.equals("InverseObjectProperties")) {
			ontologyComposition.replace("inverse role", ontologyComposition.get("inverse role") + 1);
			roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(((OWLInverseObjectPropertiesAxiom)axiom).getFirstProperty().getInverseProperty(),((OWLInverseObjectPropertiesAxiom)axiom).getSecondProperty(),Collections.emptyList()));
			roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(((OWLInverseObjectPropertiesAxiom)axiom).getFirstProperty(),((OWLInverseObjectPropertiesAxiom)axiom).getSecondProperty().getInverseProperty(),Collections.emptyList()));
		// symmetric role
		}else if (type.equals("SymmetricObjectProperty")) {	
			ontologyComposition.replace("symmetric role", ontologyComposition.get("symmetric role") + 1);
			roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(((OWLSymmetricObjectPropertyAxiom)axiom).getProperty(),((OWLSymmetricObjectPropertyAxiom)axiom).getProperty().getInverseProperty(),Collections.emptyList()));
		// asymmetric role
		}else if (type.equals("AsymmetricObjectProperty")) {
			ontologyComposition.replace("asymmetric role", ontologyComposition.get("asymmetric role") + 1);
			throw new Exception("Asymmetric Property Normalization Undefined For Axiom:\n\t"+axiom.toString());
		// functional role
		}else if (type.equals("FunctionalObjectProperty")) {
			ontologyComposition.replace("functional role", ontologyComposition.get("functional role") + 1);
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(((OWLFunctionalObjectPropertyAxiom)axiom).getProperty(),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()));
		// functional data
		}else if (type.equals("FunctionalDataProperty")) {
			ontologyComposition.replace("functional data", ontologyComposition.get("functional data") + 1);
			classAxioms.add(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLDataMaxCardinalityImpl(((OWLFunctionalDataPropertyAxiom)axiom).getProperty(), 1, new OWLDatatypeImpl(IRI.create("rdfs:Literal"))), Collections.emptyList()));
		// inverse functional role
		}else if (type.equals("InverseFunctionalObjectProperty")) {
			ontologyComposition.replace("inverse functional role", ontologyComposition.get("inverse functional role") + 1);
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectMaxCardinalityImpl(((OWLInverseFunctionalObjectPropertyAxiom)axiom).getProperty().getInverseProperty(),1,new OWLClassImpl(IRI.create(OWL.THING.toString()))), Collections.emptyList()));		
		// transitive role
		}else if (type.equals("TransitiveObjectProperty")) {
			ontologyComposition.replace("transitive role", ontologyComposition.get("transitive role") + 1);
			ArrayList<OWLObjectPropertyExpression> list = new ArrayList<OWLObjectPropertyExpression>();
			list.add(((OWLTransitiveObjectPropertyAxiom)axiom).getProperty());
			list.add(((OWLTransitiveObjectPropertyAxiom)axiom).getProperty());
			roleAxioms.add(new OWLSubPropertyChainAxiomImpl(list,((OWLTransitiveObjectPropertyAxiom)axiom).getProperty(), Collections.emptyList()));
		// role range
		}else if (type.equals("ObjectPropertyRange")) {
			ontologyComposition.replace("role range", ontologyComposition.get("role range") + 1);
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectAllValuesFromImpl(((OWLObjectPropertyRangeAxiom)axiom).getProperty(), ((OWLObjectPropertyRangeAxiom)axiom).getRange()), Collections.emptyList()));	
		// data range
		}else if (type.equals("DataPropertyRange")) {
			ontologyComposition.replace("data range", ontologyComposition.get("data range") + 1);
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLDataAllValuesFromImpl(((OWLDataPropertyRangeAxiom)axiom).getProperty(), ((OWLDataPropertyRangeAxiom)axiom).getRange()), Collections.emptyList()));
		// role domain
		}else if (type.equals("ObjectPropertyDomain")) {
			ontologyComposition.replace("role domain", ontologyComposition.get("role domain") + 1);
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLObjectSomeValuesFromImpl(((OWLObjectPropertyDomainAxiom)axiom).getProperty(), new OWLClassImpl(IRI.create(OWL.THING.toString()))), ((OWLObjectPropertyDomainAxiom)axiom).getDomain(), Collections.emptyList()));	
		// data domain
		}else if (type.equals("DataPropertyDomain")) {
			ontologyComposition.replace("data domain", ontologyComposition.get("data domain") + 1);
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLDataSomeValuesFromImpl(((OWLDataPropertyDomainAxiom)axiom).getProperty(), new OWLDatatypeImpl(IRI.create("rdfs:Literal"))), ((OWLDataPropertyDomainAxiom)axiom).getDomain(), Collections.emptyList()));	
		// oops forgot one
		}else {
			throw new Exception("Axiom not handeled:\n\t"+axiom.toString());
		}
	}
	
	/**
	 * Sorts TBox statements based on their NNF
	 */
	private void parseSubClassOfAxiom(OWLSubClassOfAxiom inAxiom) throws Exception {
		
		OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom)inAxiom.getNNF();
		
		//see how big it is
		int size = getSubClassOfAxiomSize(inAxiom);
		int nnfSize = getSubClassOfAxiomSize(axiom);
			
		// it was nnf and the right size! woohoo!
		if(nnfSize == size && size <= OWLAxMatcher.getMaxOWLAxAxiomSize()) {
			classAxioms.add(axiom);
			
		// is it the right size, but not nnf?
		}else if (size <= OWLAxMatcher.getMaxOWLAxAxiomSize() || nnfSize <= OWLAxMatcher.getMaxOWLAxAxiomSize()) {

			//get the antecedent and consequent
			OWLClassExpression superClass = ((OWLSubClassOfAxiom)inAxiom).getSuperClass();
			OWLClassExpression subClass = ((OWLSubClassOfAxiom)inAxiom).getSubClass();
			
			//is it an object exact cardinality?
			if (superClass.getClassExpressionType().getName().equals("ObjectExactCardinality")) {				
				parseObjectExactCardinality(inAxiom,subClass,superClass);		
				
			//is it a data exact cardinality?
			}else if (superClass.getClassExpressionType().getName().equals("DataExactCardinality")) {				
				parseDataExactCardinality(inAxiom,subClass,superClass);
			//uh oh
			}else{				
				throw new Exception(String.format("\nUNHANDLED AXIOM:\n%s\nNNF:\n%s\n",axiom.toString(),axiom.getNNF().toString()));	
			}
		}
		// is it too big for now?
		else if (size > OWLAxMatcher.getMaxOWLAxAxiomSize() && nnfSize > OWLAxMatcher.getMaxOWLAxAxiomSize()) {
			complexClassAxioms.add(axiom);
		}else {
			throw new Exception(String.format("\nUNHANDLED AXIOM:\n%s\nNNF:\n%s\n",axiom.toString(),axiom.getNNF().toString()));	
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
		
		// class = 1 && nominal = 1
		if (type.equals("Class") || type.equals("ObjectOneOf")) {
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
		}else if (type.equals("DataSomeValuesFrom") || type.equals("DataAllValuesFrom") || type.equals("DataMaxCardinality") || type.equals("DataMinCardinality") || type.equals("DataExactCardinality") || type.equals("ObjectHasValue")) {
			return 2;
		// self = always exactly one thing
		}else if (type.equals("ObjectHasSelf") || type.equals("DataHasValue")) {
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
	public static int getObjectPropertyAxiomSize(OWLPropertyAxiom axiom) {
		
		// look at its type
		String type = axiom.getAxiomType().getName();
		
		// chain = | chain | + 1
		if (type.equals("SubPropertyChainOf")) {
			return ((OWLSubPropertyChainOfAxiom)axiom).getPropertyChain().size() + 1;
		// property = 2 (no chain)
		}else if (type.equals("SubObjectPropertyOf") || type.equals("SubDataPropertyOf")) {
			return 2;
		// inverse = 2 (same as property)
		}else if (type.equals("InverseObjectProperties")) {
			return 2;
		}
		//don't want to throw an exception but this should be obviously wrong
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Returns a map that contains the count of each type of axiom in the ontology
	 * 
	 * @return count of all types of axioms in ontology HashMap<String,Integer>
	 */
	public HashMap<String,Integer> getOntologyComposition(){
		return ontologyComposition;
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
		for (OWLPropertyAxiom s : roleAxioms) {
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n",s.toString(),getObjectPropertyAxiomSize(s)));
		}
		return sb.toString();
	}

	/**
	 * Gets the Role axioms (includes data property axioms)
	 * 
	 * @return axioms ArrayList<OWLPropertyAxiom>
	 */
	public ArrayList<OWLPropertyAxiom> getRoleAxioms() {
		return roleAxioms;
	}
	
	/**
	 * Gets all simple class Axioms that are shorter than 3 terms
	 * 
	 * @return axioms ArrayList<OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getSimpleClassAxioms() {
		return classAxioms;
	}
	
	/**
	 * Gets all complex class Axioms that are longer than 3 terms
	 * 
	 * @return axioms ArrayList<OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getComplexClassAxioms() {
		return complexClassAxioms;
	}
	
}
