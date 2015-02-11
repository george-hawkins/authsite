Jekyll and Markdown
===================

Commit [`5e0c43dc24`](https://github.com/george-hawkins/authsite/commit/5e0c43dc24) pulls in what I learnt in creating the [basic-gfm-jekyll](https://github.com/george-hawkins/basic-gfm-jekyll) project.

It provides the look seen in GitHub for `.md` content.

Page `_main_pages/page-beta.html` demonstrates combining Bootstrap elements and markdown rendered as `.md` content is on GitHub.

The markdown text on page Beta is copied from `redcarpet-extensions.md` in the basic-gfm-jekyll project.

Layouts
-------

Pages that don't use any markdown should just use the `default` layout.

Pages using markdown should use the new `gfm_page` layout.

Mixing HTML and Markdown
------------------------

Using Liquid you can include blocks of Markdown in an HTML file like so:

    {% capture markdown_block %}
    Markdown in your `HTML` page {{ page.path }}!
    {% endcapture %}
    {{ markdown_block | markdownify }

This can be made even easier with the [Markdown Block](https://github.com/imathis/jekyll-markdown-block) plugin.

Using this the above can be done as:

    {% markdown %}
    Markdown in your `HTML` page {{ page.path }}!
    {% endmarkdown %}

Installing the plugin is trivial:

    $ mkdir -p _plugins
    $ cd _plugins
    $ curl -O https://raw.githubusercontent.com/imathis/jekyll-markdown-block/master/lib/jekyll-markdown-block.rb

I've extended this plugin (see `_plugins/wrapped-markdown-block.rb`) to add another tag `{% wrappedmarkdown %}`.

This simply wraps the generated content with a `<div>` that makes use of the CSS in `css/markdown-body.css`.

Notes
-----

If using GitHub Pages it's important to be aware that it doesn't support providing your own plugins.

This page is called `_README.md` as Jekyll ignores files starting with a `_` (if they aren't part of the standard structure or related to something in `_config.yml`).
