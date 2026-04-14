# Releasing

## Prerequisites

- Push access to `jenkinsci/sectioned-view-plugin`
- Maven credentials configured for the Jenkins artifact repository

## Steps

1. **Update CHANGELOG.md** — add a new section at the top:
   ```
   ### Release X.Y - DD Mon YYYY
   
   * Change description ([PR #N](https://github.com/jenkinsci/sectioned-view-plugin/pull/N))
   ```

2. **Commit the changelog update:**
   ```bash
   git add CHANGELOG.md
   git commit -m "Update CHANGELOG.md for X.Y release"
   ```

3. **Run the Maven release:**
   ```bash
   mvn release:prepare release:perform
   ```
   This creates the `sectioned-view-X.Y` tag and bumps `revision` in pom.xml for the next development iteration.

4. **Create a GitHub Release** from the new tag:
   ```bash
   gh release create sectioned-view-X.Y \
     --title "Sectioned View X.Y" \
     --notes "PASTE CHANGELOG NOTES HERE" \
     --verify-tag
   ```

5. **Deploy to jenkins.efecte.com** (optional, if not waiting for plugin update center):
   ```bash
   mvn package -DskipTests
   # Copy target/sectioned-view.hpi to /var/lib/jenkins/plugins/
   ```
