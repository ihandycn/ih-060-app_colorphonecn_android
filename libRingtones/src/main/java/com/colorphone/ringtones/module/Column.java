package com.colorphone.ringtones.module;

import com.colorphone.ringtones.bean.ColumnBean;

public class Column {
    /**
     * Column id
     */
    private String id;
    private String name;
    private boolean isSelected;

    public static Column valueOf(ColumnBean columnBean) {
        Column column = new Column();
        column.id = columnBean.getTargetid();
        column.name = columnBean.getName();
        return column;
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
