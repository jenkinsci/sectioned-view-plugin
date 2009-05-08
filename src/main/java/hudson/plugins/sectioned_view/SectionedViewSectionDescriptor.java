package hudson.plugins.sectioned_view;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;

public abstract class SectionedViewSectionDescriptor extends Descriptor<SectionedViewSection> {

	protected SectionedViewSectionDescriptor(Class<? extends SectionedViewSection> clazz) {
		super(clazz);
	}

	protected SectionedViewSectionDescriptor() {
	}

	@Override
	public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
		SectionedViewSection section = (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);
		section.jobNames.clear();
		for (TopLevelItem item : Hudson.getInstance().getItems()) {
			String escapedName = item.getName().replaceAll("\\.", "_");
			if (formData.containsKey(escapedName) && formData.getBoolean(escapedName))
				section.jobNames.add(item.getName());
		}
		return section;
	}

}
