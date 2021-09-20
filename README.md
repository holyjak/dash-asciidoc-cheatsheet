AsciiDoctor cheat sheet creation for Dash
=========================================

Run:

    docker compose up

Browse the result:

    cd AsciiDoctor.docset/Contents/Resources/Documents
    python3 -m http.server 8001
    # browse to http://localhost:8001

See https://github.com/Kapeli/cheatset#readme

Based on https://powerman.name/doc/asciidoc, https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/, and https://tomd.xyz/asciidoctor-cheatsheet/

## Log - Clojure

```
git clone https://github.com/powerman/asciidoc-cheatsheet.git
cd asciidoc-cheatsheet
docker run --rm -it -v (pwd):/documents/ asciidoctor/docker-asciidoctor asciidoctor full.adoc

bb html2dash.bb
docker compose up
```

