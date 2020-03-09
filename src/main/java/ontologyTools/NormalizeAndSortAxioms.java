package ontologyTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

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
 * Normalizes and sorts the axioms in an ontology so that they can be evaluated
 * for OWLAx coverage
 * 
 * @author Aaron Eberhart
 * @author DaSe Lab
 *
 */
public class NormalizeAndSortAxioms
{
	private ArrayList<OWLSubClassOfAxiom> classAxioms;
	private ArrayList<OWLSubClassOfAxiom> complexClassAxioms;
	private ArrayList<OWLPropertyAxiom>   roleAxioms;
	private OntologyComposition           ontologyComposition;

	/**
	 * Gets axioms from the ontology and then sorts them
	 * 
	 * @param ontology OWLOntology
	 * @throws Exception
	 */
	public NormalizeAndSortAxioms(OWLOntology ontology) throws Exception
	{

		// initialize everything
		roleAxioms = new ArrayList<OWLPropertyAxiom>();
		classAxioms = new ArrayList<OWLSubClassOfAxiom>();
		complexClassAxioms = new ArrayList<OWLSubClassOfAxiom>();

		// save the quantities of these things for later
		this.ontologyComposition = new OntologyComposition();
		this.ontologyComposition.setnClassesInSignature((int) ontology.classesInSignature().count());
		this.ontologyComposition.setnObjectPropertiesInSignature((int) ontology.objectPropertiesInSignature().count());
		this.ontologyComposition.setnDataPropertiesInSignature((int) ontology.dataPropertiesInSignature().count());

		// sort the axioms from the ontology
		getAxioms(ontology).forEach(a -> {
			try
			{
				sortAxiomByType(a.getAxiomWithoutAnnotations());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		});

		this.ontologyComposition.setnSimpleClassAxioms(classAxioms.size());
		this.ontologyComposition.setnComplexClassAxioms(complexClassAxioms.size());
		this.ontologyComposition.setnRoleAxioms(roleAxioms.size());
	}

	/**
	 * Gets all axioms from the ontology, ignoring annotations, class assertions,
	 * and declarations
	 */
	private Stream<OWLAxiom> getAxioms(OWLOntology ontology)
	{
		return ontology.axioms().filter(a -> correctType(a.getAxiomType().getName()));
	}

	/**
	 * False if the type string of an axiom is Annotation, class assertion,
	 * Different individuals, or Declaration, True otherwise
	 */
	private boolean correctType(String type)
	{
		return !(type.equals("DifferentIndividuals") || type.equals("Rule") || type.equals("AnnotationAssertion")
				|| type.equals("Declaration") || type.equals("AnnotationPropertyRangeOf")
				|| type.contentEquals("SubAnnotationPropertyOf") || type.equals("ClassAssertion"));
	}

	/**
	 * Sorts one axiom by its type
	 */
	private void sortAxiomByType(OWLAxiom axiom) throws Exception
	{

		String type = axiom.getAxiomType().getName();

		// Sub Class
		if (type.equals("SubClassOf"))
		{
			ontologyComposition.incrementAxiomTypeCount("subclass");
			// these are nested so parse them separately
			parseSubClassOfAxiom((OWLSubClassOfAxiom) axiom);
			// equivalent classes
		}
		else if (type.equals("EquivalentClasses"))
		{
			ontologyComposition.incrementAxiomTypeCount("equivalent classes");
			((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms().forEach(a -> {
				try
				{
					parseSubClassOfAxiom((OWLSubClassOfAxiom) a);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			// disjoint classes
		}
		else if (type.equals("DisjointClasses"))
		{
			ontologyComposition.incrementAxiomTypeCount("disjoint classes");
			((OWLDisjointClassesAxiom) axiom).asOWLSubClassOfAxioms().forEach(a -> {
				try
				{
					parseSubClassOfAxiom(a);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			// disjoint union
		}
		else if (type.equals("DisjointUnion"))
		{
			ontologyComposition.incrementAxiomTypeCount("disjoint union");
			((OWLDisjointUnionAxiom) axiom).getOWLEquivalentClassesAxiom().asOWLSubClassOfAxioms().forEach(a -> {
				try
				{
					parseSubClassOfAxiom((OWLSubClassOfAxiom) a);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			((OWLDisjointUnionAxiom) axiom).getOWLDisjointClassesAxiom().asOWLSubClassOfAxioms().forEach(a -> {
				try
				{
					parseSubClassOfAxiom((OWLSubClassOfAxiom) a);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			// Sub Role
		}
		else if (type.equals("SubObjectPropertyOf"))
		{
			ontologyComposition.incrementAxiomTypeCount("subrole");
			roleAxioms.add((OWLSubObjectPropertyOfAxiom) axiom);
			// Sub Data
		}
		else if (type.equals("SubDataPropertyOf"))
		{
			ontologyComposition.incrementAxiomTypeCount("subdata");
			roleAxioms.add((OWLSubDataPropertyOfAxiom) axiom);
			// Equivalent Roles
		}
		else if (type.equals("EquivalentObjectProperties"))
		{
			ontologyComposition.incrementAxiomTypeCount("equivalent roles");
			((OWLEquivalentObjectPropertiesAxiom) axiom).asSubObjectPropertyOfAxioms().forEach(a -> roleAxioms.add(a));
			// Equivalent Data
		}
		else if (type.equals("EquivalentDataProperties"))
		{
			ontologyComposition.incrementAxiomTypeCount("equivalent data");
			((OWLEquivalentDataPropertiesAxiom) axiom).asSubDataPropertyOfAxioms().forEach(a -> roleAxioms.add(a));
			// Disjoint Roles
		}
		else if (type.equals("DisjointObjectProperties"))
		{
			ontologyComposition.incrementAxiomTypeCount("disjoint roles");
			roleAxioms.add((OWLDisjointObjectPropertiesAxiom) axiom);
			System.err.println("Disjoint Properties Normalization Undefined For Axiom:\n\t" + axiom.toString());
			// Disjoint Data
		}
		else if (type.equals("DisjointDataProperties"))
		{
			ontologyComposition.incrementAxiomTypeCount("disjoint data");
			roleAxioms.add((OWLDisjointDataPropertiesAxiom) axiom);
			System.err.println("Disjoint Properties Normalization Undefined For Axiom:\n\t" + axiom.toString());
			// Sub Role chain
		}
		else if (type.equals("SubPropertyChainOf"))
		{
			ontologyComposition.incrementAxiomTypeCount("subrole chain");
			roleAxioms.add((OWLSubPropertyChainOfAxiom) axiom);
			// Inverse Role
		}
		else if (type.equals("InverseObjectProperties"))
		{
			ontologyComposition.incrementAxiomTypeCount("inverse role");
			roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(
					((OWLInverseObjectPropertiesAxiom) axiom).getFirstProperty().getInverseProperty(),
					((OWLInverseObjectPropertiesAxiom) axiom).getSecondProperty(), Collections.emptyList()));
			roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(
					((OWLInverseObjectPropertiesAxiom) axiom).getSecondProperty().getInverseProperty(),
					((OWLInverseObjectPropertiesAxiom) axiom).getFirstProperty(), Collections.emptyList()));
			// reflexive role
		}
		else if (type.equals("ReflexiveObjectProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("reflexive role");
			classAxioms.add(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())),
					new OWLObjectHasSelfImpl(((OWLReflexiveObjectPropertyAxiom) axiom).getProperty()),
					Collections.emptyList()));
			// irreflexive role
		}
		else if (type.equals("IrrefexiveObjectProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("irreflexive role");
			classAxioms.add(new OWLSubClassOfAxiomImpl(
					new OWLObjectHasSelfImpl(((OWLIrreflexiveObjectPropertyAxiom) axiom).getProperty()),
					new OWLClassImpl(IRI.create(OWL.NOTHING.toString())), Collections.emptyList()));
			// symmetric role
		}
		else if (type.equals("SymmetricObjectProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("symmetric role");
			roleAxioms.add(new OWLSubObjectPropertyOfAxiomImpl(((OWLSymmetricObjectPropertyAxiom) axiom).getProperty(),
					((OWLSymmetricObjectPropertyAxiom) axiom).getProperty().getInverseProperty(),
					Collections.emptyList()));
			// asymmetric role
		}
		else if (type.equals("AsymmetricObjectProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("asymmetric role");
			roleAxioms.add((OWLAsymmetricObjectPropertyAxiom) axiom);
			System.err.println("Asymmetric Properties Normalization Undefined For Axiom:\n\t" + axiom.toString());
			// functional role
		}
		else if (type.equals("FunctionalObjectProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("functional role");
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())),
					new OWLObjectMaxCardinalityImpl(((OWLFunctionalObjectPropertyAxiom) axiom).getProperty(), 1,
							new OWLClassImpl(IRI.create(OWL.THING.toString()))),
					Collections.emptyList()));
			// functional data
		}
		else if (type.equals("FunctionalDataProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("functional data");
			classAxioms.add(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())),
					new OWLDataMaxCardinalityImpl(((OWLFunctionalDataPropertyAxiom) axiom).getProperty(), 1,
							new OWLDatatypeImpl(IRI.create("rdfs:Literal"))),
					Collections.emptyList()));
			// inverse functional role
		}
		else if (type.equals("InverseFunctionalObjectProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("inverse functional role");
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())),
					new OWLObjectMaxCardinalityImpl(
							((OWLInverseFunctionalObjectPropertyAxiom) axiom).getProperty().getInverseProperty(), 1,
							new OWLClassImpl(IRI.create(OWL.THING.toString()))),
					Collections.emptyList()));
			// transitive role
		}
		else if (type.equals("TransitiveObjectProperty"))
		{
			ontologyComposition.incrementAxiomTypeCount("transitive role");
			roleAxioms.add(new OWLSubPropertyChainAxiomImpl(
					Arrays.asList(((OWLTransitiveObjectPropertyAxiom) axiom).getProperty(),
							((OWLTransitiveObjectPropertyAxiom) axiom).getProperty()),
					((OWLTransitiveObjectPropertyAxiom) axiom).getProperty(), Collections.emptyList()));
			// role range
		}
		else if (type.equals("ObjectPropertyRange"))
		{
			ontologyComposition.incrementAxiomTypeCount("role range");
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())),
					new OWLObjectAllValuesFromImpl(((OWLObjectPropertyRangeAxiom) axiom).getProperty(),
							((OWLObjectPropertyRangeAxiom) axiom).getRange()),
					Collections.emptyList()));
			// data range
		}
		else if (type.equals("DataPropertyRange"))
		{
			ontologyComposition.incrementAxiomTypeCount("data range");
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(new OWLClassImpl(IRI.create(OWL.THING.toString())),
					new OWLDataAllValuesFromImpl(((OWLDataPropertyRangeAxiom) axiom).getProperty(),
							((OWLDataPropertyRangeAxiom) axiom).getRange()),
					Collections.emptyList()));
			// role domain
		}
		else if (type.equals("ObjectPropertyDomain"))
		{
			ontologyComposition.incrementAxiomTypeCount("role domain");
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(
					new OWLObjectSomeValuesFromImpl(((OWLObjectPropertyDomainAxiom) axiom).getProperty(),
							new OWLClassImpl(IRI.create(OWL.THING.toString()))),
					((OWLObjectPropertyDomainAxiom) axiom).getDomain(), Collections.emptyList()));
			// data domain
		}
		else if (type.equals("DataPropertyDomain"))
		{
			ontologyComposition.incrementAxiomTypeCount("data domain");
			parseSubClassOfAxiom(new OWLSubClassOfAxiomImpl(
					new OWLDataSomeValuesFromImpl(((OWLDataPropertyDomainAxiom) axiom).getProperty(),
							new OWLDatatypeImpl(IRI.create("rdfs:Literal"))),
					((OWLDataPropertyDomainAxiom) axiom).getDomain(), Collections.emptyList()));
			// oops forgot one
		}
		else
		{
			throw new Exception("Axiom not handled:\n\t" + axiom.toString());
		}
	}

