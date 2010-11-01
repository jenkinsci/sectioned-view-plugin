package hudson.plugins.sectioned_view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.View;
import hudson.model.ViewGroup;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class ViewListingSection extends SectionedViewSection {

    private List<String> views;
    
    private int columns;

    @DataBoundConstructor
    public ViewListingSection(String name, Width width, Positioning alignment, List<String> views, int columns) {
        super(name, width, alignment);
        this.setViews(views);
        this.setColumns(columns);
    }
    
    public void setViews(List<String> views) {
        this.views = views;
    }

    public List<String> getViews() {
        return views;
    }

    /**
     * Used for generating the config UI.
     */
    public String getViewsString() {
        char delim = ',';
        // Build string connected with delimiter, quoting as needed
        StringBuilder buf = new StringBuilder();
        for (String value : views)
            buf.append(delim).append(value);
        return buf.substring(1);
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getColumns() {
        return columns;
    }

    public List<Collection<View>> getNestedViewColumns() {
        List<Collection<View>> nestedViewColumns = new ArrayList<Collection<View>>();
        for (int i = 0; i < columns; i++) {
            nestedViewColumns.add(new ArrayList<View>());
        }
        int column = 0;
        for (String viewName : views) {
            String[] viewComponents = viewName.split("\\$");
            View view = Hudson.getInstance().getView(viewComponents[0]);
            if (view == null) continue;
            boolean skipView = false;
            for (int i = 1; i < viewComponents.length; i++) {
                if (view instanceof ViewGroup) {
                    view = ((ViewGroup) view).getView(viewComponents[i]);
                    if (view == null) {
                        skipView = true;
                        break;
                    }
                } else {
                    skipView = true;
                    break;
                }
            }
            if (skipView) continue;
            nestedViewColumns.get(column % columns).add(view);
            column++;
        }
        return nestedViewColumns;
    }

    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            int columns = formData.getInt("columns");
            if (columns <= 0) throw new FormException("Columns must be a number greater than 0", "columns");
            return (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);
        }

        @Override
        public String getDisplayName() {
            return "View Listing Section";
        }
        
        public FormValidation doCheckColumns(@QueryParameter String value) {
            if (StringUtils.isNotEmpty(value) && StringUtils.isNumeric(value)) {
                int columns = Integer.parseInt(value);
                if (columns > 0) return FormValidation.ok();
            }
            return FormValidation.error("Columns must be a number greater than 0");
        }
    }
}
