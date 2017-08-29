package org.semanticweb.owl.explanation.impl.laconic;

import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
/*
 * Copyright (C) 2008, University of Manchester
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
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 15-Sep-2008<br><br>
 */
public class BetaGenerator extends BaseDescriptionGenerator {


    public BetaGenerator(OWLDataFactory factory) {
        super(factory);
    }


    @Override
    public Set<OWLClassExpression> visit(OWLClass desc) {
        Set<OWLClassExpression> descs = new HashSet<>(3);
        descs.add(desc);
        descs.add(getDataFactory().getOWLNothing());
        return descs;
    }


    @Override
    public Set<OWLClassExpression> visit(OWLObjectComplementOf desc) {
        Set<OWLClassExpression> descs = new HashSet<>();
        for(OWLClassExpression d : computeTau(desc.getOperand())) {
            descs.add(getDataFactory().getOWLObjectComplementOf(d));
        }
        return descs;
    }


    protected Set<OWLClassExpression> compute(OWLClassExpression description) {
        return computeBeta(description);
    }


    @Override
    public Set<OWLClassExpression> visit(OWLObjectMaxCardinality desc) {
        // Decrease the cardinality and weaken the filler
        Set<OWLClassExpression> fillers = computeTau(desc.getFiller());
        Set<OWLClassExpression> result = new HashSet<>();
        for (int n = desc.getCardinality(); n > 0; n--) {
            for(OWLClassExpression filler : fillers) {
                result.add(getDataFactory().getOWLObjectMinCardinality(n, desc.getProperty(), filler));
            }
        }
        result.add(getLimit());
        return result;
    }


    @Override
    public Set<OWLClassExpression> visit(OWLObjectExactCardinality desc) {
        Set<OWLClassExpression> fillers = computeBeta(desc.getFiller());
        Set<OWLClassExpression> result = new HashSet<>();
        for(OWLClassExpression filler : fillers) {
            result.add(getDataFactory().getOWLObjectExactCardinality(desc.getCardinality(), desc.getProperty(), filler));
        }
        result.add(getLimit());
        return result;
    }


    @Override
    public Set<OWLClassExpression> visit(OWLObjectUnionOf desc) {
//        // If every disjunct is named, we can just split them
        boolean anon = false;
        for(OWLClassExpression ce : desc.asDisjunctSet()) {
            if(ce.isAnonymous()) {
                anon = true;
                break;
            }
        }
        if(anon) {
            return super.visit(desc);
        }
        else {
            Set<OWLClassExpression> result = new HashSet<>();
            result.addAll(desc.asDisjunctSet());
            return result;
        }

    }


    @Override
    public Set<OWLClassExpression> visit(OWLObjectMinCardinality desc) {
        // Increase the cardinality and weaken filler
        Set<OWLClassExpression> fillers = computeBeta(desc.getFiller());
        Set<OWLClassExpression> result = new HashSet<>();
        for(OWLClassExpression filler : fillers) {
            result.add(getDataFactory().getOWLObjectMinCardinality(desc.getCardinality(), desc.getProperty(), filler));
        }
        result.add(getLimit());
        return result;
    }

    @Override
    protected OWLClass getLimit() {
        return getDataFactory().getOWLNothing();
    }


    @Override
    protected OWLDataRange getDataLimit() {
        return getDataFactory().getOWLDataComplementOf(getDataFactory().getTopDatatype());
    }


    @Override
    public Set<OWLClassExpression> visit(OWLDataHasValue desc) {
        return Collections.singleton((OWLClassExpression) desc);
    }
}
