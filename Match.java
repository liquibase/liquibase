public class Match {
  public static void main(String[] args) {
    String string = "My Table  ";
//             "[\\[?]My Table  [\\]?]"
    String string1 = "\"My Table \"";
    String string2 = "{My Table}";

    //System.out.println(string.matches("\\[My Table  \\]"));
    System.out.println(string.matches("[\\[]?My Table  [\\]]?"));

    System.out.println(string.matches("[\\[?]My Table  [\\]?]"));
    System.out.println(string1.matches("\"[\\[]*My Table [\\]]*\""));
  }
}
