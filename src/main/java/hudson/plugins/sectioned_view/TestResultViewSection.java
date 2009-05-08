package hudson.plugins.sectioned_view;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class TestResultViewSection extends SectionedViewSection {

    @DataBoundConstructor
    public TestResultViewSection(String name) {
        super(name);
    }
	
	public Descriptor<SectionedViewSection> getDescriptor() {
		return DESCRIPTOR;
	}
    
    public static final Descriptor<SectionedViewSection> DESCRIPTOR = new DescriptorImpl();

    @Extension
    public static class DescriptorImpl extends Descriptor<SectionedViewSection> {
        @Override
        public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        	SectionedViewSection section = req.bindJSON(TestResultViewSection.class, formData);
        	section.jobNames.clear();
            for (TopLevelItem item : Hudson.getInstance().getItems()) {
                if(formData.getBoolean(item.getName()))
                	section.jobNames.add(item.getName());
            }
            return section;
        }

        @Override
        public String getDisplayName() {
            return "Test Result Section";
        }
    }
}
