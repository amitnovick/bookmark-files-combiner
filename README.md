# bookmark-files-combiner

A script that takes PDFs, bookmark metadata files in YAML corresponding to these PDFs and an output directory
It will then combine the PDFs, and apply the bookmark metadata to it

## Usage

```bash
$ bb src/bookmark_files_combiner/core.clj /path/to/pdf-files/ /path/to/bookmark-files/ /path/to/output-boomark-file/
```

## Requirements

- PDFTK (tested on my Ubuntu 22.04 LTS system with the pdftk port to java, version 3.2.2)
- Babashka (tested with v1.3.176)

## License

Copyright © 2023 Amit Novick

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
