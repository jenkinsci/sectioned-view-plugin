package hudson.plugins.sectioned_view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.Descriptor;
import hudson.model.ItemGroup;
import hudson.util.DescribableList;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

public class SectionedViewTest {

    public @Rule JenkinsRule j = new JenkinsRule();

    @Test @Issue("JENKINS-37174")
    public void doNotEnumerateItemsRepeatedly() throws Exception {
        j.createFreeStyleProject("show");
        j.createFreeStyleProject("hide");

        SectionedView sw = new SectionedView("sw");
        j.jenkins.addView(sw);
        DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>> sections = (DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>>) sw.getSections();

        JobGraphsSection jgs = spy(new JobGraphsSection("jgs", SectionedViewSection.Width.THIRD, SectionedViewSection.Positioning.LEFT));
        ListViewSection lvs = spy(new ListViewSection("lvs", SectionedViewSection.Width.THIRD, SectionedViewSection.Positioning.CENTER));
        TestResultViewSection trvs = spy(new TestResultViewSection("trvs", SectionedViewSection.Width.THIRD, SectionedViewSection.Positioning.RIGHT));
        sections.add(jgs);
        sections.add(lvs);
        sections.add(trvs);
        for (SectionedViewSection section : sections) {
            section.setIncludeRegex(".*show.*");
        }

        JenkinsRule.WebClient wc = j.createWebClient();
        String content = wc.getPage(j.jenkins).getWebResponse().getContentAsString();
        assertThat(content, containsString("show"));
        assertThat(content, containsString("hide"));

        content = wc.getPage(sw).getWebResponse().getContentAsString();

        assertThat(content, containsString("show"));
        assertThat(content, not(containsString("hide")));
        assertThat(content, containsString("jgs"));
        assertThat(content, containsString("lvs"));
        assertThat(content, containsString("trvs"));

        verify(jgs, times(1)).getItems(any(ItemGroup.class));
        verify(lvs, times(1)).getItems(any(ItemGroup.class));
        verify(trvs, times(1)).getItems(any(ItemGroup.class));
    }

    @Test @Issue("JENKINS-44987")
    public void htmlUI() throws Exception {
        String MARKUP = "<div><b><a href=\"adsf\">LVS</a></b></div>";

        j.jenkins.setMarkupFormatter(new RawHtmlMarkupFormatter(false));

        SectionedView sw = new SectionedView("SW");
        j.jenkins.addView(sw);
        DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>> sections = (DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>>) sw.getSections();
        sections.add(new ListViewSection(MARKUP, SectionedViewSection.Width.THIRD, SectionedViewSection.Positioning.CENTER));

        JenkinsRule.WebClient wc = j.createWebClient();
        String content = wc.getPage(sw).getWebResponse().getContentAsString();
        assertThat(content, containsString(MARKUP));
    }
}

