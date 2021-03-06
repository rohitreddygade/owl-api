/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.semanticweb.owlapi.model;

import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.RemoveOntologyAnnotationData;

/** Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 24-Mar-2009 Represents an ontology change where an annotation is
 * removed from an ontology. */
public class RemoveOntologyAnnotation extends AnnotationChange {

    /**
     * @param ont the ontology to which the change is to be applied
     * @param annotation the annotation
     */
    public RemoveOntologyAnnotation(OWLOntology ont,
                                    OWLAnnotation annotation) {
        super(ont, annotation);
    }

    @Override
    public OWLOntologyChangeData getChangeData() {
        return new RemoveOntologyAnnotationData(getAnnotation());
    }

    @Override
	public void accept(OWLOntologyChangeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <O> O accept(OWLOntologyChangeVisitorEx<O> visitor) {
    	return visitor.visit(this);
    }

    @Override
	public int hashCode() {
        return 23 + getOntology().hashCode() + getAnnotation().hashCode();
    }

    @Override
	public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof RemoveOntologyAnnotation)) {
            return false;
        }
        RemoveOntologyAnnotation other = (RemoveOntologyAnnotation) obj;
        return getAnnotation().equals(other.getAnnotation()) && getOntology().equals(other.getOntology());
    }

    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RemoveOntologyAnnotation(");
        sb.append(getAnnotation().toString());
        sb.append(" OntologyID(");
        sb.append(getOntology().getOntologyID());
        sb.append(")");
        sb.append(")");
        return sb.toString();
    }
}
