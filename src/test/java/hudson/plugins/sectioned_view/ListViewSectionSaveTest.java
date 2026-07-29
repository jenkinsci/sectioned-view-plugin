package hudson.plugins.sectioned_view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import hudson.plugins.sectioned_view.SectionedViewSection.Positioning;
import hudson.plugins.sectioned_view.SectionedViewSection.Width;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest2;

@WithJenkins
class ListViewSectionSaveTest {

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    /** Models a legacy/third-party column: no @DataBoundConstructor, descriptor overrides newInstance. */
    public static class LegacyColumn extends ListViewColumn {
        @TestExtension("legacyColumnSurvivesViewSave")
        public static class DescriptorImpl extends ListViewColumnDescriptor {
            @Override
            public ListViewColumn newInstance(StaplerRequest2 req, JSONObject formData) {
                return new LegacyColumn();
            }

            @Override
            public String getDisplayName() {
                return "Legacy Column";
            }
        }
    }

    public static class CountingColumn extends ListViewColumn {
        static final AtomicInteger CONSTRUCTIONS = new AtomicInteger();

        @DataBoundConstructor
        public CountingColumn() {
            CONSTRUCTIONS.incrementAndGet();
        }

        @TestExtension("viewSaveConstructsEachColumnOnce")
        public static class DescriptorImpl extends ListViewColumnDescriptor {
            @Override
            public String getDisplayName() {
                return "Counting Column";
            }
        }
    }

    @Test
    @Issue("JENKINS-59551")
    void legacyColumnSurvivesViewSave() throws Exception {
        SectionedView view = new SectionedView("sw");
        j.jenkins.addView(view);
        ListViewSection section = new ListViewSection("lvs", Width.FULL, Positioning.CENTER);
        section.setColumns(List.of(new LegacyColumn()));
        view.setSections(List.of(section));

        assertDoesNotThrow(() -> j.configRoundtrip(view));
    }

    @Test
    @Issue("JENKINS-59551")
    void viewSaveConstructsEachColumnOnce() throws Exception {
        SectionedView view = new SectionedView("sw");
        j.jenkins.addView(view);
        ListViewSection section = new ListViewSection("lvs", Width.FULL, Positioning.CENTER);
        section.setColumns(List.of(new CountingColumn()));
        view.setSections(List.of(section));

        CountingColumn.CONSTRUCTIONS.set(0);
        j.configRoundtrip(view);

        assertThat(CountingColumn.CONSTRUCTIONS.get(), is(1));
    }
}
