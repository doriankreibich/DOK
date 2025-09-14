package com.example.dok.config;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MarkdownConfig {

    @Bean
    public Parser parser() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create(),
                WikiLinkExtension.create(),
                AutolinkExtension.create(),
                InsExtension.create()
        ));
        options.set(WikiLinkExtension.LINK_FIRST_SYNTAX, true);
        return Parser.builder(options).build();
    }

    @Bean
    public HtmlRenderer renderer(Parser parser) {
        MutableDataSet options = new MutableDataSet(parser.getOptions());
        options.set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "");
        return HtmlRenderer.builder(options).build();
    }
}
