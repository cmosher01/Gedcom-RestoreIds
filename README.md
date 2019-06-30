# Gedcom-RestoreIds


Copyright © 2017, Christopher Alan Mosher, Shelton, Connecticut, USA, <cmosher01@gmail.com>.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)
[![License](https://img.shields.io/github/license/cmosher01/Gedcom-RestoreIds.svg)](https://www.gnu.org/licenses/gpl.html)

When you import a GEDCOM file into an application, and then export it,
often it changes every ID in the file. This application helps to restore
the IDs to their original values.

Feed it the original file and the updated file, and this application will
use the REFN values it each file to match the records with each other, and
then restore the IDs in the updated file to the IDs from the matching
records in the original file.
