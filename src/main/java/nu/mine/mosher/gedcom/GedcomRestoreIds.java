package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.mopper.ArgParser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static nu.mine.mosher.logging.Jul.log;
import static nu.mine.mosher.logging.Jul.thrown;

// Created by Christopher Alan Mosher on 2017-08-27

public class GedcomRestoreIds implements Gedcom.Processor {
    private final GedcomRestoreIdsOptions options;
    private GedcomTree tree;
    private final Map<String, String> mapNewIdToRefn = new HashMap<>(4096);
    private final Map<String, String> mapRefnToOldId = new HashMap<>(4096);

    public static void main(final String... args) throws InvalidLevel, IOException {
        log();
        final GedcomRestoreIdsOptions options = new ArgParser<>(new GedcomRestoreIdsOptions()).parse(args).verify();
        new Gedcom(options, new GedcomRestoreIds(options)).main();
        System.out.flush();
        System.err.flush();
    }

    private GedcomRestoreIds(final GedcomRestoreIdsOptions options) {
        this.options = options;
    }

    @Override
    public boolean process(final GedcomTree tree) {
        this.tree = tree;
        try {
            getOldIds();
            getNewIds();
            remapIds(this.tree.getRoot());
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
        return true;
    }

    private void getNewIds() {
        tree.getRoot().forEach(n -> readRefnAndId(n, false));
        log().info("Loaded "+Integer.toString(this.mapNewIdToRefn.size())+" REFNs from new GEDCOM");
    }

    private void getOldIds() throws IOException, InvalidLevel {
        final GedcomTree source = Gedcom.readFile(new BufferedInputStream(new FileInputStream(this.options.source)));
        new GedcomConcatenator(source).concatenate();
        source.getRoot().forEach(n -> readRefnAndId(n, true));
        log().info("Loaded "+Integer.toString(this.mapRefnToOldId.size())+" REFNs from "+this.options.source.getCanonicalPath());
    }

    private void readRefnAndId(final TreeNode<GedcomLine> nodeTop, final boolean old) {
        final GedcomLine lineTop = nodeTop.getObject();
        if (!lineTop.hasID()) {
            return;
        }

        final TreeNode<GedcomLine> nodeRefn = findChildNode(nodeTop, GedcomTag.REFN);
        if (nodeRefn == null) {
            log().warning("Could not find REFN for: "+lineTop);
            return;
        }

        final String refn = nodeRefn.getObject().getValue();
        if (refn.isEmpty()) {
            log().warning("REFN is empty, for : "+lineTop);
            return;
        }

        if (old && this.mapRefnToOldId.containsKey(refn)) {
            log().warning("Duplicate REFN found: "+refn);
            return;
        }

        if (old) {
            this.mapRefnToOldId.put(refn, lineTop.getID());
        } else {
            this.mapNewIdToRefn.put(lineTop.getID(), refn);
        }
    }

    private void remapIds(final TreeNode<GedcomLine> node) {
        node.forEach(this::remapIds);

        final GedcomLine gedcomLine = node.getObject();
        if (gedcomLine == null) {
            return;
        }

        if (gedcomLine.hasID()) {
            final String refn = this.mapNewIdToRefn.get(gedcomLine.getID());
            if (refn == null) {
                log().warning("Cannot find REFN for ID in new file: "+gedcomLine);
            } else {
                final String idOld = this.mapRefnToOldId.get(refn);
                if (idOld == null) {
                    log().warning("Cannot find REFN for ID in old file: "+refn);
                } else {
                    node.setObject(new GedcomLine(gedcomLine.getLevel(), "@"+idOld+"@", gedcomLine.getTagString(), gedcomLine.getValue()));
                }
            }
        } else if (gedcomLine.isPointer()) {
            final String refn = this.mapNewIdToRefn.get(gedcomLine.getPointer());
            if (refn == null) {
                log().warning("Cannot find REFN for ID in new file: "+gedcomLine);
            } else {
                final String idOld = this.mapRefnToOldId.get(refn);
                if (idOld == null) {
                    log().warning("Cannot find REFN for ID in old file: " + refn);
                } else {
                    // assume that no line with a pointer also has an ID (true in GEDCOM 5.5.1)
                    node.setObject(new GedcomLine(gedcomLine.getLevel(), "", gedcomLine.getTagString(), "@" + idOld + "@"));
                }
            }
        }
    }

    private static TreeNode<GedcomLine> findChildNode(final TreeNode<GedcomLine> nodeParent, final GedcomTag tagChild) {
        for (final TreeNode<GedcomLine> c : nodeParent) {
            if (c.getObject().getTag().equals(tagChild)) {
                return c;
            }
        }
        return null;
    }
}
