package hudson.plugins.sectioned_view;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;
import hudson.util.FormValidation;

public abstract class SectionedViewSectionDescriptor extends Descriptor<SectionedViewSection> {

	protected SectionedViewSectionDescriptor(Class<? extends SectionedViewSection> clazz) {
		super(clazz);
	}

	protected SectionedViewSectionDescriptor() {
	}

	@Override
	public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
		SectionedViewSection section = (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);

		if (formData.get("useincluderegex") != null) {
			JSONObject merp = formData.getJSONObject("useincluderegex");
			section.includeRegex = Util.nullify(merp.getString("includeRegex"));
			try {
				section.includePattern = Pattern.compile(section.includeRegex);
			} catch (PatternSyntaxException e) {
				throw new FormException("Regular expression is invalid: " + e.getMessage(), e, "includeRegex");
			}
		} else {
			section.includeRegex = null;
			section.includePattern = null;
		}
		
		section.jobNames.clear();
		for (TopLevelItem item : Hudson.getInstance().getItems()) {
			String escapedName = item.getName().replaceAll("\\.", "_");
			if (formData.containsKey(escapedName) && formData.getBoolean(escapedName))
				section.jobNames.add(item.getName());
		}
		return section;
	}

    /**
     * Checks if the include regular expression is valid.
     */
    public FormValidation doCheckIncludeRegex( @QueryParameter String value ) throws IOException, ServletException, InterruptedException  {
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
