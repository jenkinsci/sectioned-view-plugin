package hudson.plugins.sectioned_view;

import hudson.Extension;
import hudson.util.EnumConverter;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

public class TextSection extends SectionedViewSection {

    private String text;

    private Style style;

    /**
     * @deprecated since 1.8 use {@link #TextSection(String, Width, Positioning, String, Style)} instead
     */
    @Deprecated
    public TextSection(String name, Width width, Positioning alignment, String text) {
        this(name, width, alignment, text, Style.NONE);
    }

    @DataBoundConstructor
    public TextSection(String name, Width width, Positioning alignment, String text, Style style) {
        super(name, width, alignment);
        this.text = text;
        this.style = style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Style getStyle() {
        return style;
    }

    public boolean hasStyle() {
        return style != Style.NONE;
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

    /**
     * Constants that control how a Text Section is styled.
     */
    public enum Style {
        NONE("None", ""),
        NOTE("Note", "sectioned-view-note"),
        INFO("Info", "sectioned-view-info"),
        WARN("Warning", "sectioned-view-warning"),
        TIP("Tip", "sectioned-view-tip");

        private final String description;

        private final String cssClass;

        public String getDescription() {
            return description;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getName() {
            return name();
        }

        Style(String description, String cssClass) {
            this.description = description;
            this.cssClass = cssClass;
        }

        static {
            Stapler.CONVERT_UTILS.register(new EnumConverter(), Style.class);
        }
    }
}
