/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.unistuttgart.xsample.qe.icarus1.match.cs;

import de.unistuttgart.xsample.qe.icarus1.Options;
import de.unistuttgart.xsample.qe.icarus1.match.SearchConstraint;
import de.unistuttgart.xsample.qe.icarus1.match.SearchOperator;
import de.unistuttgart.xsample.qe.icarus1.match.SearchParameters;
import de.unistuttgart.xsample.qe.icarus1.match.TargetTree;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RelationConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "relation"; //$NON-NLS-1$

	public RelationConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SearchParameters.SEARCH_CASESENSITIVE, SearchParameters.DEFAULT_SEARCH_CASESENSITIVE))
			return new RelationConstraint(value, operator);
		else
			return new RelationCIConstraint(value, operator);
	}

	private static class RelationConstraint extends SearchConstraint {

		private static final long serialVersionUID = 1716609613318759367L;

		public RelationConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getRelation();
		}

		@Override
		public RelationConstraint clone() {
			return (RelationConstraint) super.clone();
		}
	}

	private static class RelationCIConstraint extends SearchConstraint.CaseInsensitiveConstraint {

		private static final long serialVersionUID = -3611860983057645172L;

		public RelationCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getRelation().toLowerCase();
		}

		@Override
		public RelationCIConstraint clone() {
			return (RelationCIConstraint) super.clone();
		}
	}
}
