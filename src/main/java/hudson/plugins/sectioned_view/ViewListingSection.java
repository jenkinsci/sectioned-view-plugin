/*
 * The MIT License
 *
 * Copyright (c) 2010-2011, Timothy Bingaman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.sectioned_view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.Extension;
import hudson.model.View;
import hudson.model.ViewGroup;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest2;

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

    public boolean isChecked(String viewName) {
        return views.contains(viewName);
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getColumns() {
        return columns;
    }

    public List<Collection<View>> getNestedViewColumns(ViewGroup viewGroup) {
        List<Collection<View>> nestedViewColumns = new ArrayList<Collection<View>>();
        for (int i = 0; i < columns; i++) {
            nestedViewColumns.add(new ArrayList<View>());
        }
        
        if (views == null || views.isEmpty()) return nestedViewColumns;
        
        int column = 0;
        for (String viewName : views) {
            String[] viewComponents = viewName.split("\\$");
            View view = viewGroup.getView(viewComponents[0]);
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
        public SectionedViewSection newInstance(StaplerRequest2 req, JSONObject formData) throws FormException {
            int columns = formData.getInt("columns");
            if (columns <= 0) throw new FormException("Columns must be a number greater than 0", "columns");
            return (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);
        }

        @Override
        public String getDisplayName() {
            return "View Listing Section";
        }
        
    }
}
