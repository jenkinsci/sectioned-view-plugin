package hudson.plugins.sectioned_view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Messages;
import hudson.model.TopLevelItem;
import hudson.model.Node.Mode;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.DescriptorList;
import hudson.util.EnumConverter;

public abstract class SectionedViewSection implements ExtensionPoint, Describable<SectionedViewSection> {

    public SectionedViewSection(String name, Width width, Positioning alignment) {
        this.name = name;
        this.width = width;
        this.alignment = alignment;
        determineCss();
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
	
	private Width width;
	
	private Positioning alignment;
	
	transient String css;

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

    public boolean contains(TopLevelItem item) {
		return jobNames.contains(item.getName());
	}

	protected Object readResolve() {
		if (includeRegex != null)
			includePattern = Pattern.compile(includeRegex);
		if (width == null)
		    width = Width.FULL;
		if (alignment == null)
		    alignment = Positioning.CENTER;
		determineCss();
		return this;
	}

	private void determineCss() {
	    final StringBuffer css = new StringBuffer();
	    css.append(width.getCss());
	    css.append(alignment.getCss());
        if (width == Width.FULL || alignment == Positioning.CENTER) {
            css.append("clear: both; ");
        } else if (alignment == Positioning.LEFT) {
            css.append("clear: left; ");
        } else if (alignment == Positioning.RIGHT) {
            css.append("clear: right; ");
        }
        this.css = css.toString();
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

    public String getCss() {
        return css;
    }

    /**
     * Constants that control how a Section is positioned.
     */
    public enum Positioning {
        CENTER("Center", "margin-left: auto; margin-right: auto; "),
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