	/**
	 * Sorts TBox statements based on their NNF
	 */
	private void parseSubClassOfAxiom(OWLSubClassOfAxiom inAxiom) throws Exception
	{

		// see how big it is
		int size = getSubClassOfAxiomSize(inAxiom);

		// size = 2 -> already nnf
		if (size == 2)
		{
			classAxioms.add(inAxiom);

		}
		else
		{
			// get the nnf
			OWLSubClassOfAxiom axiom   = (OWLSubClassOfAxiom) inAxiom.getNNF();
			int                nnfSize = getSubClassOfAxiomSize(axiom);

			// nnf was the right size! woohoo!
			if (nnfSize <= OWLAxMatcher.getMaxOWLAxAxiomSize())
			{
				classAxioms.add(axiom);

				// is it the right size, but not nnf?
				// cardinality _should_ be the only axiom type that grows in NNF
			}
			else if (size <= OWLAxMatcher.getMaxOWLAxAxiomSize() && nnfSize > OWLAxMatcher.getMaxOWLAxAxiomSize())
			{

				// get the antecedent and consequent
				OWLClassExpression superClass = ((OWLSubClassOfAxiom) inAxiom).getSuperClass();
				OWLClassExpression subClass   = ((OWLSubClassOfAxiom) inAxiom).getSubClass();

				// is it an object exact cardinality?
				if (superClass.getClassExpressionType().getName().equals("ObjectExactCardinality"))
				{
					parseObjectExactCardinality(inAxiom, subClass, (OWLObjectExactCardinality) superClass);
				}
				else if (subClass.getClassExpressionType().getName().equals("ObjectExactCardinality"))
				{
					parseObjectExactCardinality(inAxiom, (OWLObjectExactCardinality) subClass, superClass);
					// is it a data exact cardinality?
				}
				else if (superClass.getClassExpressionType().getName().equals("DataExactCardinality"))
				{
					parseDataExactCardinality(inAxiom, subClass, (OWLDataExactCardinality) superClass);
				}
				else if (subClass.getClassExpressionType().getName().equals("DataExactCardinality"))
				{
					parseDataExactCardinality(inAxiom, (OWLDataExactCardinality) subClass, superClass);
					// uh oh
				}
				else
				{
					throw new Exception(String.format("\nUNHANDLED AXIOM:\n%s\nNNF:\n%s\n", axiom.toString(),
							axiom.getNNF().toString()));
				}
			}
			// is it too big for now?
			else if (size > OWLAxMatcher.getMaxOWLAxAxiomSize() && nnfSize > OWLAxMatcher.getMaxOWLAxAxiomSize())
			{
				complexClassAxioms.add(axiom);
			}
			else
			{
				throw new Exception(String.format("\nUNHANDLED AXIOM:\n%s\nNNF:\n%s\n", axiom.toString(),
						axiom.getNNF().toString()));
			}
		}
	}

	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseObjectExactCardinality(OWLSubClassOfAxiom axiom, OWLObjectExactCardinality subClass,
			OWLClassExpression superClass) throws Exception
	{

		// get stuff from old axiom
		OWLObjectPropertyExpression property    = subClass.getProperty();
		List<OWLClass>              classes     = subClass.classesInSignature().collect(Collectors.toList());
		List<OWLAnnotation>         annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));

		// check if axiom is the right size
		if (classes.size() > 1)
		{
			throw new Exception("Too many classes in consequent of \n" + axiom.toString());
		}

		// split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(new OWLObjectMaxCardinalityImpl(property, 1, classes.get(0)), superClass,
				annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(
				new OWLObjectSomeValuesFromImpl(property, classes.get(0)), superClass, annotations);

		// add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);
	}

	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseDataExactCardinality(OWLSubClassOfAxiom axiom, OWLDataExactCardinality subClass,
			OWLClassExpression superClass) throws Exception
	{

		// get stuff from old axiom
		OWLDataPropertyExpression property    = subClass.getProperty();
		List<OWLDatatype>         datatypes   = subClass.datatypesInSignature().collect(Collectors.toList());
		List<OWLAnnotation>       annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));

		// check if axiom is the right size
		if (datatypes.size() > 1)
		{
			throw new Exception("Too many datatypes in consequent of \n" + axiom.toString());
		}

		// split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(new OWLDataMaxCardinalityImpl(property, 1, datatypes.get(0)), superClass,
				annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(
				new OWLDataSomeValuesFromImpl(property, datatypes.get(0)), superClass, annotations);

		// add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);
	}

	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseObjectExactCardinality(OWLSubClassOfAxiom axiom, OWLClassExpression subClass,
			OWLObjectExactCardinality superClass) throws Exception
	{

		// get stuff from old axiom
		OWLObjectPropertyExpression property    = superClass.getProperty();
		List<OWLClass>              classes     = superClass.classesInSignature().collect(Collectors.toList());
		List<OWLAnnotation>         annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));

		// check if axiom is the right size
		if (classes.size() > 1)
		{
			throw new Exception("Too many classes in consequent of \n" + axiom.toString());
		}

		// split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(subClass, new OWLObjectMaxCardinalityImpl(property, 1, classes.get(0)),
				annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(subClass,
				new OWLObjectSomeValuesFromImpl(property, classes.get(0)), annotations);

		// add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);
	}

	/**
	 * Splits an exact cardinality axiom into two axioms that are equivalent
	 */
	private void parseDataExactCardinality(OWLSubClassOfAxiom axiom, OWLClassExpression subClass,
			OWLDataExactCardinality superClass) throws Exception
	{

		// get stuff from old axiom
		OWLDataPropertyExpression property    = superClass.getProperty();
		List<OWLDatatype>         datatypes   = superClass.datatypesInSignature().collect(Collectors.toList());
		List<OWLAnnotation>       annotations = axiom.annotations().collect(Collectors.toCollection(ArrayList::new));

		// check if axiom is the right size
		if (datatypes.size() > 1)
		{
			throw new Exception("Too many datatypes in consequent of \n" + axiom.toString());
		}

		// split into new axioms
		axiom = new OWLSubClassOfAxiomImpl(subClass, new OWLDataMaxCardinalityImpl(property, 1, datatypes.get(0)),
				annotations);
		OWLSubClassOfAxiom axiom2 = new OWLSubClassOfAxiomImpl(subClass,
				new OWLDataSomeValuesFromImpl(property, datatypes.get(0)), annotations);

		// add to tbox
		classAxioms.add(axiom);
		classAxioms.add(axiom2);
	}

	/**
	 * Gets the size of a subclass axiom
	 * 
	 * @param axiom OWLSubClassOfAxiom
	 */
	public static int getSubClassOfAxiomSize(OWLSubClassOfAxiom axiom)
	{
		return getSubClassOfAxiomSize(((OWLSubClassOfAxiom) axiom).getSubClass(),
				((OWLSubClassOfAxiom) axiom).getSuperClass());
	}

	/**
	 * Gets the size of the subclass axiom by adding the sizes of its components
	 */
	private static int getSubClassOfAxiomSize(OWLClassExpression subClass, OWLClassExpression superClass)
	{
		return getClassExpressionSize(subClass) + getClassExpressionSize(superClass);
	}

	/**
	 * Gets the size of an owl expression
	 */
	private static int getClassExpressionSize(OWLClassExpression expression)
	{

		// look at its type
		String type = expression.getClassExpressionType().getName();

		// class = 1 && nominal = 1
		if (type.equals("Class"))
		{
			return 1;
			// nominal
		}
		else if (type.equals("ObjectOneOf"))
		{
			return (int) ((OWLObjectOneOf) expression).individuals().count();
			// negation = 0
		}
		else if (type.equals("ObjectComplementOf"))
		{
			return getClassExpressionSize(expression.getComplementNNF());
			// quantifier = | class expression | + 1
		}
		else if (type.equals("ObjectSomeValuesFrom"))
		{
			return getClassExpressionSize(((OWLObjectSomeValuesFrom) expression).getFiller()) + 1;
		}
		else if (type.equals("ObjectAllValuesFrom"))
		{
			return getClassExpressionSize(((OWLObjectAllValuesFrom) expression).getFiller()) + 1;
		}
		else if (type.equals("ObjectMaxCardinality"))
		{
			return getClassExpressionSize(((OWLObjectMaxCardinality) expression).getFiller()) + 1;
		}
		else if (type.equals("ObjectMinCardinality"))
		{
			return getClassExpressionSize(((OWLObjectMinCardinality) expression).getFiller()) + 1;
		}
		else if (type.equals("ObjectExactCardinality"))
		{
			return getClassExpressionSize(((OWLObjectExactCardinality) expression).getFiller()) + 1;
			// data = same as quantifier just not nested
		}
		else if (type.equals("DataSomeValuesFrom") || type.equals("DataAllValuesFrom")
				|| type.equals("DataMaxCardinality") || type.equals("DataMinCardinality")
				|| type.equals("DataExactCardinality") || type.equals("ObjectHasValue"))
		{
			return 2;
			// self = always exactly one thing
		}
		else if (type.equals("ObjectHasSelf") || type.equals("DataHasValue"))
		{
			return 1;
			// conjunction = sum of conjuncts
		}
		else if (type.equals("ObjectIntersectionOf"))
		{
			return ((OWLObjectIntersectionOf) expression).conjunctSet().mapToInt(a -> getClassExpressionSize(a)).sum();
			// disjunction = sum of disjuncts
		}
		else if (type.equals("ObjectUnionOf"))
		{
			return ((OWLObjectUnionOf) expression).disjunctSet().mapToInt(a -> getClassExpressionSize(a)).sum();
		}
		// don't want to throw an exception but this should be obviously wrong
		return Integer.MAX_VALUE;
	}

	/**
	 * Gets the size of a property axiom
	 * 
	 * @param axiom OWLObjectPropertyAxiom
	 */
	public static int getObjectPropertyAxiomSize(OWLPropertyAxiom axiom)
	{

		// look at its type
		String type = axiom.getAxiomType().getName();

		// chain = | chain | + 1
		if (type.equals("SubPropertyChainOf"))
		{
			return ((OWLSubPropertyChainOfAxiom) axiom).getPropertyChain().size() + 1;
			// property = 2 (no chain)
		}
		else if (type.equals("SubObjectPropertyOf") || type.equals("SubDataPropertyOf"))
		{
			return 2;
			// inverse = 2 (same as property)
		}
		else if (type.equals("InverseObjectProperties"))
		{
			return 2;
		}
		// don't want to throw an exception but this should be obviously wrong
		return Integer.MAX_VALUE;
	}

	/**
	 * Returns a map that contains the count of each type of axiom in the ontology
	 * 
	 * @return count of all types of axioms in ontology HashMap<String,Integer>
	 */
	public OntologyComposition getOntologyComposition()
	{
		return ontologyComposition;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Class Axioms:\n");
		for (OWLSubClassOfAxiom s : classAxioms)
		{
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n", s.toString(), getSubClassOfAxiomSize(s)));
		}
		sb.append("\nComplex Class Axioms:\n");
		for (OWLSubClassOfAxiom s : complexClassAxioms)
		{
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n", s.toString(), getSubClassOfAxiomSize(s)));
		}
		sb.append("\nRole Axioms:\n");
		for (OWLPropertyAxiom s : roleAxioms)
		{
			sb.append(String.format("\t%s\n\tAxiom Size: %d\n\n", s.toString(), getObjectPropertyAxiomSize(s)));
		}
		return sb.toString();
	}

	/**
	 * Gets the Role axioms (includes data property axioms)
	 * 
	 * @return axioms ArrayList<OWLPropertyAxiom>
	 */
	public ArrayList<OWLPropertyAxiom> getRoleAxioms()
	{
		return roleAxioms;
	}

	/**
	 * Gets all simple class Axioms that are shorter than 3 terms
	 * 
	 * @return axioms ArrayList<OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getSimpleClassAxioms()
	{
		return classAxioms;
	}

	/**
	 * Gets all complex class Axioms that are longer than 3 terms
	 * 
	 * @return axioms ArrayList<OWLSubClassOfAxiom>
	 */
	public ArrayList<OWLSubClassOfAxiom> getComplexClassAxioms()
	{
		return complexClassAxioms;
	}

}
