# Gedcom-RestoreIds

When you import a GEDCOM file into an application, and then export it,
often it changes every ID in the file. This application helps to restore
the IDs to their original values.

Feed it the original file and the updated file, and this application will
use the REFN values it each file to match the records with each other, and
then restore the IDs in the updated file to the IDs from the matching
records in the original file.
