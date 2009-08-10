package liquibase.ext.sample3;

public class Sample3Child {
    private String name;
    private Sample3GrandChild grandChild;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sample3GrandChild createSample3Grand() {
        grandChild = new Sample3GrandChild();
        return grandChild;
    }

    public Sample3GrandChild getGrandChild() {
        return grandChild;
    }
}
