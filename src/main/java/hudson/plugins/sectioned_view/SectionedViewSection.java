package hudson.plugins.sectioned_view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.DescriptorList;

public abstract class SectionedViewSection implements ExtensionPoint, Describable<SectionedViewSection> {

    public SectionedViewSection(String name) {
        this.name = name;
    }
    
    /**
     * List of job names. This is what gets serialized.
     */
    /*package*/ final SortedSet<String> jobNames = new TreeSet<String>(CaseInsensitiveComparator.INSTANCE);
    
    private String name;

	/**
	 * Include regex string.
	 */
	String includeRegex;

	/**
	 * Compiled include pattern from the includeRegex string.
	 */
	transient Pattern includePattern;

    /**
     * Returns all the registered {@link SectionedViewSection} descriptors.
     */
    public static DescriptorExtensionList<SectionedViewSection, SectionedViewSectionDescriptor> all() {
    	return Hudson.getInstance().getDescriptorList(SectionedViewSection.class);
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIncludeRegex() {
		return includeRegex;
	}

	public boolean contains(TopLevelItem item) {
		return jobNames.contains(item.getName());
	}

	protected Object readResolve() {
		if (includeRegex != null)
			includePattern = Pattern.compile(includeRegex);
		return this;
	}

	public Collection<TopLevelItem> getItems() {
        SortedSet<String> names = new TreeSet<String>(jobNames);

        if (includePattern != null) {
            for (TopLevelItem item : Hudson.getInstance().getItems()) {
                String itemName = item.getName();
                if (includePattern.matcher(itemName).matches()) {
                    names.add(itemName);
                }
            }
        }

        List<TopLevelItem> items = new ArrayList<TopLevelItem>(names.size());
        for (String n : names) {
            TopLevelItem item = Hudson.getInstance().getItem(n);
            if(item!=null)
                items.add(item);
        }
        return items;
	}
	
    public SectionedViewSectionDescriptor getDescriptor() {
        return (SectionedViewSectionDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

}