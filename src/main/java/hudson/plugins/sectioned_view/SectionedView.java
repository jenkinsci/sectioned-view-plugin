package hudson.plugins.sectioned_view;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.HealthReport;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Saveable;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.model.Descriptor.FormException;
import hudson.util.DescribableList;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class SectionedView extends View {

	private DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>> sections;

	public Iterable<SectionedViewSection> getSections() {
		return sections;
	}

	@DataBoundConstructor
	public SectionedView(String name) {
		super(name);
		initSections();
	}

	protected void initSections() {
		if (sections != null) {
			// already persisted
			return;
		}

		sections = new DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>>(
				Saveable.NOOP);
	}

	public static Collection<Job> sortJobs(Collection<Job> jobs) {
		TreeSet<Job> set = new TreeSet<Job>(new JobComparator());
		if (jobs != null) {
			set.addAll(jobs);
		}
		return set;
	}

	public static boolean isBuilding(Job job) {
		Run lastBuild = job.getLastBuild();
		return lastBuild != null
				&& (lastBuild.isLogUpdated() || lastBuild.isBuilding());
	}

	private static final class JobComparator implements Comparator<Job> {
		public int compare(Job o1, Job o2) {
			// first compare by status
			Result r1 = getResult(o1);
			Result r2 = getResult(o2);
			if (r1 != null && r2 != null) {
				if (r1.isBetterThan(r2)) {
					return 1;
				} else if (r1.isWorseThan(r2)) {
					return -1;
				}
			}

			HealthReport h1 = o1.getBuildHealth();
			HealthReport h2 = o2.getBuildHealth();
			if (h1 != null && h2 != null) {
				// second compare by stability
				int health = h1.compareTo(h2);
				if (health != 0) {
					return health;
				}
			}

			// finally compare by name
			return o1.getName().compareTo(o2.getName());
		}
	}

	public static Result getResult(Job job) {
		Run lastBuild = job.getLastBuild();
		while (lastBuild != null
				&& (lastBuild.hasntStartedYet() || lastBuild.isBuilding() || lastBuild
						.isLogUpdated())) {
			lastBuild = lastBuild.getPreviousBuild();
		}
		if (lastBuild != null) {
			return lastBuild.getResult();
		} else {
			return Result.NOT_BUILT;
		}
	}

	/**
	 * Handles the configuration submission.
	 * 
	 * Load view-specific properties here.
	 */
	@Override
	protected void submit(StaplerRequest req) throws ServletException,
			FormException {
		if (sections == null) {
			sections = new DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>>(
					Saveable.NOOP);
		}
		sections.rebuildHetero(req, req.getSubmittedForm(), Hudson
				.getInstance().getDescriptorList(SectionedViewSection.class),
				"sections");
	}

	@Extension
	public static final class DescriptorImpl extends ViewDescriptor {

		@Override
		public String getDisplayName() {
			return "Sectioned View";
		}

		/**
		 * Checks if the include regular expression is valid.
		 */
		public FormValidation doCheckIncludeRegex(@QueryParameter String value)
				throws IOException, ServletException, InterruptedException {
			String v = Util.fixEmpty(value);
			if (v != null) {
				try {
					Pattern.compile(v);
				} catch (PatternSyntaxException pse) {
					return FormValidation.error(pse.getMessage());
				}
			}
			return FormValidation.ok();
		}
	}

	@Override
	public boolean contains(TopLevelItem item) {
		for (SectionedViewSection section : sections) {
			if (section.contains(item)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Item doCreateItem(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException {
        return Hudson.getInstance().doCreateItem(req, rsp);
	}

	@Override
	public Collection<TopLevelItem> getItems() {
		SortedSet<String> names = new TreeSet<String>();

		for (SectionedViewSection section : sections) {
			names.addAll(section.jobNames);
		}

		List<TopLevelItem> items = new ArrayList<TopLevelItem>(names.size());
		for (String n : names) {
			TopLevelItem item = Hudson.getInstance().getItem(n);
			if (item != null)
				items.add(item);
		}
		return items;
	}

	@Override
	public void onJobRenamed(Item item, String oldName, String newName) {
		for (SectionedViewSection section : sections) {
	        if(section.jobNames.remove(oldName) && newName!=null)
	        	section.jobNames.add(newName);
		}
	}
}
