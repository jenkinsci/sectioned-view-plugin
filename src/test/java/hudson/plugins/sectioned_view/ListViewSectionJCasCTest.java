package hudson.plugins.sectioned_view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import hudson.views.ListViewColumn;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
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
}
