package uk.ac.manchester.cs.owl.inference.dig11;

import org.semanticweb.owlapi.inference.OWLReasonerAdapter;
import org.semanticweb.owlapi.inference.UnsupportedReasonerOperationException;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.Document;

import java.util.*;
import java.util.logging.Logger;
/*
 * Copyright (C) 2006, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 21-Nov-2006<br><br>
 * <p/>
 * A DIG reasoner supports queries about classes and individual.  This particular implementation
 * does not support role queries, because the current version of DIG (1.1) is lacking with respect
 * to role query functionality (e.g. domain/range and role characteristic queries are not supported).
 */
public class DIGReasoner extends OWLReasonerAdapter {

    private static final Logger logger = Logger.getLogger(DIGReasoner.class.getName());

    private HTTPReasoner reasoner;

    private DIGTranslator translator;

    private String kbURI;

    private boolean resynchronize;

    /**
     * Creates a reasoner that is backed by a DIG server.
     * @param owlOntologyManager The <code>OWLOntologyManager</code> that should be
     *                           used to obtain imported ontologies.
     */
    public DIGReasoner(OWLOntologyManager owlOntologyManager) throws OWLException {
        super(owlOntologyManager);
        resynchronize = true;
        reasoner = new HTTPReasonerImpl(owlOntologyManager);
        translator = new DIGTranslatorImpl(owlOntologyManager);
        reasoner.setReasonerURL(DIGReasonerPreferences.getInstance().getReasonerURL());
    }


    /**
     * Gets the HTTPReasoner that backs this DIG reasoner.
     */
    public HTTPReasoner getReasoner() {
        return reasoner;
    }


    /**
     * Gets the translator that translates back and forth between DIG documents
     * and OWLAPI objects.
     */
    public DIGTranslator getTranslator() {
        return translator;
    }


    /**
     * Gets the DIG server kb URI that this reasoner uses as a
     * handle.
     */
    public String getKbURI() {
        return kbURI;
    }


    public boolean isClassified() {
        return !resynchronize;
    }


    public void classify() throws OWLReasonerException {
        synchroniseReasoner();
    }


    public boolean isRealised() throws OWLReasonerException {
        return !resynchronize;
    }


    public void realise() throws OWLReasonerException {
        synchroniseReasoner();
    }


    /**
     * This method should be called to dispose of the reasoner.
     * In terms of the external DIG reasoner, this method simply
     * releases the knowledge base corresponding to this reasoner.
     */
    public void disposeReasoner() {
        releaseCurrentKB();
    }


    protected void ontologiesCleared() {
        resynchronize = true;
    }


    protected void ontologiesChanged() {
        resynchronize = true;
    }


    protected void handleOntologyChanges(List<OWLOntologyChange> changes) {
        resynchronize = true;
    }

