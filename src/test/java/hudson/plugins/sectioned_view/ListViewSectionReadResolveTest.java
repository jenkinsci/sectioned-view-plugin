package hudson.plugins.sectioned_view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import hudson.plugins.sectioned_view.SectionedViewSection.Positioning;
import hudson.plugins.sectioned_view.SectionedViewSection.Width;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ListViewSectionReadResolveTest {

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    @Issue("JENKINS-59551")
    void readResolveRestoresTransientState() throws Exception {
        ListViewSection original = new ListViewSection("lvs", Width.THIRD, Positioning.CENTER);
        original.setIncludeRegex(".*foo.*");

        String xml = Jenkins.XSTREAM2.toXML(original);
        ListViewSection loaded = (ListViewSection) Jenkins.XSTREAM2.fromXML(xml);

        assertThat(loaded.getIncludeRegex(), is(".*foo.*"));
        assertThat("css must be recomputed on load", loaded.getCss(), notNullValue());
        assertThat("includePattern must be recompiled on load", loaded.includePattern, notNullValue());
    }

    @Test
    @Issue("JENKINS-59551")
    void legacyConfigWithoutJobFiltersDoesNotBreakGetItems() throws Exception {
        ListViewSection original = new ListViewSection("lvs", Width.THIRD, Positioning.CENTER);
        String xml = Jenkins.XSTREAM2.toXML(original);
        String legacyXml = xml.replaceFirst("(?s)<jobFilters>.*?</jobFilters>|<jobFilters/>", "");
        assertThat("setup: jobFilters element must be stripped", legacyXml, not(containsString("jobFilters")));

        ListViewSection loaded = (ListViewSection) Jenkins.XSTREAM2.fromXML(legacyXml);
        assertDoesNotThrow(() -> loaded.getItems(j.jenkins));
    }

    @Test
    @Issue("JENKINS-59551")
    void legacyConfigWithoutColumnsGetsDefaultColumns() throws Exception {
        ListViewSection original = new ListViewSection("lvs", Width.THIRD, Positioning.CENTER);
        String xml = Jenkins.XSTREAM2.toXML(original);
        String legacyXml = xml.replaceFirst("(?s)<columns>.*?</columns>|<columns/>", "");
        assertThat("setup: columns element must be stripped", legacyXml, not(containsString("<columns")));

        ListViewSection loaded = (ListViewSection) Jenkins.XSTREAM2.fromXML(legacyXml);
        List<String> names = new ArrayList<>();
        for (hudson.views.ListViewColumn c : loaded.getColumns()) {
            names.add(c.getClass().getSimpleName());
        }
        assertThat(names, contains(
                "StatusColumn", "WeatherColumn", "JobColumn", "LastSuccessColumn",
                "LastFailureColumn", "LastDurationColumn", "BuildButtonColumn"));
    }
}
