# Changelog

### Release 1.29 - 14 Nov 2024

* Migrate to Jakarta EE 9 ([PR #45](https://github.com/jenkinsci/sectioned-view-plugin/pull/45))

### Release 1.28 - 29 Oct 2024

* Remote YUI tree view and replace with jelly based rendering ([PR #38](https://github.com/jenkinsci/sectioned-view-plugin/pull/38))

### Release 1.27 - 06 Aug 2024

* Run Jenkins Security Scan with Java 8 ([PR #40](https://github.com/jenkinsci/sectioned-view-plugin/pull/40))
* Enable Jenkins Security Scan ([PR #39](https://github.com/jenkinsci/sectioned-view-plugin/pull/39))
* [JENKINS-73571](https://issues.jenkins.io/browse/JENKINS-73571) Replace method include() (not working with Jenkins >= 2.426.x) with the includes() (pure javascript method that should work with all modern browsers) ([PR #37](https://github.com/jenkinsci/sectioned-view-plugin/pull/37))

### Release 1.26 - 15 Dec 2023

* [JENKINS-45038](https://issues.jenkins-ci.org/browse/JENKINS-45038) Add "Recurse in subfolders" option to sectioned view plugin ([PR #19](https://github.com/jenkinsci/sectioned-view-plugin/pull/19))
* [JENKINS-58418](https://issues.jenkins-ci.org/browse/JENKINS-58418) Fixed escaping of similar job names that could cause saving of view to throw an exception ([PR #25](https://github.com/jenkinsci/sectioned-view-plugin/pull/25))

### Release 1.25 - 27 Aug 2018

* [JENKINS-52399](https://issues.jenkins-ci.org/browse/JENKINS-52399) Add Folder Listing Section
* [JENKINS-37174](https://issues.jenkins-ci.org/browse/JENKINS-37174) Restore itemGroup var used by column to calculate relative job names

### Release 1.24 - 18 Aug 2017

* [JENKINS-44987](https://issues.jenkins-ci.org/browse/JENKINS-44987) Push section names through markup formatter

### Release 1.23 - 30 May 2017

* Fix repo URL

### Release 1.22 - 29 May 2017

* Findbugs

### Release 1.21 - 29 May 2017

* [JENKINS-26818](https://issues.jenkins-ci.org/browse/JENKINS-26818) fixed issues with test trend display and upgraded to latest jenkins
* [JENKINS-37174](https://issues.jenkins-ci.org/browse/JENKINS-37174) Eliminate some of the duplicate item crunching, do not enumerate section items repeatedly while loading the view

### Release 1.20 - 18 Nov 2015

* [JENKINS-25920](https://issues.jenkins-ci.org/browse/JENKINS-25920) Update UI style to match that in newer Jenkins versions
* [JENKINS-8276](https://issues.jenkins-ci.org/browse/JENKINS-8276) Fixed issue with View Job Filters in List View Section
* Job names in List View Section inside a Folder are now relative to that folder
* [JENKINS-30799](https://issues.jenkins-ci.org/browse/JENKINS-30799) Job list in List View Section config is now scrollable
* View Listing Section style updated to match newer Jenkins
* Fixed View Job Filters for jobs in Folders
* [JENKINS-19376](https://issues.jenkins-ci.org/browse/JENKINS-19376) + [JENKINS-10612](https://issues.jenkins-ci.org/browse/JENKINS-10612) - Fixed display of 3-column layout

### Release 1.19 - 10 Jul 2015

* Support jobs within subfolders. [Changes](https://github.com/jenkinsci/sectioned-view-plugin/commit/e405ad06b55ddf8a2a98f6998d5975e771b47bea).
* [JENKINS-29250](https://issues.jenkins-ci.org/browse/JENKINS-29250) - Sectioned View appearance broken in 1.619

### Release 1.18 - 17 Jun 2013

* Support CloudBees Folders or other item groups.
* Word wrap text sections even style is None.
* Fixed problems with textarea in text view sections introduced in 1.17.

### Release 1.17 - 5 Jun 2013

* Security flaw: need to use configured markup formatter for text view sections.
* Better static resource caching.

### Release 1.16 - 4 Dec 2011

* Fixed word wrapping in text section [JENKINS-11755](https://issues.jenkins-ci.org/browse/JENKINS-11755)
* Fixed nested view tree in view listing section for recent versions of Jenkins [JENKINS-11702](https://issues.jenkins-ci.org/browse/JENKINS-11702)

### Release 1.15 - 23 Mar 2011

* Fixed NPE in View Listing Section when no views were selected.
* Cleaned up plugin licensing (MIT License).

### Release 1.14 - 3 Feb 2011

* Added support for ViewsTabBar extensions (such as DropDown ViewsTabBar Plugin or Extension Point for Project Views Navigation).
* Replaced some occurrences of "Hudson" with "Jenkins" and updated links to point to new Jenkins wiki.

### Release 1.13 - 13 Dec 2010

* Built against Jenkins 1.388
* Applied patch for [JENKINS-8276](https://issues.jenkins-ci.org/browse/JENKINS-8276) - Added ability to use View Job Filters on all Sectioned View Sections.

### Release 1.12 - 1 Nov 2010

* Requires Jenkins 1.377 or higher
* Added a new section type for displaying a listing of selected views. Supports the Nested View Plugin for hierarchical views.

### Release 1.11 - 15 Jun 2010

* Requires Jenkins 1.345 or higher
* Made job names clickable when configuring sections
* Some community-contributed localizations
* Added support for labelled test group graphs in Test Result and Job Graphs sections (see LabeledTestGroupsPublisher Plugin)

### Release 1.10 - 23 Feb 2010

* Fixed url to compiler warnings trend graph when using a newer version of the Warnings Plugin - [JENKINS-5725](https://issues.jenkins-ci.org/browse/JENKINS-5725)
* Note: this version will now fail to display the warnings when used with older versions of the Warnings Plugin
* Added some new graphs to Job Graphs Section
* Open Tasks trend
* Static Analysis combined trend

### Release 1.9 - 22 Feb 2010

* Added a Tip style option to the Text Section (to match the style of the confluence tip macro)
* Integrated some French community-contributed translations

### Release 1.8 - 28 Jan 2010

* Added an option for selecting a Text Section's style (currently just note/info/warning style)
* Newly added List View Sections now contain the standard columns by default

### Release 1.7 - 23 Jan 2010

* Requires Jenkins 1.342 or higher
* Fix 1.342 Jenkins core compatibility

### Release 1.6

* Fixed [JENKINS-4844](https://issues.jenkins-ci.org/browse/JENKINS-4844) - Sectioned View in "My Views" now shows the correct tabs.

### Release 1.5

* Requires Jenkins 1.329 or higher
* Added ability to configure columns in List View Section just like in the standard List View

### Release 1.4

* Fixed table layout on configuration page
* Added violation graph support for Freestyle projects to the Job Graphs Section

### Release 1.3

* Added "Text Section" type
* Added section width and positioning settings

### Release 1.2

* Support for regex-based job includes for each section
* First implementation of "Job Graphs Section" type

### Release 1.1 (release failed, skipping this version)

### Release 1.0

* Initial release.
* Implemented "Test Result Section" and standard "List View Section"
