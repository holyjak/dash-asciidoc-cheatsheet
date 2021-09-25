AsciiDoctor cheat sheet creation for Dash
=========================================

An opinionated AsciiDoctor cheat sheet for the web author and tooling to turn it [into a docset for Dash for macOS](https://github.com/Kapeli/cheatset#readme).

Based partially on https://powerman.name/doc/asciidoc .

## Transformation steps

Prerequisites: [Babashka](https://babashka.org), Docker.

    # 1. .adoc -> HTML:
    docker run --rm -it -v (pwd):/documents/ asciidoctor/docker-asciidoctor \
      asciidoctor web-cheatsheet.adoc
    # 2. HTML -> docset
    bb html2dash.bb

## TODO

* Links to entries from docset ToC do not work, cats. ok
* Improve styling, copy the necessary asciidoc style minimum (e.g. to make %autowidth working).
* Does intra link ' see "Links and anchors" below' work?

* Add to ToC and/or index selected intra-entry stuff, such as "Macros" under Extra
  (simple version: 1. hickory to find all such inside an entry, 2. add idx pointing to the parent entry)