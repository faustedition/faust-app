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
