package liquibase.logging.mdc.customobjects;

import liquibase.logging.mdc.CustomMdcObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateChangesets implements CustomMdcObject {
    private List<MdcChangeset> duplicateChangesets;
}
