package com.colorphone.ringtones.module;

import com.colorphone.ringtones.bean.ColumnBean;

public class Column {
    /**
     * Column id
     */
    private String id;
    private String name;
    private boolean isSelected;

    public Column() {}

    public Column(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Column valueOf(ColumnBean columnBean) {
        Column column = new Column();
        column.id = columnBean.getTargetid();
        column.name = columnBean.getName();
        return column;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
