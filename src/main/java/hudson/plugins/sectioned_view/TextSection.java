package hudson.plugins.sectioned_view;

import hudson.Extension;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class TextSection extends SectionedViewSection {
    
    private String text;

    @DataBoundConstructor
    public TextSection(String name, Width width, Positioning alignment, String text) {
        super(name, width, alignment);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);
        }

        @Override
        public String getDisplayName() {
            return "Text Section";
        }
    }
}
