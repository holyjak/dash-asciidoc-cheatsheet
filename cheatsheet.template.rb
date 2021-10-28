cheatsheet do
  title 'AsciiDoctor'
  docset_file_name 'AsciiDoctor'
  keyword 'adoc'
  # resources 'resources_dir'  # An optional resources folder which can contain images or anything else
  style '
    div.name p {font-weight: bold}
    .title {
      font-size: 1.1em;
      margin-bottom: 0.5em;
      color: #527bbd;
      font-weight: 400;
    }
    .admon td:nth-child(1) { width: 6em; }
    .underline {text-decoration: underline}
    .line-through {text-decoration: line-through}
    .exampleblock > .content {
      background: #fffef7;
      border: 1px solid #e0e0dc;
      box-shadow: 0 1px 4px #e0e0dc;
      padding: 0.75em;
      border-radius: 4px;
    }
  '

  introduction '<p>On opinionated AsciiDoctor cheat sheet for authoring HTML, by 
  <a onclick="window.dash.openExternal_(this.href); return false;" href="https://holyjak.cz">Jakub Holý</a></p>'

  {% for category in categories %}
  category do
    id '{{category.0}}'

    {% for entry in category.1 %}
    entry do
      name '{{entry.0}}'
      {% for command in entry.2 %}
      command '{{command}}'
      {% endfor %}
      notes <<-'HTMLEND'
        {{entry.1|safe}}
      HTMLEND
    end
    {% endfor %}
  end
  {% endfor %}

  notes '<a onclick="window.dash.openExternal_(this.href); return false;" href=\'https://github.com/holyjak/dash-asciidoc-cheatsheet\'>Open cheat sheet source</a>'
end