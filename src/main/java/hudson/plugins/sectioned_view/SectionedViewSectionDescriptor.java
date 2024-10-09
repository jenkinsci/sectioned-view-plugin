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

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import hudson.model.Items;
import hudson.model.TopLevelItem;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Saveable;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import hudson.views.ViewJobFilter;
import jenkins.model.Jenkins;

public abstract class SectionedViewSectionDescriptor extends Descriptor<SectionedViewSection> {

    protected SectionedViewSectionDescriptor(Class<? extends SectionedViewSection> clazz) {
        super(clazz);
    }

    protected SectionedViewSectionDescriptor() {
    }

    public boolean hasJobFilterExtensions() {
        return !ViewJobFilter.all().isEmpty();
    }

    @Override
    public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        SectionedViewSection section = (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);

        if (formData.get("useincluderegex") != null) {
            JSONObject merp = formData.getJSONObject("useincluderegex");
            try {
                section.setIncludeRegex(Util.nullify(merp.getString("includeRegex")));
            } catch (PatternSyntaxException e) {
                throw new FormException("Regular expression is invalid: " + e.getMessage(), e, "includeRegex");
            }
            section.setExecutingRegexOnAllJobs(merp.getBoolean("executingRegexOnAllJobs"));
        } else {
            section.includeRegex = null;
            section.includePattern = null;
        }

        section.jobNames.clear();
        ItemGroup<?> group = req.findAncestorObject(ItemGroup.class);
        if (group == null) {
            group = Jenkins.getInstance();
        }

        for (TopLevelItem item : Items.getAllItems(group, TopLevelItem.class)) {
            String escapedName = escapeJobName(item.getRelativeNameFrom(group));
            if (formData.containsKey(escapedName) && formData.getBoolean(escapedName))
                section.jobNames.add(item.getRelativeNameFrom(group));
        }

        if (section.jobFilters == null) {
            section.jobFilters = new DescribableList<ViewJobFilter,Descriptor<ViewJobFilter>>(Saveable.NOOP);
        }
        try {
            section.jobFilters.rebuildHetero(req, formData, ViewJobFilter.all(), "jobFilters");
        } catch (IOException e) {
            throw new FormException("Error rebuilding list of view job filters.", e, "jobFilters");
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

    /**
     * Utility method which escapes job names so that they can be safely
     * inserted into the name field of HTML inputs. Escaping is needed since
     * job names may contain characters which have special meaning in name
     * fields of HTML inputs. For example the dot (.) character, in a name
     * field Jenkins input handling (Stapler) will treat the dot as an
     * hierarchical separation and split the name on the dot, which in this
     * case is unwanted. So in order to mitigate that dots are replaces with a
     * character which are disallowed in job names and have no special meaning
     * in input fields, e.g. hash tag (#).
     *
     * @param name Job name to be escaped before insertion into HTML input name
     * @return Escaped job name
     */
    public static String escapeJobName(String name) {
        // For a list of forbidden job name characters, see jenkins.model.Jenkins#checkGoodName()
        return name.replaceAll("\\.", "#");
    }

}