    /**
     * Releases the current kb.
     */
    private void releaseCurrentKB() {
        try {
            if (kbURI != null) {
                reasoner.releaseKnowledgeBase(kbURI);
                kbURI = null;
            }
        }
        catch (DIGReasonerException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean isReasonerSynchonised() {
        return !resynchronize && kbURI != null;
    }


    /**
     * This method synchronises the reasoner if necessary.
     */
    protected void synchroniseReasoner() throws DIGReasonerException {
        if(isReasonerSynchonised()) {
            return;
        }
        if (kbURI == null) {
            kbURI = reasoner.createKnowledgeBase();
        }
        logger.info("Synchronizing reasoner...");
        translator = new DIGTranslatorImpl(getOWLOntologyManager());
        Document doc = translator.createTellsDocument(kbURI);
        // Clear KB
        reasoner.clearKnowledgeBase(kbURI);
        DIGRenderer ren = new DIGRenderer(getOWLOntologyManager(), doc, doc.getDocumentElement());
            for (OWLOntology ont : getLoadedOntologies()) {
                ont.accept(ren);
            }
        reasoner.performRequest(doc);
        logger.info("... synchronized.");
        resynchronize = false;
    }


    public boolean isDefined(OWLClass cls) throws OWLReasonerException {
        return true;
    }


    public boolean isDefined(OWLObjectProperty prop) throws OWLReasonerException {
        return true;
    }


    public boolean isDefined(OWLDataProperty prop) throws OWLReasonerException {
        return true;
    }


    public boolean isDefined(OWLIndividual ind) throws OWLReasonerException {
        return true;
    }


    /**
     * Performs a request using the DIG HTTP Reasoner.  This method
     * ensures that the reasoner has been synchronised before the
     * request is made.
     * @param doc The Request DIG Document
     * @return The response from the reasoner.
     */
    protected Document performRequest(Document doc)throws DIGReasonerException {
        // Ensure that the reasoner is synchronised.
        synchroniseReasoner();
        return reasoner.performRequest(doc);
    }


    /**
     * A convenience method that creates an asks document using the
     * current translator
     */
    private Document createAsksDocument()throws DIGReasonerException {
        synchroniseReasoner();
        return translator.createAsksDocument(kbURI);
    }


    /**
     * A convenience method to get a query response iterator.
     */
    private Iterator<DIGQueryResponse> getIterator(Document doc)throws DIGReasonerException {
        return translator.getDIGQueryResponseIterator(getOWLDataFactory(), doc);
    }


    /**
     * Converts a concept set response to OWLAPI ojects.
     */
    private static Set<Set<OWLClass>> toClassSet(Iterator<DIGQueryResponse> it)throws DIGReasonerException {
        Set<Set<OWLClass>> result = new HashSet<Set<OWLClass>>();
        while (it.hasNext()) {
            result.addAll(it.next().getConceptSets());
        }
        return result;
    }

    private static Set<Set<OWLObjectProperty>> toObjectPropertySet(Iterator<DIGQueryResponse> it)throws DIGReasonerException {
        Set<Set<OWLObjectProperty>> result = new HashSet<Set<OWLObjectProperty>>();
        while(it.hasNext()) {
            result.addAll(it.next().getRoleSets());
        }
        return result;
    }




    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Reasoner interface


    public boolean isConsistent(OWLOntology ontology)throws OWLReasonerException {
        synchroniseReasoner();
        return isSatisfiable(getOWLOntologyManager().getOWLDataFactory().getOWLThing());
    }

    public boolean isSatisfiable(OWLClassExpression cls)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createSatisfiableQuery(doc, "q0", cls);
        return getIterator(performRequest(doc)).next().getBoolean();
    }


    public boolean isSubClassOf(OWLClassExpression clsC, OWLClassExpression clsD)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createSubsumesQuery(doc, "q0", clsD, clsC);
        return getIterator(performRequest(doc)).next().getBoolean();
    }


