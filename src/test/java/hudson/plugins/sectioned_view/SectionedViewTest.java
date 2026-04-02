package hudson.plugins.sectioned_view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.util.DescribableList;
import java.util.Arrays;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

public class SectionedViewTest {

    public @Rule JenkinsRule j = new JenkinsRule();

    @Test @Issue("JENKINS-37174")
    public void doNotEnumerateItemsRepeatedly() throws Exception {
        FreeStyleProject show = j.createFreeStyleProject();
        FreeStyleProject hide = j.createFreeStyleProject();

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
            section.setIncludeRegex(".*" + show.getName() + ".*");
        }

        JenkinsRule.WebClient wc = j.createWebClient();
        String content = wc.getPage(j.jenkins).getWebResponse().getContentAsString();
        assertThat(content, containsString(show.getName()));
        assertThat(content, containsString(hide.getName()));

        content = wc.getPage(sw).getWebResponse().getContentAsString();

        assertThat(content, containsString(show.getName()));
        assertThat(content, not(containsString(hide.getName())));
        assertThat(content, containsString("jgs"));
        assertThat(content, containsString("lvs"));
        assertThat(content, containsString("trvs"));

        verify(jgs, times(1)).getItems(any(ItemGroup.class));
        verify(lvs, times(1)).getItems(any(ItemGroup.class));
        verify(trvs, times(1)).getItems(any(ItemGroup.class));
    }

    @Test @Issue("JENKINS-44987")
    public void htmlUI() throws Exception {
        String MARKUP = "<div><b><a href=\"adsf\" rel=\"nofollow noopener noreferrer\">LVS</a></b></div>";

        j.jenkins.setMarkupFormatter(new RawHtmlMarkupFormatter(false));

        SectionedView sw = new SectionedView("SW");
        j.jenkins.addView(sw);
        DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>> sections = (DescribableList<SectionedViewSection, Descriptor<SectionedViewSection>>) sw.getSections();
        sections.add(new ListViewSection(MARKUP, SectionedViewSection.Width.THIRD, SectionedViewSection.Positioning.CENTER));

        JenkinsRule.WebClient wc = j.createWebClient();
        String content = wc.getPage(sw).getWebResponse().getContentAsString();
        assertThat(content, containsString(MARKUP));
    }

    @Test @Issue("JENKINS-58418")
    public void jobWithDotInNameCollision() throws Exception {
        j.createFreeStyleProject("foo.bar");
        j.createFreeStyleProject("foo_bar");
        SectionedView sw = new SectionedView("sw");
        j.jenkins.addView(sw);
        sw.setSections(Arrays.asList(
                new JobGraphsSection("JobGraph", SectionedViewSection.Width.FULL, SectionedViewSection.Positioning.CENTER),
                new ListViewSection("List", SectionedViewSection.Width.FULL, SectionedViewSection.Positioning.CENTER)
        ));

        JenkinsRule.WebClient wc = j.createWebClient();
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);

        HtmlPage configPage = wc.getPage(sw, "configure");
        HtmlForm form = configPage.getFormByName("viewConfig");
        HtmlButton saveButton = j.getButtonByCaption(form,"Save");
        HtmlPage responsePage = saveButton.click();

        assertThat(responsePage.getWebResponse().getStatusCode(), is(200));
    }
}

