package entities.distribution;

public abstract class ExceedableDistribution extends Distribution{

    protected Integer exceededBy;
    protected int nrWeeks;

    @Override
    public int getPenalty() {
        if (required) {
            return penalty;
        }
        if (exceededBy == null) {
            throw new IllegalStateException("Constraint not checked yet");
        } else {
            return (penalty * exceededBy) / nrWeeks;
        }
    }
}
