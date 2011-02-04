/*
 * The MIT License
 *
 * Copyright (c) 2009-2011, Timothy Bingaman
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

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Saveable;
import hudson.model.Descriptor.FormException;
import hudson.util.DescribableList;
import hudson.views.BuildButtonColumn;
import hudson.views.JobColumn;
import hudson.views.LastDurationColumn;
import hudson.views.LastFailureColumn;
import hudson.views.LastSuccessColumn;
import hudson.views.ListViewColumn;
import hudson.views.StatusColumn;
import hudson.views.WeatherColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ListViewSection extends SectionedViewSection {

    private DescribableList<ListViewColumn, Descriptor<ListViewColumn>> columns;

    @DataBoundConstructor
    public ListViewSection(String name, Width width, Positioning alignment) {
        super(name, width, alignment);
    }

    public Iterable<ListViewColumn> getColumns() {
        return columns;
    }

    public static List<ListViewColumn> getDefaultColumns() {
        ArrayList<ListViewColumn> r = new ArrayList<ListViewColumn>();
        DescriptorExtensionList<ListViewColumn, Descriptor<ListViewColumn>> all = ListViewColumn.all();

        for (Class<? extends ListViewColumn> d: DEFAULT_COLUMNS) {
            Descriptor<ListViewColumn> des = all.find(d);
            if (des  != null) {
                try {
                    r.add(des.newInstance(null, null));
                } catch (FormException e) {
                    LOGGER.log(Level.WARNING, "Failed to instantiate "+des.clazz,e);
                }
            }
        }

        return Collections.unmodifiableList(r);
    }

    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            ListViewSection section = (ListViewSection) super.newInstance(req, formData);

            if (section.columns == null) {
                section.columns = new DescribableList<ListViewColumn,Descriptor<ListViewColumn>>(Saveable.NOOP);
            }
            try {
                section.columns.rebuildHetero(req, formData, Hudson.getInstance().<ListViewColumn,Descriptor<ListViewColumn>>getDescriptorList(ListViewColumn.class), "columns");
            } catch (IOException e) {
                throw new FormException("Error rebuilding list of columns.", e, "columns");
            }

            return section;
        }

        @Override
        public String getDisplayName() {
            return "List View Section";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ListViewSection.class.getName());

    /**
     * Traditional column layout before the {@link ListViewColumn} becomes extensible.
     */
    @SuppressWarnings("unchecked")
    private static final List<Class<? extends ListViewColumn>> DEFAULT_COLUMNS =  Arrays.asList(
        StatusColumn.class,
        WeatherColumn.class,
        JobColumn.class,
        LastSuccessColumn.class,
        LastFailureColumn.class,
        LastDurationColumn.class,
        BuildButtonColumn.class
    );
}
