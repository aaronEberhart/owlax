package ontologyTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;


import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasSelfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubObjectPropertyOfAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubPropertyChainAxiomImpl;

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
	private static final String[] ontologyHashKeys = {"subclass","equivalent classes","disjoint classes","disjoint union","subrole","subdata","equivalent roles","equivalent data","disjoint roles","disjoint data","subrole chain","inverse role","reflexive role","irreflexive role","symmetric role","asymmetric role","role cardinality","data cardinality","functional role","functional data","inverse functional role","transitive role","role domain","data domain","role range","data range"};
	private ArrayList<OWLSubClassOfAxiom> classAxioms;
	private ArrayList<OWLSubClassOfAxiom> complexClassAxioms;
	private ArrayList<OWLPropertyAxiom> roleAxioms;
	private HashMap<String,Double> ontologyComposition;
	
	/**
	 * Gets axioms from the ontology and then sorts them
	 * 
	 * @param ontology OWLOntology
	 */
	public NormalizeAndSortAxioms(OWLOntology ontology) {
		
		//initialize everything
		roleAxioms = new ArrayList<OWLPropertyAxiom>();
		classAxioms = new ArrayList<OWLSubClassOfAxiom>();
		complexClassAxioms = new ArrayList<OWLSubClassOfAxiom>();
		
		//save the quantities of these things for later
		ontologyComposition = new HashMap<String,Double>(){private static final long serialVersionUID = 3L;{
			put("number of classes",(double)ontology.classesInSignature().count());
			put("number of roles",(double)ontology.objectPropertiesInSignature().count());
			put("number of data properties",(double)ontology.dataPropertiesInSignature().count());
			put(ontology.getFormat().toString(),(double)ontologyIndex);
			put(ontology.getOntologyID().toString(),(double)ontologyIndex--);
			for (String key : ontologyHashKeys){put(key,0.0);}
		}};
		
		//sort the axioms from the ontology
		getAxioms(ontology).forEach(a -> {try {sortAxiomByType(a.getAxiomWithoutAnnotations());} catch (Exception e) {
			System.err.println(e);}});
		
		ontologyComposition.put("simple class axioms", (double)classAxioms.size());
		ontologyComposition.put("complex class axioms", (double)complexClassAxioms.size());
		ontologyComposition.put("role axioms", (double)roleAxioms.size());
	}

	/**
	 * Gets all axioms from the ontology, ignoring annotations, class assertions, and declarations
	 */
	private Stream<OWLAxiom> getAxioms(OWLOntology ontology){
		return ontology.axioms().filter(a -> correctType(a.getAxiomType().getName()));
	}
	
	/**
	 * False if the type string of an axiom is Annotation, class assertion, role/data assertion, Different individuals, or Declaration, True otherwise
	 */
	private boolean correctType(String type) {
		return !(type.equals("DifferentIndividuals") || type.equals("Rule") || type.equals("AnnotationAssertion") || type.equals("Declaration") 
				|| type.equals("AnnotationPropertyDomainOf") || type.equals("AnnotationPropertyRangeOf") || type.contentEquals("SubAnnotationPropertyOf") 
				|| type.equals("AnnotationPropertyDomain") || type.equals("AnnotationPropertyRange") || type.contentEquals("SubAnnotationProperty") 
				|| type.equals("ClassAssertion") || type.equals("ObjectPropertyAssertion") || type.equals("DataPropertyAssertion") || type.equals("DatatypeDefinition"));
	}
	
	/**
	 * Sorts one axiom by its type
	 */
	private void sortAxiomByType(OWLAxiom axiom) {
		
		String type = axiom.getAxiomType().getName();
		
		try {
			// Sub Class
			if (type.equals("SubClassOf")) {
				ontologyComposition.replace("subclass", ontologyComposition.get("subclass") + 1);
				//these are nested so parse them separately
				parseSubClassOfAxiom((OWLSubClassOfAxiom)axiom);
			// equivalent classes
			}else if (type.equals("EquivalentClasses")) {
					ontologyComposition.replace("equivalent classes", ontologyComposition.get("equivalent classes") + 1);
					((OWLEquivalentClassesAxiom)axiom).asOWLSubClassOfAxioms().forEach(a -> { try { parseSubClassOfAxiom((OWLSubClassOfAxiom)a); } catch (Exception e) {System.err.println(e);}});
			// disjoint classes
			}else if (type.equals("DisjointClasses")) {			
				ontologyComposition.replace("disjoint classes", ontologyComposition.get("disjoint classes") + 1);
				((OWLDisjointClassesAxiom)axiom).asOWLSubClassOfAxioms().forEach(a -> {try {parseSubClassOfAxiom(a);} catch (Exception e) {System.err.println(e);}});
			// disjoint union
			}else if (type.equals("DisjointUnion")) {
				ontologyComposition.replace("disjoint union", ontologyComposition.get("disjoint union") + 1);
				((OWLDisjointUnionAxiom)axiom).getOWLEquivalentClassesAxiom().asOWLSubClassOfAxioms().forEach(a -> { try { parseSubClassOfAxiom((OWLSubClassOfAxiom)a); } catch (Exception e) {System.err.println(e);}});
				((OWLDisjointUnionAxiom)axiom).getOWLDisjointClassesAxiom().asOWLSubClassOfAxioms().forEach(a -> { try { parseSubClassOfAxiom((OWLSubClassOfAxiom)a); } catch (Exception e) {System.err.println(e);}});
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
				((OWLEquivalentObjectPropertiesAxiom)axiom).asSubObjectPropertyOfAxioms().forEach(a -> roleAxioms.add(a));
			// Equivalent Data
			}else if (type.equals("EquivalentDataProperties")) {
				ontologyComposition.replace("equivalent data", ontologyComposition.get("equivalent data") + 1);
				((OWLEquivalentDataPropertiesAxiom)axiom).asSubDataPropertyOfAxioms().forEach(a -> roleAxioms.add(a));
			// Disjoint Roles
			}else if (type.equals("DisjointObjectProperties")) {
				ontologyComposition.replace("disjoint roles", ontologyComposition.get("disjoint roles") + 1);
				roleAxioms.add((OWLDisjointObjectPropertiesAxiom)axiom);
				System.err.println("Disjoint Properties Normalization Undefined For Axiom:\n\t"+axiom.toString());
			// Disjoint Data
			}else if(type.equals("DisjointDataProperties")) {
				ontologyComposition.replace("disjoint data", ontologyComposition.get("disjoint data") + 1);
				roleAxioms.add((OWLDisjointDataPropertiesAxiom)axiom);
				System.err.println("Disjoint Properties Normalization Undefined For Axiom:\n\t"+axiom.toString());
			// Sub Role chain
			}else if (type.equals("SubPropertyChainOf")) {
				ontologyComposition.replace("subrole chain", ontologyComposition.get("subrole chain") + 1);
				roleAxioms.add((OWLSubPropertyChainOfAxiom)axiom);				
			// Inverse Role
			}else if (type.equals("InverseObjectProperties")) {
				ontologyComposition.replace("inverse role", ontologyComposition.get("inverse role") + 1);
				roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(((OWLInverseObjectPropertiesAxiom)axiom).getFirstProperty().getInverseProperty(),((OWLInverseObjectPropertiesAxiom)axiom).getSecondProperty(),Collections.emptyList()));
				roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(((OWLInverseObjectPropertiesAxiom)axiom).getSecondProperty().getInverseProperty(),((OWLInverseObjectPropertiesAxiom)axiom).getFirstProperty(),Collections.emptyList()));
			// reflexive role
			}else if (type.equals("ReflexiveObjectProperty")) {	
				ontologyComposition.replace("reflexive role", ontologyComposition.get("reflexive role") + 1);
				classAxioms.add(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())), new OWLObjectHasSelfImpl(((OWLReflexiveObjectPropertyAxiom)axiom).getProperty()), Collections.emptyList()));
			// irreflexive role
			}else if (type.equals("IrrefexiveObjectProperty")) {	
				ontologyComposition.replace("irreflexive role", ontologyComposition.get("irreflexive role") + 1);
				classAxioms.add(new OWLSubClassOfAxiomImpl(new OWLObjectHasSelfImpl(((OWLIrreflexiveObjectPropertyAxiom)axiom).getProperty()),new OWLClassImpl(IRI.create(OWL.NOTHING.toString())), Collections.emptyList()));
			// symmetric role
			}else if (type.equals("SymmetricObjectProperty")) {	
				ontologyComposition.replace("symmetric role", ontologyComposition.get("symmetric role") + 1);
				roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(((OWLSymmetricObjectPropertyAxiom)axiom).getProperty(),((OWLSymmetricObjectPropertyAxiom)axiom).getProperty().getInverseProperty(),Collections.emptyList()));
			// asymmetric role
			}else if (type.equals("AsymmetricObjectProperty")) {
				ontologyComposition.replace("asymmetric role", ontologyComposition.get("asymmetric role") + 1);
				roleAxioms.add((OWLAsymmetricObjectPropertyAxiom)axiom);
				System.err.println("Asymmetric Properties Normalization Undefined For Axiom:\n\t"+axiom.toString());
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
				roleAxioms.add(new OWLSubPropertyChainAxiomImpl(Arrays.asList(((OWLTransitiveObjectPropertyAxiom)axiom).getProperty(),((OWLTransitiveObjectPropertyAxiom)axiom).getProperty()),((OWLTransitiveObjectPropertyAxiom)axiom).getProperty(), Collections.emptyList()));
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
				throw new Exception("Axiom not handled:\n\t"+axiom.toString());
			}
		}catch(Exception e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Sorts TBox statements based on their NNF
	 */
	private void parseSubClassOfAxiom(OWLSubClassOfAxiom inAxiom) throws Exception {
		
		//see how big it is
		int size = getSubClassOfAxiomSize(inAxiom);
		
		// size = 2 -> already nnf
		if(size == 2) {
			classAxioms.add(inAxiom);
		
		}else {			
			//get the nnf
			OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom)inAxiom.getNNF();
			int nnfSize = getSubClassOfAxiomSize(axiom);
				
			// is it the right size, but not nnf?
			// cardinality _should_ be the only axiom type that grows in NNF
			if (size <= OWLAxMatcher.getMaxOWLAxAxiomSize() && nnfSize > OWLAxMatcher.getMaxOWLAxAxiomSize()) {				
				
				//get the antecedent and consequent
				OWLClassExpression superClass = ((OWLSubClassOfAxiom)inAxiom).getSuperClass();
				OWLClassExpression subClass = ((OWLSubClassOfAxiom)inAxiom).getSubClass();
				
				try {
					//is it an object exact cardinality?
					if (superClass.getClassExpressionType().getName().equals("ObjectExactCardinality")) {
						ontologyComposition.replace("role cardinality", ontologyComposition.get("role cardinality") + 1);
						parseObjectExactCardinality(inAxiom,subClass,(OWLObjectExactCardinality)superClass);		
					}else if (subClass.getClassExpressionType().getName().equals("ObjectExactCardinality")) {
						ontologyComposition.replace("role cardinality", ontologyComposition.get("role cardinality") + 1);
						parseObjectExactCardinality(inAxiom,(OWLObjectExactCardinality)subClass,superClass);
					//is it a data exact cardinality?
					}else if (superClass.getClassExpressionType().getName().equals("DataExactCardinality")) {				
						ontologyComposition.replace("data cardinality", ontologyComposition.get("data cardinality") + 1);
						parseDataExactCardinality(inAxiom,subClass,(OWLDataExactCardinality)superClass);
					}else if (subClass.getClassExpressionType().getName().equals("DataExactCardinality")) {
						ontologyComposition.replace("data cardinality", ontologyComposition.get("data cardinality") + 1);
						parseDataExactCardinality(inAxiom,(OWLDataExactCardinality)subClass,superClass);
					}else {
						complexClassAxioms.add(axiom);	
					}
					//uh oh
					}catch(Exception e){				
						System.err.println(e);	
					}
				// nnf was the right size! woohoo!
				}else if(nnfSize <= OWLAxMatcher.getMaxOWLAxAxiomSize()) {
					
					//can we normalize a conjunction?
					if (axiom.getSuperClass().getClassExpressionType().getName().equals("ObjectIntersectionOf")) {
						((OWLObjectIntersectionOf)axiom.getSuperClass()).conjunctSet().forEach(a -> classAxioms.add(new OWLSubClassOfAxiomImpl(axiom.getSubClass(), a, Collections.emptyList())));
					//can we normalize a disjunction?
					}else if (axiom.getSubClass().getClassExpressionType().getName().equals("ObjectUnionOf")) {
						((OWLObjectUnionOf)axiom.getSubClass()).disjunctSet().forEach(a -> classAxioms.add(new OWLSubClassOfAxiomImpl(a, axiom.getSuperClass(), Collections.emptyList())));
					//oh well couldn't normalize but still works
					}else {
						classAxioms.add(axiom);
					}
			// is it too big for now?
			}else if (size > OWLAxMatcher.getMaxOWLAxAxiomSize() && nnfSize > OWLAxMatcher.getMaxOWLAxAxiomSize()) {
				complexClassAxioms.add(axiom);
			}else {
				throw new Exception(String.format("\nIMPOSSIBLE AXIOM:\n%s\nNNF:\n%s\n",axiom.toString(),axiom.getNNF().toString()));	
			}
		}
	}
		
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseObjectExactCardinality(OWLSubClassOfAxiom axiom,OWLObjectExactCardinality subClass,OWLClassExpression superClass) throws Exception {
		
		//get stuff from old axiom		
		OWLObjectPropertyExpression property = subClass.getProperty();
		OWLClassExpression classexpression = subClass.getFiller();
		List<OWLAnnotation> annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));
		
		//split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(new OWLObjectMaxCardinalityImpl(property, 1, classexpression),superClass,annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(new OWLObjectSomeValuesFromImpl(property, classexpression),superClass,annotations);
		
		//add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);		
	}
	
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseDataExactCardinality(OWLSubClassOfAxiom axiom,OWLDataExactCardinality subClass,OWLClassExpression superClass) throws Exception {
		
		//get stuff from old axiom	
		OWLDataPropertyExpression property = subClass.getProperty();
		OWLDataRange datarange = subClass.getFiller();
		List<OWLAnnotation> annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));
		
		//split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(new OWLDataMaxCardinalityImpl(property, 1, datarange),superClass,annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(new OWLDataSomeValuesFromImpl(property, datarange),superClass, annotations);
		
		//add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);		
	}
	
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseObjectExactCardinality(OWLSubClassOfAxiom axiom,OWLClassExpression subClass,OWLObjectExactCardinality superClass) throws Exception {
		
		//get stuff from old axiom		
		OWLObjectPropertyExpression property = superClass.getProperty();
		OWLClassExpression classexpression = superClass.getFiller();
		List<OWLAnnotation> annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));
		
		//split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(subClass,new OWLObjectMaxCardinalityImpl(property, 1, classexpression), annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(subClass,new OWLObjectSomeValuesFromImpl(property, classexpression), annotations);
		
		//add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);		
	}
	
	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseDataExactCardinality(OWLSubClassOfAxiom axiom,OWLClassExpression subClass,OWLDataExactCardinality superClass) throws Exception {
		
		//get stuff from old axiom	
		OWLDataPropertyExpression property = superClass.getProperty();
		OWLDataRange datarange = superClass.getFiller();
		List<OWLAnnotation> annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));
				
		//split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(subClass,new OWLDataMaxCardinalityImpl(property, 1, datarange), annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(subClass,new OWLDataSomeValuesFromImpl(property, datarange), annotations);
		
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
	 * Gets the size of an owl class expression
	 */
	private static int getClassExpressionSize(OWLClassExpression expression) {
		
		//look at its type
		String type = expression.getClassExpressionType().getName();
		
		// class = 1 && nominal = 1
		if (type.equals("Class")) {
			return 1;
		// nominal
		} else if (type.equals("ObjectOneOf")) {
			return (int)((OWLObjectOneOf)expression).individuals().count();
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
		// data = same as class but not nested
		}else if (type.equals("DataSomeValuesFrom") || type.equals("DataAllValuesFrom") || type.equals("DataMaxCardinality") || type.equals("DataMinCardinality") || type.equals("DataExactCardinality")) {
			return  2;
		// \exists R.{a}
		}else if (type.equals("ObjectHasValue")){
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
	 * @return HashMap&lt;String,Integer>
	 */
	public HashMap<String,Double> getOntologyComposition(){
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
	 * @return ArrayList&lt;OWLPropertyAxiom>
	 */
	public ArrayList<OWLPropertyAxiom> getRoleAxioms() {
		return roleAxioms;
	}
	
	/**
	 * Gets all simple class Axioms that are shorter than or equal to
	 * the max size of all OWLAx axioms
	 * 
	 * @return ArrayList&lt;OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getSimpleClassAxioms() {
		return classAxioms;
	}
	
	/**
	 * Gets all complex class Axioms that are longer than the
	 * max size of all OWLAx axioms
	 * 
	 * @return ArrayList&lt;OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getComplexClassAxioms() {
		return complexClassAxioms;
	}
	
}
