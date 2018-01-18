package com.honeycomb.colorphone.resultpage.data;

import android.support.annotation.NonNull;

public class CardData implements Comparable {

    private int cardType;

    public CardData(int cardType){
        this.cardType = cardType;
    }

    public int getCardType(){
        return cardType;
    }

    public int getPriority() {
        switch (cardType) {
            // Security ＞ Max ＞ Battery ＞ Boost+ ＞ Junk Cleaner > CPU Cooler > Accessibility
            case ResultConstants.CARD_VIEW_TYPE_SECURITY: return 7;
            case ResultConstants.CARD_VIEW_TYPE_MAX_GAME_BOOSTER: return 6;
            case ResultConstants.CARD_VIEW_TYPE_MAX_APP_LOCKER: return 6;
            case ResultConstants.CARD_VIEW_TYPE_MAX_DATA_THIEVES: return 6;
            case ResultConstants.CARD_VIEW_TYPE_BATTERY: return 5;
            case ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS: return 4;
            case ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER: return 3;
            case ResultConstants.CARD_VIEW_TYPE_CPU_COOLER: return 2;
            case ResultConstants.CARD_VIEW_TYPE_DEFAULT: return 0;
            default: return 0;
        }
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof CardData) {
            return ((CardData) o).getPriority() - getPriority(); // Descending order by priority
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CardData) {
            return cardType == ((CardData) obj).cardType;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        switch (cardType) {
            case ResultConstants.CARD_VIEW_TYPE_BATTERY: return "Battery";
            case ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS: return "BoostPlus";
            case ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER: return "JunkCleaner";
            case ResultConstants.CARD_VIEW_TYPE_SECURITY: return "Security";
            case ResultConstants.CARD_VIEW_TYPE_CPU_COOLER: return "CpuCooler";
            case ResultConstants.CARD_VIEW_TYPE_MAX_GAME_BOOSTER: return "Max|GameBooster";
            case ResultConstants.CARD_VIEW_TYPE_MAX_APP_LOCKER: return "Max|AppLocker";
            case ResultConstants.CARD_VIEW_TYPE_MAX_DATA_THIEVES: return "Max|DataThieves";
            case ResultConstants.CARD_VIEW_TYPE_DEFAULT: return "Default";
            default: return "Unknown";
        }
    }
}
