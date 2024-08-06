package liquibase.util;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Getter;
import lombok.Setter;
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

  @Getter
  static class User {
    private final String name;
    @Setter
    private int age;

    User(String name) {this.name = name;}

      public boolean isHuman() {
      return true;
    }
  }
}
