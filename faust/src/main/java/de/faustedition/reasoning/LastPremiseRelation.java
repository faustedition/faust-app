/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.reasoning;

import java.util.Collection;

public class LastPremiseRelation<E>  extends PremiseBasedRelation<E>  {

	
	public LastPremiseRelation(Premise<E>... premises) {
		super(premises);
	}
	public LastPremiseRelation(Collection<? extends Premise<E>> c) {
		super(c);
	}

	@Override
	public boolean areRelated(E subject, E object) {
		for (int i = 0; i < this.size() - 1; i++) {
			if (this.get(i).applies(subject, object)
					|| this.get(i).applies(object, subject)) 
				return false;
		}
		return this.get(this.size() - 1).applies(subject, object);
	}

}
