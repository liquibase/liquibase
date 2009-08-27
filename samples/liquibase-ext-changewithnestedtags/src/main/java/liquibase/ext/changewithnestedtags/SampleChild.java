package liquibase.ext.changewithnestedtags;

public class SampleChild {
    private String name;
    private SampleGrandChild grandChild;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SampleGrandChild createFarNested() {
        grandChild = new SampleGrandChild();
        return grandChild;
    }

    public SampleGrandChild getGrandChild() {
        return grandChild;
    }
}
