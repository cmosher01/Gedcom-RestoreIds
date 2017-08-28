package nu.mine.mosher.gedcom;

import java.io.File;
import java.io.IOException;

@SuppressWarnings({"access", "WeakerAccess", "unused"})
public class GedcomRestoreIdsOptions extends GedcomOptions {
    public File source;

    public void help() {
        this.help = true;
        System.err.println("Usage: java -jar gedcom-restoreids-all.jar [OPTIONS] <in.ged -g source.ged >out.ged");
        System.err.println("Restores IDs in in.ged GEDCOM file to those in source.ged file, using REFNs.");
        System.err.println("Options:");
        System.err.println("-g, --gedcom=FILE    GEDCOM file to extract from.");
        options();
    }

    public void g(final String file) throws IOException {
        gedcom(file);
    }

    public void gedcom(final String file) throws IOException {
        this.source = new File(file);
        if (!this.source.canRead()) {
            throw new IllegalArgumentException("Cannot read GEDCOM input file: "+this.source.getCanonicalPath());
        }
    }

    public GedcomRestoreIdsOptions verify() {
        if (this.help) {
            return this;
        }
        if (this.source == null) {
            throw new IllegalArgumentException("Missing required GEDCOM input file.");
        }
        return this;
    }
}
