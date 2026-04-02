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
import hudson.util.EnumConverter;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;

public class TextSection extends SectionedViewSection {

    private String text;

    private Style style;

    /**
     * @deprecated since 1.8 use {@link #TextSection(String, Width, Positioning, String, Style)} instead
     */
    @Deprecated
    public TextSection(String name, Width width, Positioning alignment, String text) {
        this(name, width, alignment, text, Style.NONE);
    }

    @DataBoundConstructor
    public TextSection(String name, Width width, Positioning alignment, String text, Style style) {
        super(name, width, alignment);
        this.text = text;
        this.style = style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Style getStyle() {
        return style;
    }

    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public SectionedViewSection newInstance(StaplerRequest2 req, JSONObject formData) throws FormException {
            return (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);
        }

        @Override
        public String getDisplayName() {
            return "Text Section";
        }
    }

    /**
     * Constants that control how a Text Section is styled.
     */
    public enum Style {
        NONE("None", ""),
        NOTE("Note", "jenkins-alert-warning"),
        INFO("Info", "jenkins-alert-info"),
        WARN("Warning", "jenkins-alert-danger"),
        TIP("Tip", "jenkins-alert-success");

        private final String description;

        private final String cssClass;

        public String getDescription() {
            return description;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getName() {
            return name();
        }

        Style(String description, String cssClass) {
            this.description = description;
            this.cssClass = cssClass;
        }

        static {
            Stapler.CONVERT_UTILS.register(new EnumConverter(), Style.class);
        }
    }
}
