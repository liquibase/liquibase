package liquibase.database.structure;

public class Sequence implements Comparable<Sequence> {
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int compareTo(Sequence o) {
        return this.getName().compareTo(o.getName());
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sequence sequence = (Sequence) o;

        return !(name != null ? !name.equals(sequence.name) : sequence.name != null);

    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }
}
