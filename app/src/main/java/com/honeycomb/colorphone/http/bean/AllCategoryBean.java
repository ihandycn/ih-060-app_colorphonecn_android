package com.honeycomb.colorphone.http.bean;

import java.util.List;

public class AllCategoryBean {

    private List<CategoryItem> category_list;

    public List<CategoryItem> getCategories() {
        return category_list;
    }

    public void setCategories(List<CategoryItem> categories) {
        this.category_list = categories;
    }

    private static class CategoryItem {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
