AsciiDoctor cheat sheet creation for Dash
=========================================

An opinionated AsciiDoctor cheat sheet for the web author and tooling to turn it [into a docset for Dash for macOS](https://github.com/Kapeli/cheatset#readme).

Based partially on https://powerman.name/doc/asciidoc .

## Transformation steps

    docker run --rm -it -v (pwd):/documents/ asciidoctor/docker-asciidoctor asciidoctor web-cheatsheet.adoc

