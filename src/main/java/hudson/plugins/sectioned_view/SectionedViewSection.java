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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.Saveable;
import hudson.model.TopLevelItem;
import hudson.util.DescribableList;
import hudson.util.EnumConverter;
import hudson.views.ViewJobFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;


public abstract class SectionedViewSection implements ExtensionPoint, Describable<SectionedViewSection> {

    public SectionedViewSection(String name, Width width, Positioning alignment) {
        this.name = name;
        this.width = width;
        this.alignment = alignment;
        determineCss();
        initJobFilters();
    }

    /**
     * List of job names. This is what gets serialized.
     */
    /*package*/ final SortedSet<String> jobNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    /*package*/ DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>> jobFilters;

    private String name;

    /**
     * Include regex string.
     */
    String includeRegex;
    
    /**
     * execute regex on all jobs
     */
    
    boolean executingRegexOnAllJobs;

    /**
     * Compiled include pattern from the includeRegex string.
     */
    transient Pattern includePattern;

    private Width width;

    private Positioning alignment;

    transient String css;

    /**
     * Returns all the registered {@link SectionedViewSection} descriptors.
     */
    public static DescriptorExtensionList<SectionedViewSection, SectionedViewSectionDescriptor> all() {
        return Hudson.getInstance().<SectionedViewSection, SectionedViewSectionDescriptor>getDescriptorList(SectionedViewSection.class);
    }

    public @NonNull String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIncludeRegex() {
        return includeRegex;
    }

    public void setIncludeRegex(String regex) throws PatternSyntaxException {
        includeRegex = regex;
        includePattern = Pattern.compile(regex);
    }

    public boolean isExecutingRegexOnAllJobs() {
        return executingRegexOnAllJobs;
    }
    
    public void setExecutingRegexOnAllJobs(boolean value) {
        this.executingRegexOnAllJobs = value;
    }
    
    public Iterable<ViewJobFilter> getJobFilters() {
        return jobFilters;
    }

    /**
     * @deprecated use {@link SectionedViewSectionDescriptor#hasJobFilterExtensions()} instead
     */
    @Deprecated
    public boolean hasJobFilterExtensions() {
        return getDescriptor().hasJobFilterExtensions();
    }

    public Width getWidth() {
        return width;
    }

    public void setWidth(Width width) {
        this.width = width;
    }

    public Positioning getAlignment() {
        return alignment;
    }

    public void setAlignment(Positioning alignment) {
        this.alignment = alignment;
    }

    public boolean contains(TopLevelItem item, ItemGroup<? extends TopLevelItem> itemGroup) {
        return jobNames.contains(item.getRelativeNameFrom(itemGroup));
    }

    protected Object readResolve() {
        if (includeRegex != null)
            setIncludeRegex(includeRegex);
        if (width == null)
            width = Width.FULL;
        if (alignment == null)
            alignment = Positioning.CENTER;
        determineCss();
        initJobFilters();
        return this;
    }

    private void determineCss() {
        final StringBuffer css = new StringBuffer();

        css.append(alignment.getCss());
        if (width == Width.THIRD && alignment == Positioning.CENTER) {
            css.append("width: 34%; "); // the center part takes 34% so 33% + 34% + 33% = 100%
        }
        else {
            css.append(width.getCss());
        }
        if (width == Width.FULL) {
            css.append("clear: both; ");
        } else if (alignment == Positioning.LEFT) {
            css.append("clear: left; ");
        } else if (alignment == Positioning.RIGHT || alignment == Positioning.CENTER) {
            css.append("clear: right; ");
        }
        this.css = css.toString();
    }

    public Collection<TopLevelItem> getItems(ItemGroup<? extends TopLevelItem> itemGroup) {
        SortedSet<String> names = new TreeSet<String>(jobNames);

        Collection<? extends TopLevelItem> topLevelItems = null;
        if(executingRegexOnAllJobs) {
            topLevelItems = Items.getAllItems(itemGroup, TopLevelItem.class);
        } else {
            topLevelItems = itemGroup.getItems();
        }
        
        if (includePattern != null) {
            for (TopLevelItem item : topLevelItems) {
                String itemName = item.getRelativeNameFrom(itemGroup);
                if (includePattern.matcher(itemName).matches()) {
                    names.add(itemName);
                }
            }
        }

        List<TopLevelItem> items = new ArrayList<TopLevelItem>(names.size());
        for (String n : names) {
            Jenkins instance = Jenkins.getInstance();
            if (instance != null) {
                TopLevelItem item = instance.getItem(n, itemGroup, TopLevelItem.class);
                if (item != null)
                    items.add(item);
            }
        }

        // check the filters
        Iterable<ViewJobFilter> jobFilters = getJobFilters();
        List<TopLevelItem> allItems = new ArrayList<TopLevelItem>(topLevelItems);
        for (ViewJobFilter jobFilter: jobFilters) {
            items = jobFilter.filter(items, allItems, null);
        }
        return items;
    }

    protected void initJobFilters() {
        if (jobFilters != null) {
            return;
        }
        ArrayList<ViewJobFilter> r = new ArrayList<ViewJobFilter>();
        jobFilters = new DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>>(Saveable.NOOP, r);
    }

    public SectionedViewSectionDescriptor getDescriptor() {
        return (SectionedViewSectionDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

    public String getCss() {
        return css;
    }

    /**
     * Constants that control how a Section is positioned.
     */
    public enum Positioning {
        CENTER("Center", "margin-left: auto; margin-right: auto; float: left;"),
        LEFT("Left", "float: left; "),
        RIGHT("Right", "float: right; ");

        private final String description;

        private final String css;

        public String getDescription() {
            return description;
        }

        public String getCss() {
            return css;
        }

        public String getName() {
            return name();
        }

        Positioning(String description, String css) {
            this.description = description;
            this.css = css;
        }

        static {
            Stapler.CONVERT_UTILS.register(new EnumConverter(), Positioning.class);
        }
    }

    /**
     * Constants that control how a Section is floated.
     */
    public enum Width {
        FULL("Full", 100, "width: 100%; "),
        HALF("1/2", 50, "width: 50%; "),
        THIRD("1/3", 33, "width: 33%; "),
        TWO_THIRDS("2/3", 66, "width: 66%; ");

        private final String description;

        private final String css;

        private final int percent;

        public String getDescription() {
            return description;
        }

        public String getCss() {
            return css;
        }

        public int getPercent() {
            return percent;
        }

        public String getName() {
            return name();
        }

        Width(String description, int percent, String css) {
            this.description = description;
            this.percent = percent;
            this.css = css;
        }

        static {
            Stapler.CONVERT_UTILS.register(new EnumConverter(), Width.class);
        }
    }
}
