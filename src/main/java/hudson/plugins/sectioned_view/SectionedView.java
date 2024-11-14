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

import hudson.Extension;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

public class SectionedView extends View {

	private DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>> sections;

	public Iterable<SectionedViewSection> getSections() {
		return sections;
	}

	public void setSections(Collection<? extends SectionedViewSection> sections) {
		this.sections.clear();
		this.sections.addAll(sections);
	}

	@DataBoundConstructor
	public SectionedView(String name) {
		super(name);
		initSections();
	}

	private void initSections() {
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

	private static final class JobComparator implements Comparator<Job>, Serializable {

		private static final long serialVersionUID = 6388545755407223000L;

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
	protected void submit(StaplerRequest2 req) throws ServletException, FormException {
		initSections();
		try {
            sections.rebuildHetero(req, req.getSubmittedForm(), Hudson
            		.getInstance().<SectionedViewSection, Descriptor<SectionedViewSection>>getDescriptorList(SectionedViewSection.class),
            		"sections");
        } catch (IOException e) {
            throw new FormException("Error rebuilding list of sections.", e, "sections");
        }
	}

	@Extension
	public static final class DescriptorImpl extends ViewDescriptor {

		@Override
		public String getDisplayName() {
			return "Sectioned View";
		}
	}

	@Override
	public boolean contains(TopLevelItem item) {
		for (SectionedViewSection section : sections) {
			if (section.contains(item, getOwnerItemGroup())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Item doCreateItem(StaplerRequest2 req, StaplerResponse2 rsp)
			throws IOException, ServletException {
        return Hudson.getInstance().doCreateItem(req, rsp);
	}

	@Override
	public Collection<TopLevelItem> getItems() {
		List<TopLevelItem> items = new ArrayList<TopLevelItem>();

		for (SectionedViewSection section : sections) {
		    items.addAll(section.getItems(getOwnerItemGroup()));
		}

		return items;
	}

	@Override
	public void onJobRenamed(Item item, String oldName, String newName) {
		for (SectionedViewSection section : sections) {
			if (section.jobNames != null && section.jobNames.remove(oldName) && newName!=null) {
				section.jobNames.add(newName);
			}
		}
	}
}
