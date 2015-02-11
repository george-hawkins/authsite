#
# Extends MarkdownBlock so that the rendered content is wrapped with a <div>
# that makes use of the CSS in css/markdown-body.css.
#
module Jekyll
  class WrappedMarkdownBlock < MarkdownBlock
    def initialize(tag_name, markup, tokens)
      super
    end

    def render(context)
        "<div class=\"markdown-body\">#{super}</div>"
    end
  end
end

Liquid::Template.register_tag('wrappedmarkdown', Jekyll::WrappedMarkdownBlock)
