package hudson.plugins.sectioned_view;

import hudson.Extension;
import hudson.model.PageDecorator;

@Extension
public class SectionedViewPageDecorator extends PageDecorator {
    public SectionedViewPageDecorator() {
        super(SectionedViewPageDecorator.class);
    }
}