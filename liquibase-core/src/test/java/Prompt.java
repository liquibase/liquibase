import liquibase.Scope;
import liquibase.ui.ConsoleDelegate;
import liquibase.ui.ConsoleUIService;
import liquibase.ui.UIService;

import java.io.*;
import static java.lang.Thread.sleep;

public class Prompt {
    public static void main(String[] args) {
      UIService service = new ConsoleUIService();
        try {
           String input = service.prompt("Enter a value", "Yes", 10, new ConsoleDelegate());
           System.out.println(input);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
