package entities.distribution;

import util.Constants;

public abstract class ImplicitDistribution extends ExceedableDistribution{
    public ImplicitDistribution() {
        required = true;
        penalty = Constants.BIG_M;
    }
}
