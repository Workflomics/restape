package nl.esciencecenter.controller.dto;

public class ApeTaxTuple {
    private final String id;
    private final String label;

    public ApeTaxTuple(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
}
