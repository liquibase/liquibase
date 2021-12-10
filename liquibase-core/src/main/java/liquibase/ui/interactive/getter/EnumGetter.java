package liquibase.ui.interactive.getter;

import liquibase.ui.interactive.AbstractCommandLineValueGetter;

public class EnumGetter<E extends Enum> extends AbstractCommandLineValueGetter<E> {

    private final Class<E> e;

    public EnumGetter(Class<E> e){
        super(e);
        this.e = e;
    }

    @Override
    public boolean validate(E input) {
        return true;
    }

    @Override
    public E convert(String input) {
        return (E) Enum.valueOf(e, input);
    }

}
