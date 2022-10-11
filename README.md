AsciiDoctor cheat sheet creation for Dash
=========================================

An opinionated AsciiDoctor cheat sheet for the web author and tooling to turn it [into a docset for Dash for macOS](https://github.com/Kapeli/cheatset#readme).

Based partially on https://powerman.name/doc/asciidoc .

## Transformation steps

Prerequisites: [Babashka](https://babashka.org), Docker.

    # 1. .adoc -> HTML:
    docker run --rm -it -v (pwd):/documents/ asciidoctor/docker-asciidoctor \
      asciidoctor web-cheatsheet.adoc
    # 2. HTML -> raw docset & and the docset generator AsciiDoctor.rb
    bb html2dash.bb

There are two ways to get the docset into Dash:

1. Locally, you can manually add the created docset directory `./AsciiDoctor.docset/` This is preferable as the formatting and index are richer
2. From the generated docset generator `AsciiDoctor.rb` (see below) - this is how the docset is created by Dash itself, when I update https://github.com/Kapeli/cheatsheets/blob/master/cheatsheets/AsciiDoctor.rb[Kapeli/cheatsheets/../AsciiDoctor.rb].

Simulating how Dash produces a cheatsheet docset using https://github.com/Kapeli/cheatset/[cheatset]:

    # 3. .rb -> docset
    docker run --rm  -it --volume $PWD:/tmp --name cheatset jonasbn/cheatset:latest generate AsciiDoctor.rb

## Creating a release

Tag the code (`git tag 2.0.16/v2`), push (`git push; git push --tags`). On versioning: the version is `<asciidoctor version>/<cheatsheet version>`

Create a release manually from the pushed tag (even though GH will create a release automatically from the tag, you need to also create it manually so that our workflow kicks in) - _Draft release_, type the new tag name, …​ .

A GH action kicks in that builds the docset .zip and other artifacts and attaches it to the release.

Finally **submit a PR to update Kapeli/cheatsheets/../AsciiDoctor.rb**.


