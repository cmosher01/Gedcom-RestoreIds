package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.mopper.ArgParser;

import java.io.*;
import java.util.*;

import static nu.mine.mosher.logging.Jul.log;

// Created by Christopher Alan Mosher on 2017-08-27
// TODO at least log ERROR message if creating a file with duplicate IDs

public class GedcomRestoreIds implements Gedcom.Processor {
    private final GedcomRestoreIdsOptions options;
    private GedcomTree tree;



    private final List<Heuristic> hs = new ArrayList<>(4096);
    private final Set<String> setId = new HashSet<>(4096, 1);



    public static void main(final String... args) throws InvalidLevel, IOException {
        log();
        final GedcomRestoreIdsOptions options = new ArgParser<>(new GedcomRestoreIdsOptions()).parse(args).verify();
        new Gedcom(options, new GedcomRestoreIds(options)).main();
        System.out.flush();
        System.err.flush();
    }



    private GedcomRestoreIds(final GedcomRestoreIdsOptions options) {
        this.options = options;
        this.options.refs.forEach(ref -> this.hs.add(new Heuristic(ref)));
    }



    @Override
    public boolean process(final GedcomTree tree) {
        try {
            this.tree = tree;
            getOldIds();
            getNewIds();
            remapIds();
            return true;
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private void getOldIds() throws IOException, InvalidLevel {
        final GedcomTree source = Gedcom.readFile(new BufferedInputStream(new FileInputStream(this.options.source)));
        new GedcomConcatenator(source).concatenate();
        getIds(source, true);
    }

    private void getNewIds() throws IOException {
        getIds(this.tree, false);
        this.tree.getRoot().forAll(n -> {
            if (n.getObject() != null && n.getObject().hasID()) {
                this.setId.add(n.getObject().getID());
            }
        });
    }

    private void getIds(final GedcomTree tree, final boolean old) throws IOException {
        this.hs.forEach(h -> h.getRef().forEach(tree, n -> put(h, n, old)));
    }


    private void remapIds() {
        this.tree.getRoot().forAll(node -> {
            final GedcomLine gedcomLine = node.getObject();
            if (gedcomLine == null || !gedcomLine.isLink()) {
                return;
            }

            final String idOld = findBestOldIdForNewId(gedcomLine.getLink());
            if (idOld.isEmpty()) {
                return;
            }

            node.setObject(gedcomLine.replaceLink(idOld));
            log().fine("Replacing "+gedcomLine.getLink()+" with "+idOld);
        });
    }

    private String findBestOldIdForNewId(final String idNew) {
        for (final Heuristic h : this.hs) {
            final String idOld = h.find(idNew);
            if (!idOld.isEmpty()) {
                if (idAlreadyExists(idOld)) {
                    return "";
                }
                return idOld;
            }
        }
        return "";
    }

    private boolean idAlreadyExists(final String id) {
        return this.setId.contains(id);
    }

    private static boolean put(final Heuristic h, final TreeNode<GedcomLine> n, final boolean old) {
        return h.put(findContainingRecord(n).getObject().getID(), getValue(n), old);
    }

    private static String getValue(final TreeNode<GedcomLine> n) {
        final GedcomLine line = n.getObject();
        return line.isPointer() ? line.getPointer() : line.getValue();
    }

    private static TreeNode<GedcomLine> findContainingRecord(final TreeNode<GedcomLine> n) {
        if (n.parent().parent() == null) {
            return n;
        }
        return findContainingRecord(n.parent());
    }
}
