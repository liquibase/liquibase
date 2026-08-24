package liquibase.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ObjectMethodsTest {

  @Test
  void readMethods() {
    ObjectMethods objectMethods = new ObjectMethods(User.class);
    assertThat(objectMethods.getReadMethod("name").getName()).isEqualTo("getName");
    assertThat(objectMethods.getReadMethod("age").getName()).isEqualTo("getAge");
    assertThat(objectMethods.getReadMethod("human").getName()).isEqualTo("isHuman");
    assertThat(objectMethods.getReadMethod("gender")).isNull();
  }

  @Test
  void writeMethods() {
    ObjectMethods objectMethods = new ObjectMethods(User.class);
    assertThat(objectMethods.getWriteMethod("name")).isNull();
    assertThat(objectMethods.getWriteMethod("age").getName()).isEqualTo("setAge");
    assertThat(objectMethods.getWriteMethod("human")).isNull();
  }

  @Test
  void writeMethodsPreserveUppercaseAcronyms() {
    ObjectMethods objectMethods = new ObjectMethods(Acronyms.class);
    // Uppercase acronyms (first two chars both uppercase) must be kept as the property name so that
    // <param name="GET">/<param name="URL"> resolve to setGET/setURL (issue #7582, regression since 4.29.0).
    assertThat(objectMethods.getWriteMethod("GET").getName()).isEqualTo("setGET");
    assertThat(objectMethods.getWriteMethod("URL").getName()).isEqualTo("setURL");
    // Regular camelCase names are still lowercased as before.
    assertThat(objectMethods.getWriteMethod("name").getName()).isEqualTo("setName");
  }

  static class User {
    private final String name;
    private int age;

    User(String name) {this.name = name;}

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public boolean isHuman() {
      return true;
    }
  }

  static class Acronyms {
    public void setGET(String value) {
    }

    public void setURL(String value) {
    }

    public void setName(String value) {
    }
  }
}
