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
    # 3. .rb -> AsciiDoctor.docset
    cp 

## FIXME

The ruby -> docset transf. screws up due to trying to interpret markdown inside HTML:

* Blocks: `<` becomes `&gt;language`
* Source code listing: `<pre>` is somehow lost, becomes `<p>`
* the rest of "Blocks and admonitions" and also "Lists"
* "Escape ..." text 