package liquibase.license;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LicenseTrackList {
    private final List<LicenseTrack> licenseTracks = new ArrayList<>();
}
