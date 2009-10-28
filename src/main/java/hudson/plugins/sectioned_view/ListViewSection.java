package hudson.plugins.sectioned_view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ListViewSection extends SectionedViewSection {

    private DescribableList<ListViewColumn, Descriptor<ListViewColumn>> columns;

    // First add all the known instances in the correct order:
    private static final Descriptor [] defaultColumnDescriptors  =  {
        StatusColumn.DESCRIPTOR,
        WeatherColumn.DESCRIPTOR,
        JobColumn.DESCRIPTOR,
        LastSuccessColumn.DESCRIPTOR,
        LastFailureColumn.DESCRIPTOR,
        LastDurationColumn.DESCRIPTOR,
        BuildButtonColumn.DESCRIPTOR
    };

    @DataBoundConstructor
    public ListViewSection(String name, Width width, Positioning alignment) {
        super(name, width, alignment);
        initColumns();
    }

    protected Object readResolve() {
        super.readResolve();
        initColumns();
        return this;
    }

    protected void initColumns() {
        if (columns != null) {
            // already persisted
            return;
        }
        // OK, set up default list of columns:
        // create all instances
        ArrayList<ListViewColumn> r = new ArrayList<ListViewColumn>();
        DescriptorExtensionList<ListViewColumn, Descriptor<ListViewColumn>> all = ListViewColumn.all();
        ArrayList<Descriptor<ListViewColumn>> left = new ArrayList<Descriptor<ListViewColumn>>();
        left.addAll(all);
        for (Descriptor d: defaultColumnDescriptors) {
            Descriptor<ListViewColumn> des = all.find(d.getClass().getName());
            if (des  != null) {
                try {
                    r.add (des.newInstance(null, null));
                   left.remove(des);
                } catch (FormException e) {
                    // so far impossible. TODO: report
                }
                
            }
        }
        for (Descriptor<ListViewColumn> d : left)
            try {
                r.add(d.newInstance(null,null));
            } catch (FormException e) {
                // so far impossible. TODO: report
            }
        Iterator<ListViewColumn> filter = r.iterator();
        while (filter.hasNext()) {
            if (!filter.next().shownByDefault()) {
                filter.remove();
            }
        }
        columns = new DescribableList<ListViewColumn, Descriptor<ListViewColumn>>(Saveable.NOOP);
        try {
            columns.replaceBy(r);
        } catch (IOException ex) {
            Logger.getLogger(ListViewSection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Iterable<ListViewColumn> getColumns() {
        return columns;
    }
    
    public static List<ListViewColumn> getDefaultColumns() {
        ArrayList<ListViewColumn> r = new ArrayList<ListViewColumn>();
        DescriptorExtensionList<ListViewColumn, Descriptor<ListViewColumn>> all = ListViewColumn.all();
        for (Descriptor d: defaultColumnDescriptors) {
            Descriptor<ListViewColumn> des = all.find(d.getClass().getName());
            if (des  != null) {
                try {
                    r.add (des.newInstance(null, null));
                } catch (FormException e) {
                    // so far impossible. TODO: report
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
            section.columns.rebuildHetero(req, formData, Hudson.getInstance().getDescriptorList(ListViewColumn.class), "columns");
            
            return section;
        }

        @Override
        public String getDisplayName() {
            return "List View Section";
        }
    }
}
