package hudson.plugins.sectioned_view;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.Functions;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Pattern;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * @author ogondza.
 */
@WithJenkins
class TestResultViewSectionTest {

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void showIt() throws Exception {
        assumeFalse(Functions.isWindows() && System.getenv("CI") != null, "TODO seems to crash the test JVM in CI builds");
        FreeStyleProject p = j.createFreeStyleProject("test_project");
        p.getPublishersList().add(new JUnitResultArchiver("*.xml"));
        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child("result.xml").copyFrom(TestResultViewSectionTest.class.getResourceAsStream("junit-report-1472.xml"));
                return true;
            }
        });
        j.waitForCompletion(j.buildAndAssertStatus(Result.UNSTABLE, p));
        j.waitForCompletion(j.buildAndAssertStatus(Result.UNSTABLE, p));

        SectionedView sectionedView = new SectionedView("sw");
        j.jenkins.addView(sectionedView);
        TestResultViewSection tests = new TestResultViewSection("tests view", SectionedViewSection.Width.FULL, SectionedViewSection.Positioning.CENTER);
        tests.includeRegex = ".*";
        tests.includePattern = Pattern.compile(".*");
        sectionedView.setSections(Collections.singletonList(tests));
        sectionedView.save();

        String out;
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            out = wc.getPage(sectionedView).getWebResponse().getContentAsString();
        }

        assertTrue(out.contains("tests view"), out);
        assertTrue(out.contains("test_project"), out);
        assertTrue(out.contains("1 failure"), out);
    }
}
