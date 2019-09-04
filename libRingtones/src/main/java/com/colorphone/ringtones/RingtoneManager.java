package com.colorphone.ringtones;

import com.colorphone.ringtones.module.Column;
import com.colorphone.ringtones.module.Ringtone;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sundxing
 */
public class RingtoneManager {

    private static final boolean TEST_MODE = true;

    private List<Column> mSubColumns = new ArrayList<>();
    private static RingtoneManager sColumnManager  = new RingtoneManager();


    public static RingtoneManager getInstance() {
        return sColumnManager;
    }

    public boolean isSubColumnsReady() {
        // TODO
        if (TEST_MODE) {
            ensureSubColumns();
            return true;
        }

        return false;
    }

    private void ensureSubColumns() {
        if (mSubColumns.isEmpty()) {
            mSubColumns.add(new Column("303989", "网络流行"));
            mSubColumns.add(new Column("303993", "华语金曲"));
            mSubColumns.add(new Column("303997", "个性搞笑"));
            mSubColumns.add(new Column("304001", "欧美日韩"));
            mSubColumns.add(new Column("304005", "DJ舞曲"));
            mSubColumns.add(new Column("304009", "动人情歌"));
            mSubColumns.get(0).setSelected(true);
        }
    }

    public List<Column> getSubColumns() {
        ensureSubColumns();
        return mSubColumns;
    }

    public Column getSelectedSubColumn() {
        for (Column column : mSubColumns) {
            if (column.isSelected()) {
                return column;
            }
        }
        return null;
    }

    public interface RingtoneSetHandler {
        void onSetRingtone(Ringtone ringtone);
    }

}
