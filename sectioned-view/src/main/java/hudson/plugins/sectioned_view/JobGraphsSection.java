package hudson.plugins.sectioned_view;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class JobGraphsSection extends SectionedViewSection {

    @DataBoundConstructor
    public JobGraphsSection(String name, Width width, Positioning alignment) {
        super(name, width, alignment);
    }

    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public String getDisplayName() {
            return "Job Graphs Section";
        }
    }
}