    public boolean isEquivalentClass(OWLClassExpression clsC, OWLClassExpression clsD)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createSubsumesQuery(doc, "q0", clsC, clsD);
        translator.createSubsumesQuery(doc, "q1", clsD, clsC);
        Iterator<DIGQueryResponse> it = getIterator(performRequest(doc));
        return it.next().getBoolean() && it.next().getBoolean();
    }


    public Set<Set<OWLClass>> getSuperClasses(OWLClassExpression classExpression)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createDirectSuperConceptsQuery(doc, "q0", classExpression);
        return toClassSet(getIterator(performRequest(doc)));
    }


    public Set<Set<OWLClass>> getAncestorClasses(OWLClassExpression classExpression)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createAncestorConceptsQuery(doc, "q0", classExpression);
        return toClassSet(getIterator(performRequest(doc)));
    }


    public Set<Set<OWLClass>> getSubClasses(OWLClassExpression classExpression)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createDirectSubConceptsQuery(doc, "q0", classExpression);
        return toClassSet(getIterator(performRequest(doc)));
    }


    public Set<Set<OWLClass>> getDescendantClasses(OWLClassExpression classExpression)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createDescendantConceptsQuery(doc, "q0", classExpression);
        return toClassSet(getIterator(performRequest(doc)));
    }


    public Set<OWLClass> getEquivalentClasses(OWLClassExpression classExpression)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createEquivalentConceptsQuery(doc, "q0", classExpression);
        Iterator<DIGQueryResponse> it = getIterator(performRequest(doc));
        Set<OWLClass> results = new HashSet<OWLClass>();
        while(it.hasNext()) {
            for(Set<OWLClass> clsSet : it.next().getConceptSets()) {
                results.addAll(clsSet);
            }
        }
        return results;
    }


    /**
     * A convenience methods for obtaining all classes which are inconsistent.
     * @return A set of classes which are inconsistent.
     */
    public Set<OWLClass> getUnsatisfiableClasses()throws OWLReasonerException {
        // No need to sync - the getOWLEquivalentClassesAxiom method will do this
        return getEquivalentClasses(getOWLOntologyManager().getOWLDataFactory().getOWLNothing());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Individual stuff
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public Set<Set<OWLClass>> getTypes(OWLNamedIndividual individual, boolean direct)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createIndividualTypesQuery(doc, "q0", individual);
        return toClassSet(getIterator(performRequest(doc)));
    }


    public Set<OWLNamedIndividual> getIndividuals(OWLClassExpression clsC, boolean direct)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createInstancesOfConceptQuery(doc, "q0", clsC);
        return getIterator(performRequest(doc)).next().getIndividuals();
    }


    public Map<OWLObjectProperty, Set<OWLNamedIndividual>> getObjectPropertyRelationships(OWLNamedIndividual individual)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Map<OWLDataProperty, Set<OWLLiteral>> getDataPropertyRelationships(OWLNamedIndividual individual)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<OWLNamedIndividual> getRelatedIndividuals(OWLNamedIndividual subject, OWLObjectPropertyExpression property) throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<OWLLiteral> getRelatedValues(OWLNamedIndividual subject, OWLDataPropertyExpression property) throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean hasDataPropertyRelationship(OWLNamedIndividual subject, OWLDataPropertyExpression property,
                                               OWLLiteral object)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean hasObjectPropertyRelationship(OWLNamedIndividual subject, OWLObjectPropertyExpression property,
                                                 OWLNamedIndividual object)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean hasType(OWLNamedIndividual individual, OWLClassExpression type, boolean direct)throws OWLReasonerException {
        return flattenSetOfSets(getTypes(individual, direct)).contains(type);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Property stuff
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////


    public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createDirectSuperPropertiesQuery(doc, "q0", property);
        return toObjectPropertySet(getIterator(performRequest(doc)));
    }


    public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createDirectSubPropertiesQuery(doc, "q0", property);
        return toObjectPropertySet(getIterator(performRequest(doc)));
    }


    public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createAncestorPropertiesQuery(doc, "q0", property);
        return toObjectPropertySet(getIterator(performRequest(doc)));
    }


    public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property)throws OWLReasonerException {
        synchroniseReasoner();
        Document doc = createAsksDocument();
        translator.createDescendantPropertiesQuery(doc, "q0", property);
        return toObjectPropertySet(getIterator(performRequest(doc)));
    }


    public Set<Set<OWLObjectProperty>> getInverseProperties(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property)throws OWLReasonerException {
        Set<OWLObjectProperty> ancestors = flattenSetOfSets(getAncestorProperties(property));
        Set<OWLObjectProperty> descendants = flattenSetOfSets(getDescendantProperties(property));
        ancestors.retainAll(descendants);
        return ancestors;
    }


    public Set<Set<OWLClassExpression>> getDomains(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<OWLClassExpression> getRanges(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isFunctional(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isInverseFunctional(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isSymmetric(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isTransitive(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isReflexive(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isIrreflexive(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isAsymmetric(OWLObjectProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<Set<OWLClassExpression>> getDomains(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public Set<OWLDataRange> getRanges(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }


    public boolean isFunctional(OWLDataProperty property)throws OWLReasonerException {
        throw new UnsupportedReasonerOperationException();
    }
}