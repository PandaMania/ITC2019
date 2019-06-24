package entities.distribution;

public abstract class ExceedableDistribution extends Distribution{

    protected Integer exceededBy=0;
    protected int nrWeeks;

    @Override
    public int getPenalty() {
        if (required) {
            return penalty * exceededBy;
        }
        if (exceededBy == null) {
            throw new IllegalStateException("Constraint not checked yet");
        } else {
            return (penalty * exceededBy) / nrWeeks;
        }
    }

    public Integer getExceededBy() {
        return exceededBy;
    }
}
