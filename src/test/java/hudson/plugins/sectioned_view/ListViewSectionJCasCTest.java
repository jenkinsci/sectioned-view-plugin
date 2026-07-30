package hudson.plugins.sectioned_view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static io.jenkins.plugins.casc.misc.Util.getJenkinsRoot;
import static io.jenkins.plugins.casc.misc.Util.toYamlString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.views.ListViewColumn;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import io.jenkins.plugins.casc.model.CNode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

@WithJenkinsConfiguredWithCode
class ListViewSectionJCasCTest {

    private static List<String> columnClassNames(ListViewSection section) {
        List<String> names = new ArrayList<>();
        for (ListViewColumn c : section.getColumns()) {
            names.add(c.getClass().getSimpleName());
        }
        return names;
    }

    private static ListViewSection firstSection(JenkinsConfiguredWithCodeRule r) {
        SectionedView view = (SectionedView) r.jenkins.getView("test");
        return (ListViewSection) view.getSections().iterator().next();
    }

    @Test
    @ConfiguredWithCode("columns.yaml")
    @Issue("JENKINS-59551")
    void columnsAreBoundFromYaml(JenkinsConfiguredWithCodeRule r) {
        assertThat(columnClassNames(firstSection(r)), contains(
                "StatusColumn", "JobColumn", "LastSuccessColumn",
                "LastFailureColumn", "LastDurationColumn"));
    }

    @Test
    @ConfiguredWithCode("no-columns.yaml")
    @Issue("JENKINS-59551")
    void omittedColumnsFallBackToDefaults(JenkinsConfiguredWithCodeRule r) {
        assertThat(columnClassNames(firstSection(r)), contains(
                "StatusColumn", "WeatherColumn", "JobColumn", "LastSuccessColumn",
                "LastFailureColumn", "LastDurationColumn", "BuildButtonColumn"));
    }

    @Test
    @ConfiguredWithCode("columns.yaml")
    @Issue("JENKINS-59551")
    void exportRoundTripsColumns(JenkinsConfiguredWithCodeRule r) throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        CNode viewsNode = getJenkinsRoot(context).get("views");
        String exported = toYamlString(viewsNode);

        assertThat(exported, containsString("listViewSection"));
        assertThat(exported, stringContainsInOrder("columns",
                "status", "jobName", "lastSuccess", "lastFailure", "lastDuration"));
    }

    @Test
    @ConfiguredWithCode("jobnames.yaml")
    @Issue("JENKINS-59551")
    void jobNamesAreBoundFromYaml(JenkinsConfiguredWithCodeRule r) {
        ListViewSection section = firstSection(r);
        assertThat(section.getJobNames(), containsInAnyOrder("Job Alpha", "Job Beta"));
        assertTrue(section.getJobNames().contains("job alpha"),
                "job name lookup must stay case-insensitive");
    }
}
