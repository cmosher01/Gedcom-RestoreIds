package nu.mine.mosher.gedcom;

import java.io.*;
import java.util.*;

@SuppressWarnings({"access", "WeakerAccess", "unused"})
public class GedcomRestoreIdsOptions extends GedcomOptions {
    public File source;
    public List<GedcomDataRef> refs = new ArrayList<>(2);

    public void help() {
        this.help = true;
        System.err.println("Usage: java -jar gedcom-restoreids-all.jar [OPTIONS] <in.ged -g source.ged >out.ged");
        System.err.println("Restores IDs in in.ged GEDCOM file to those in source.ged file.");
        System.err.println("Options:");
        System.err.println("-g, --gedcom=FILE    GEDCOM file to extract from.");
        System.err.println("-w, --where=EXPR     Values to match on. For example, '.REPO.NAME'");
        System.err.println("                     restores REPO IDs based on their NAME.");
        System.err.println("                     May be specified multiple times, and will be");
        System.err.println("                     processed in order of precedence.");
        options();
    }

    public void g(final String file) throws IOException {
        gedcom(file);
    }

    public void gedcom(final String file) throws IOException {
        this.source = new File(file);
        if (!this.source.canRead()) {
            throw new IllegalArgumentException("Cannot read GEDCOM input file: " + this.source.getCanonicalPath());
        }
    }

    public void w(final String expr) throws GedcomDataRef.InvalidSyntax {
        where(expr);
    }

    public void where(final String expr) throws GedcomDataRef.InvalidSyntax {
        this.refs.add(new GedcomDataRef(expr));
    }

    public GedcomRestoreIdsOptions verify() {
        if (this.help) {
            return this;
        }
        if (this.source == null) {
            throw new IllegalArgumentException("Missing required GEDCOM input file.");
        }
        if (this.refs.isEmpty()) {
            throw new IllegalArgumentException("Missing -w (for example -w '.*.REFN')");
        }
        return this;
    }
}
