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


NOTE: If using cheatset:

    # 3. .rb -> docset
    docker run --rm  -it --volume $PWD:/tmp --name cheatset jonasbn/cheatset:latest generate AsciiDoctor.rb

## Creating a release

Tag the code (`git tag 2.0.0/optional-custom-marker`), push (`git push; git push --tags`)

Create a release manually from the pushed tag (even though GH will create a release automatically from the tag, you need to also create it manually so that our workflow kicks in) - _Draft release_, type the new tag name, …​ .

A GH action kicks in that builds the docset .zip and attaches it to the release
