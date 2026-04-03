/*
 * The MIT License
 *
 * Copyright (c) 2009-2015, Timothy Bingaman, Richard Thomas, Perrin Morrow
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

import com.cloudbees.hudson.plugins.folder.Folder;
import com.google.common.base.Joiner;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class FolderViewSection extends SectionedViewSection {

    private int viewColumns;
    private Integer folderLevels;
    private boolean hideJobs;
    private String regexFilter;
    private transient Pattern regexFilterPattern;

    @DataBoundConstructor
    public FolderViewSection(String name, Width width, Positioning alignment, int viewColumns, int folderLevels,
                             boolean hideJobs, String regexFilter) {
        super(name, width, alignment);
        this.setViewColumns(viewColumns);
        this.setFolderLevels(folderLevels);
        this.setHideJobs(hideJobs);
        this.setRegexFilter(regexFilter);
    }

    public boolean isChecked(Folder folder, ItemGroup itemGroup) {
        Collection<TopLevelItem> selectedItems = getItems(itemGroup);
        for (TopLevelItem item: selectedItems) {
            if (folder == item) {
                return true;
            }
        }
        return false;
    }

    public List<List<Node>> getColumnsWithContent(ItemGroup<? extends TopLevelItem> itemGroup, int baseFolderLevel) {
        List<List<Node>> columns = new ArrayList<List<Node>>();
        for (int i = 0; i < viewColumns; i++) {
            columns.add(new ArrayList<Node>());
        }

        List<Node> filteredNodes = convert(getItems(itemGroup), baseFolderLevel);

        int columnIndex = 0;
        for (Node node : filteredNodes) {
            columns.get(columnIndex).add(node);
            columnIndex = columnIndex == viewColumns -1 ? 0 : columnIndex + 1;
        }

        return columns;
    }

    public List<Node> convert(Collection<TopLevelItem> items, int baseFolderLevel) {
        List<Node> nodes = new ArrayList<Node>();
        for (TopLevelItem item : items) {
            Node node = new Node(item);
            addChildren(item, node, baseFolderLevel);
            nodes.add(node);
        }
        return nodes;
    }

    public void addChildren(TopLevelItem item, Node node, int baseFolderLevel) {
        if (item instanceof Folder) {
            Folder folder = (Folder)item;
            if (folder.getItems() != null) {
                node.setChildNodes(new ArrayList<Node>());
                for (TopLevelItem childItem : folder.getItems()) {
                    if(filter(childItem, baseFolderLevel)) {
                        Node childNode = new Node(childItem);
                        node.getChildNodes().add(childNode);
                        addChildren(childItem, childNode, baseFolderLevel);
                    }
                }
            }
        }
    }

    private boolean filter(TopLevelItem item, int baseFolderLevel) {
        boolean allow = true;
        if (!(item instanceof Folder) & hideJobs) allow = false;
        if (regexFilter != null && !regexFilter.isEmpty() && !regexFilterPattern.matcher(item.getName()).matches()) allow = false;
        if (folderLevels != null && item.getFullName().length() - item.getFullName().replace("/", "").length() >
                (folderLevels + baseFolderLevel)) allow = false;
        return allow;
    }

    public Integer getFolderLevels() {
        return folderLevels;
    }

    public void setFolderLevels(Integer folderLevels) {
        this.folderLevels = folderLevels;
    }

    public boolean isHideJobs() {
        return hideJobs;
    }

    public void setHideJobs(boolean hideJobs) {
        this.hideJobs = hideJobs;
    }

    public String getRegexFilter() {
        return regexFilter;
    }

    public void setRegexFilter(String regexFilter) {
        this.regexFilter = regexFilter;
        this.regexFilterPattern = Pattern.compile(regexFilter);
    }

    public int getViewColumns() {
        return viewColumns;
    }

    public void setViewColumns(int viewColumns) {
        this.viewColumns = viewColumns;
    }

    public String getSelectedFoldersString(ItemGroup<? extends TopLevelItem> itemGroup) {

        String parentName = "";
        if (itemGroup instanceof Folder) {
            Folder folder = (Folder) itemGroup;
            parentName = folder.getFullName();
        }

        Collection<TopLevelItem> selectedItems = getItems(itemGroup);
        if (selectedItems == null || selectedItems.isEmpty()) return "";

        StringBuilder buf = new StringBuilder();
        for (TopLevelItem item : selectedItems) {
            if (parentName.length() < item.getFullName().length()) {
                String itemFullName = item.getFullName().substring(parentName.length());
                itemFullName = itemFullName.charAt(0) == '/' ? itemFullName.substring(1) : itemFullName;
                buf.append(",").append(itemFullName.replace("/", "$"));
            }
        }
        return buf.substring(1);
    }

    public int baseFolderLevel(SectionedView sectionedView) {
        return calculateBaseFolderLevel(sectionedView);
    }

    private static int calculateBaseFolderLevel(SectionedView sectionedView) {
        int folderLevel = 0;
        if (sectionedView.getOwner() instanceof Folder) {
            Folder folder = (Folder) sectionedView.getOwner();
            folderLevel = folderLevel(folder.getFullName()) + 1;
        }
        return folderLevel;
    }

    private static int folderLevel(String fullName) {
        return fullName.length() - fullName.replace("/", "").length();
    }

    protected Object readResolve() {
        super.readResolve();
        if (regexFilter != null)
            regexFilterPattern = Pattern.compile(regexFilter);
        return this;
    }

    @Extension(optional = true)
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        public DescriptorImpl() throws Exception {
            if (Jenkins.getInstance().getPlugin("cloudbees-folder") == null) {
                throw new Exception("Can't instantiate without CloudBees Folder plugin installed");
            }
        }

        @Override
        public String getDisplayName() {
            return "Folder Listing Section";
        }

        public Collection<Item> getAllFolders(ItemGroup<Item> itemGroup) {
            Collection<Item> items = itemGroup.getItems(item -> item instanceof Folder);
            return items;
        }
    }

    public static class Node {

        private Item item;
        private List<Node> childNodes;

        public Node(Item item) {
            this.item = item;
        }

        public String getName() {
            return item.getDisplayName();
        }

        public void setChildNodes(List<Node> childNodes) {
            this.childNodes = childNodes;
        }

        public List<Node> getChildNodes() {
            return childNodes;
        }

        public Item getItem() {
            return item;
        }

        public String getUrl() {
            if(item.getParent() instanceof  Folder) {
                Folder folder = (Folder) item.getParent();
                return "job/" + encodePathSegments(folder.getFullName()).replace("/", "/job/") + "/" +
                        item.getParent().getUrlChildPrefix() + "/" + Util.rawEncode(item.getName()) + "/";
            } else {
                return "job/" + Util.rawEncode(item.getName()) + "/";
            }
        }

        private String encodePathSegments(String uri) {
            String[] segments = uri.split("/");
            for (int i = 0; i < segments.length; i++) {
                segments[i] = Util.rawEncode(segments[i]);
            }
            return Joiner.on("/").join(segments);
        }
    }
}
