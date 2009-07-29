package hudson.plugins.sectioned_view;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class ListViewSection extends SectionedViewSection {

    @DataBoundConstructor
    public ListViewSection(String name, Width width, Positioning alignment) {
        super(name, width, alignment);
    }

    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public String getDisplayName() {
            return "List View Section";
        }
    }
}
