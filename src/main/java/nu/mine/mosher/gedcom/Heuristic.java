package nu.mine.mosher.gedcom;

import java.util.*;

import static nu.mine.mosher.logging.Jul.log;

class Heuristic {
    private final GedcomDataRef ref;
    private final Map<String, String> mapNewIdToHeuristic = new HashMap<>(4096, 1.0f);
    private final Map<String, String> mapHeuristicToOldId = new HashMap<>(4096, 1.0f);
    private final Set<String> dups = new HashSet<>();

    public Heuristic(final GedcomDataRef ref) {
        this.ref = ref;
    }

    public GedcomDataRef getRef() {
        return this.ref;
    }

    public boolean put(final String id, final String h, final boolean old) {
        if (h.isEmpty() || dup(h, old)) {
            return false;
        }
        if (old) {
            this.mapHeuristicToOldId.put(h, id);
        } else {
            this.mapNewIdToHeuristic.put(id, h);
        }
        return true;
    }

    private boolean dup(final String h, final boolean old) {
        if (!old) {
            // assume IDs are always unique
            return false;
        }
        if (this.mapHeuristicToOldId.containsKey(h)) {
            this.mapHeuristicToOldId.remove(h);
            this.dups.add(h);
            log().warning("Found duplicate value, ignoring: " + h);
        }
        return this.dups.contains(h);
    }

    public String find(final String idNew) {
        final String h = this.mapNewIdToHeuristic.get(idNew);
        if (h == null || h.isEmpty()) {
            log().fine("Cannot find value for ID in new file: " + idNew);
            return "";
        }

        final String idOld = this.mapHeuristicToOldId.get(h);
        if (idOld == null || idOld.isEmpty()) {
            log().fine("Cannot find match for value in old file: " + h + " for ID " + idNew);
            return "";
        }

        log().finer("Found matching old ID (" + idOld + ") for new ID (" + idNew + ")");

        return idOld;
    }
}
